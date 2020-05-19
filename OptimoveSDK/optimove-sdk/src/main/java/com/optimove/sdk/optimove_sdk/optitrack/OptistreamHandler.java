package com.optimove.sdk.optimove_sdk.optitrack;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OptistreamHandler implements LifecycleObserver.ActivityStopped {

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private OptistreamPersistanceAdapter optistreamPersistanceAdapter;
    @NonNull
    private OptitrackConfigs optitrackConfigs;
    @NonNull
    private ScheduledExecutorService singleThreadScheduledExecutor;
    @NonNull
    private Gson optistreamGson;


    @Nullable
    private ScheduledFuture timerDispatchFuture;
    //accessed ONLY by the single thread of the executor
    private boolean dispatchRequestWaitsForResponse = false;
    private boolean initialized = false;

    public final static class Constants {
        public static final int EVENT_BATCH_LIMIT = 100;
        public static final int DISPATCH_INTERVAL_IN_SECONDS = 30;

    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamPersistanceAdapter optistreamPersistanceAdapter,
                             @NonNull OptitrackConfigs optitrackConfigs) {
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamPersistanceAdapter = optistreamPersistanceAdapter;
        this.optitrackConfigs = optitrackConfigs;
        this.singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.optistreamGson = new Gson();
    }

    private synchronized void ensureInit() {
        if (!initialized) {
            //this will not cause a leak because this component is tied to the app context
            this.initialized = true;
            this.lifecycleObserver.addActivityStoppedListener(this);
            this.scheduleTheNextDispatch();
        }
    }

    public void reportEvents(List<OptistreamEvent> optistreamEvents) {
        this.ensureInit();
        singleThreadScheduledExecutor.submit(() -> {
            boolean immediateEventFound = false;
            for (OptistreamEvent optistreamEvent: optistreamEvents) {
                optistreamPersistanceAdapter.insertEvent(optistreamGson.toJson(optistreamEvent));
                if (optistreamEvent.getMetadata().isRealtime() || isNotificationEvent(optistreamEvent)) {
                    immediateEventFound = true;
                }
            }
            if (immediateEventFound) {
                if (timerDispatchFuture != null) {
                    timerDispatchFuture.cancel(false);
                }
                dispatchBulkIfExists();
            }
        });
    }

    private void dispatchBulkIfExists(){
        if (dispatchRequestWaitsForResponse) {
            return; //protects from sending same events twice
        }
        OptistreamDbHelper.EventsBulk eventsBulk = optistreamPersistanceAdapter.getFirstEvents(Constants.EVENT_BATCH_LIMIT);
        if (eventsBulk == null) {
            scheduleTheNextDispatch();
            return;
        }
        List<String> eventJsons = eventsBulk.getEventJsons();
        if (eventJsons != null && !eventJsons.isEmpty()) {
            try {
                JSONArray jsonArrayToDispatch = new JSONArray();
                for (String eventJson: eventJsons) {
                    jsonArrayToDispatch.put(new JSONObject(eventJson));
                }
                OptiLoggerStreamsContainer.debug("Dispatching " + eventJsons.size() + " optistream events - " + OptiUtils.getEventsNamesFromStrings(eventJsons));

                dispatchRequestWaitsForResponse = true;
                httpClient.postJsonArray(optitrackConfigs.getOptitrackEndpoint(), jsonArrayToDispatch)
                        .errorListener(error -> {
                            OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                                    error.getMessage());
                            // some error occurred, try again in the next dispatch
                            dispatchRequestWaitsForResponse = false;
                            scheduleTheNextDispatch();
                        })
                        .successListener(response -> {
                            OptiLoggerStreamsContainer.debug(eventJsons.size() + " Events were dispatched");
                            singleThreadScheduledExecutor.submit(()-> {
                                OptiLoggerStreamsContainer.debug(eventJsons.size() + " Events about to be removed");
                                optistreamPersistanceAdapter.removeEvents(eventsBulk.getLastId());
                                dispatchRequestWaitsForResponse = false;
                                dispatchBulkIfExists();
                            });
                        })
                        .send();
            } catch (Exception e) {
                dispatchRequestWaitsForResponse = false;
                OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                        e.getMessage());
            }
        } else {
            // Schedule another dispatch all if we are done dispatching
            dispatchRequestWaitsForResponse = false;
            scheduleTheNextDispatch();
        }
    }

    private void scheduleTheNextDispatch(){
        try {
            this.timerDispatchFuture = this.singleThreadScheduledExecutor.schedule(this::dispatchBulkIfExists,
                    Constants.DISPATCH_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            OptiLoggerStreamsContainer.error("Failed to schedule another dispatch - %s",
                    e.getMessage());
        }
    }
    @Override
    public void activityStopped() {
        // Stop the scheduled dispatch if exists
        if (timerDispatchFuture != null) {
            timerDispatchFuture.cancel(false);
        }
        singleThreadScheduledExecutor.submit(this::dispatchBulkIfExists);
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
