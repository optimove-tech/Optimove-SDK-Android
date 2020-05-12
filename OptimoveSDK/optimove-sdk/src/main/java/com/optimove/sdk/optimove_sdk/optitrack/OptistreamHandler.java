package com.optimove.sdk.optimove_sdk.optitrack;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamQueue;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class OptistreamHandler implements LifecycleObserver.ActivityStopped {

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private OptistreamQueue optistreamQueue;
    @NonNull
    private OptitrackConfigs optitrackConfigs;

    private boolean initialized = false;
    private boolean currentlyDispatching = false;


    private final static class Constants {
        private static final int EVENT_BATCH_LIMIT = 100;
        private static final int DISPATCH_INTERVAL_IN_SECONDS = 30;

    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamQueue optistreamQueue,
                             @NonNull OptitrackConfigs optitrackConfigs) {
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamQueue = optistreamQueue;
        this.optitrackConfigs = optitrackConfigs;
    }

    private synchronized void ensureInit() {
        if (!initialized) {
            //this will not cause a leak because this component is tied to the app context
            this.lifecycleObserver.addActivityStoppedListener(this);
            this.initialized = true;
        }
    }

    public void reportEvent(OptistreamEvent optistreamEvent) {
        this.ensureInit();
        optistreamQueue.enqueue(optistreamEvent);
        if (optistreamEvent.getMetadata().isRealtime() || isNotificationEvent(optistreamEvent)) {
            startDispatching();
        }
    }
    @Override
    public void activityStopped() {
        this.ensureInit();
        this.startDispatching();
    }


    private synchronized void startDispatching() {
        if (currentlyDispatching) {
            OptiLoggerStreamsContainer.debug("Already dispatching");
            //flag to dispatch again when finished
            return;
        }

        currentlyDispatching = true;
        dispatchNextBulk();
    }

    private void dispatchNextBulk() {
        if (optistreamQueue.size() == 0) {
            OptiLoggerStreamsContainer.debug("No events to dispatch");
            currentlyDispatching = false;
            return;
        }
        List<OptistreamEvent> eventsToDispatch = optistreamQueue.first(Constants.EVENT_BATCH_LIMIT);

        try {
            JSONArray optistreamEventsJson = new JSONArray(new Gson().toJson(eventsToDispatch));
            OptiLoggerStreamsContainer.debug("Dispatching optistream events");

            httpClient.postJsonArray(optitrackConfigs.getOptitrackEndpoint(), optistreamEventsJson)
                    .errorListener(this::dispatchingFailed)
                    .successListener(response -> dispatchingSucceeded(eventsToDispatch))
                    .send();
        } catch (JSONException e) {
            OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                    e.getMessage());
        }
    }

    private void dispatchingFailed(Exception e) {
        OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                e.getMessage());
        this.currentlyDispatching = false;
    }

    private void dispatchingSucceeded(List<OptistreamEvent> optistreamEvents) {
        OptiLoggerStreamsContainer.debug("Events were dispatched");
        optistreamQueue.remove(optistreamEvents);
        dispatchNextBulk();
    }

    private boolean isNotificationEvent(OptistreamEvent optistreamEvent) {
        return optistreamEvent.getName()
                .equals(TriggeredNotificationDeliveredEvent.NAME) ||
                optistreamEvent.getName()
                        .equals(TriggeredNotificationOpenedEvent.NAME) ||
                optistreamEvent.getName()
                        .equals(ScheduledNotificationDeliveredEvent.NAME) ||
                optistreamEvent.getName()
                        .equals(ScheduledNotificationOpenedEvent.NAME);
    }


}
