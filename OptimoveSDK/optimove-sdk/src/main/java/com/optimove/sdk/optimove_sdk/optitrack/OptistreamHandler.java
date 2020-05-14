package com.optimove.sdk.optimove_sdk.optitrack;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
    private OptistreamDbHelper optistreamDbHelper;
    @NonNull
    private OptitrackConfigs optitrackConfigs;
    @NonNull
    private ScheduledExecutorService singleThreadScheduledExecutor;
    @NonNull
    private Gson optistreamGson;


    @Nullable
    private ScheduledFuture timerDispatchFuture;
    private boolean initialized = false;

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
                optistreamDbHelper.insertEvent(optistreamGson.toJson(optistreamEvent));
                if (optistreamEvent.getMetadata().isRealtime() || isNotificationEvent(optistreamEvent)) {
                    immediateEventFound = true;
                }
            }
            if (immediateEventFound) {
                if (timerDispatchFuture != null) {
                    timerDispatchFuture.cancel(false);
                }
                singleThreadScheduledExecutor.submit(this::dispatchBulkIfExists);
            }
        });
    }

    private void dispatchBulkIfExists(){
        OptistreamDbHelper.EventsBulk eventsBulk = optistreamDbHelper.getFirstEvents(Constants.EVENT_BATCH_LIMIT);
        List<String> eventJsons = eventsBulk.getEventJsons();
        if (!eventJsons.isEmpty()) {

            try {
                JSONArray jsonArrayToDispatch = new JSONArray();
                for (String eventJson: eventJsons) {
                    jsonArrayToDispatch.put(new JSONObject(eventJson));
                }
                OptiLoggerStreamsContainer.debug("Dispatching optistream events");

                httpClient.postJsonArray(optitrackConfigs.getOptitrackEndpoint(), jsonArrayToDispatch)
                        .errorListener(error -> {
                            OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                                    error.getMessage());
                            // some error occurred, try again in the next dispatch
                            scheduleTheNextDispatch();
                        })
                        .successListener(response -> {
                            OptiLoggerStreamsContainer.debug("Events were dispatched");
                            singleThreadScheduledExecutor.submit(()-> {
                                optistreamDbHelper.removeEvents(eventsBulk.getLastId());
                                dispatchBulkIfExists();
                            });
                        })
                        .send();
            } catch (Exception e) {
                OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                        e.getMessage());
            }
        } else {
            // Schedule another dispatch all if we are done dispatching
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
