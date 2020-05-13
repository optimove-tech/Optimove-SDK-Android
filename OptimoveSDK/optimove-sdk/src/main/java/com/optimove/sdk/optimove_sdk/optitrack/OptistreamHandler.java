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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptistreamHandler implements LifecycleObserver.ActivityStopped {

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private OptistreamDbHelper optistreamDbHelper;
    @NonNull
    private OptitrackConfigs optitrackConfigs;
    @NonNull
    private ExecutorService singleThreadExecutor;
    @NonNull
    private Gson optistreamGson;


    private boolean initialized = false;
    private boolean currentlyDispatching = false;


    private final static class Constants {
        private static final int EVENT_BATCH_LIMIT = 100;
        private static final int DISPATCH_INTERVAL_IN_SECONDS = 30;

    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamDbHelper optistreamDbHelper,
                             @NonNull OptitrackConfigs optitrackConfigs) {
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamDbHelper = optistreamDbHelper;
        this.optitrackConfigs = optitrackConfigs;
        this.singleThreadExecutor = Executors.newSingleThreadExecutor();
        this.optistreamGson = new Gson();
    }

    private synchronized void ensureInit() {
        if (!initialized) {
            //this will not cause a leak because this component is tied to the app context
            this.lifecycleObserver.addActivityStoppedListener(this);
            this.initialized = true;
        }
    }

    public void reportEvents(List<OptistreamEvent> optistreamEvents) {
        this.ensureInit();
        singleThreadExecutor.submit(() -> {
            boolean immediateEventFound = false;
            for (OptistreamEvent optistreamEvent: optistreamEvents) {
                if (optistreamEvent.getMetadata().isRealtime() || isNotificationEvent(optistreamEvent)) {
                    optistreamDbHelper.insertEvent(optistreamGson.toJson(optistreamEvent));
                    immediateEventFound = true;
                }
            }
            if (immediateEventFound) {
                singleThreadExecutor.submit(this::dispatchBulkIfExists);
            }
        });
    }

    private void dispatchBulkIfExists(){
        OptistreamDbHelper.EventsBulk eventsBulk = optistreamDbHelper.getFirstEvents(Constants.EVENT_BATCH_LIMIT);
        List<String> eventJsons = eventsBulk.getEventJsons();
        if (!eventJsons.isEmpty()) {
            try {
                JSONArray optistreamEventsJson = new JSONArray(optistreamGson.toJson(eventJsons));
                OptiLoggerStreamsContainer.debug("Dispatching optistream events");

                httpClient.postJsonArray(optitrackConfigs.getOptitrackEndpoint(), optistreamEventsJson)
                        .errorListener(error ->
                            OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                                    error.getMessage()))
                        .successListener(response -> {
                            OptiLoggerStreamsContainer.debug("Events were dispatched");
                            singleThreadExecutor.submit(()-> {
                                optistreamDbHelper.removeEvents(eventsBulk.getLastId());
                                dispatchBulkIfExists();
                            });
                        })
                        .send();
            } catch (JSONException e) {
                OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                        e.getMessage());
            }
        }

    }
    @Override
    public void activityStopped() {
        this.ensureInit();
        singleThreadExecutor.submit(this::dispatchBulkIfExists);
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
