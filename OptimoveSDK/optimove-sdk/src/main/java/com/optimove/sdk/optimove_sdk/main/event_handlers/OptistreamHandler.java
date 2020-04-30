package com.optimove.sdk.optimove_sdk.main.event_handlers;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.Metadata;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class OptistreamHandler extends EventHandler implements LifecycleObserver.ActivityStopped {

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private UserInfo userInfo;
    @NonNull
    private Map<String, EventConfigs> eventConfigsMap;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private OptistreamQueue optistreamQueue;
    @NonNull
    private OptitrackConfigs optitrackConfigs;

    @NonNull
    private Metadata metadata;
    private boolean initialized = false;
    private boolean currentlyDispatching = false;
    private Timer dispatcherTimer;


    private final static class Constants {
        private static final String CATEGORY = "track";
        private static final String PLATFORM = "Android";
        private static final String ORIGIN = "sdk";
        private static final int EVENT_BATCH_LIMIT = 100;
        private static final int DISPATCH_INTERVAL_IN_SECONDS = 30;

    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull UserInfo userInfo,
                             @NonNull Map<String, EventConfigs> eventConfigsMap,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamQueue optistreamQueue,
                             @NonNull OptitrackConfigs optitrackConfigs) {
        this.httpClient = httpClient;
        this.userInfo = userInfo;
        this.eventConfigsMap = eventConfigsMap;
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamQueue = optistreamQueue;
        this.optitrackConfigs = optitrackConfigs;
        this.metadata = new Metadata(BuildConfig.VERSION_NAME, Constants.PLATFORM);
        this.dispatcherTimer = new Timer();

    }

    private synchronized void init() {
        if (!initialized) {
            //this will not cause a leak because this component is tied to the app context
            this.lifecycleObserver.addActivityStoppedListener(this);
            this.scheduleNextDispatch();
            this.initialized = true;
        }
    }

    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        optistreamQueue.enqueue(convertOptimoveToOptistreamEvent(optimoveEvent));
        if (eventConfigsMap.get(optimoveEvent.getName())
                .isSupportedOnRealtime() || isNotificationEvent(optimoveEvent)) {
            startDispatching();
        }
    }

    private void scheduleNextDispatch() {
        dispatcherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startDispatching();
            }
        }, Constants.DISPATCH_INTERVAL_IN_SECONDS * 1000);
    }


    @Override
    public void activityStopped() {
        this.startDispatching();
    }

    private synchronized void startDispatching() {
        if (currentlyDispatching) {
            OptiLoggerStreamsContainer.debug("Already dispatching");
            //flag to dispatch again when finished
            return;
        }

        dispatcherTimer.cancel();
        currentlyDispatching = true;
        dispatchNextBulk();
    }

    private void dispatchNextBulk() {
        if (optistreamQueue.size() == 0) {
            OptiLoggerStreamsContainer.debug("No events to dispatch");
            currentlyDispatching = false;
            scheduleNextDispatch();
            return;
        }
        List<OptistreamEvent> eventsToDispatch = optistreamQueue.first(Constants.EVENT_BATCH_LIMIT);

        try {
            JSONObject optistreamEventsJson = new JSONObject(new Gson().toJson(eventsToDispatch));
            OptiLoggerStreamsContainer.debug("Dispatching optistream events");

            httpClient.postJson(optitrackConfigs.getOptitrackEndpoint(), optistreamEventsJson)
                    .errorListener(this::dispatchingFailed)
                    .successListener(response -> dispatchingSucceeded(eventsToDispatch))
                    .destination("asdknmlksaflkasdfslkvmaslkmalskvmlsakv")
                    .send();
        } catch (JSONException e) {
            OptiLoggerStreamsContainer.debug("Events dispatching failed",
                    e.getMessage());
        }
    }

    private void dispatchingFailed(Exception e) {
        OptiLoggerStreamsContainer.debug("Events dispatching failed",
                e.getMessage());
        scheduleNextDispatch();
        this.currentlyDispatching = false;
    }

    private void dispatchingSucceeded(List<OptistreamEvent> optistreamEvents) {
        OptiLoggerStreamsContainer.debug("Events were dispatched");
        optistreamQueue.remove(optistreamEvents);
        dispatchNextBulk();
    }

    private boolean isNotificationEvent(OptimoveEvent optimoveEvent) {
        return optimoveEvent.getName()
                .equals(TriggeredNotificationDeliveredEvent.NAME) ||
                optimoveEvent.getName()
                        .equals(TriggeredNotificationOpenedEvent.NAME) ||
                optimoveEvent.getName()
                        .equals(ScheduledNotificationDeliveredEvent.NAME) ||
                optimoveEvent.getName()
                        .equals(ScheduledNotificationOpenedEvent.NAME);
    }

    private OptistreamEvent convertOptimoveToOptistreamEvent(OptimoveEvent optimoveEvent) {
        return OptistreamEvent.builder()
                .withTenantId(optitrackConfigs.getSiteId())
                .withCategory(Constants.CATEGORY)
                .withName(optimoveEvent.getName())
                .withOrigin(Constants.ORIGIN)
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp(333)
                .withContext(optimoveEvent.getParameters())
                .withMetadata(metadata)
                .build();
    }


}
