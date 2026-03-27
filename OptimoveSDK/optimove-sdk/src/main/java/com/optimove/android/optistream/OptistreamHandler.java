package com.optimove.android.optistream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.android.auth.AuthManager;
import com.optimove.android.main.common.LifecycleObserver;
import com.optimove.android.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.android.main.tools.networking.HttpClient;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OptistreamHandler implements LifecycleObserver.ActivityStopped {

    @NonNull
    private final LifecycleObserver lifecycleObserver;
    @NonNull
    private final OptistreamPersistanceAdapter optistreamPersistanceAdapter;
    @NonNull
    private final OptitrackConfigs optitrackConfigs;
    @NonNull
    private final ScheduledExecutorService singleThreadScheduledExecutor;
    @NonNull
    private final Gson optistreamGson;
    @NonNull
    private final OptistreamDispatcher dispatcher;

    @Nullable
    private ScheduledFuture timerDispatchFuture;
    private boolean dispatchRequestWaitsForResponse = false;
    private boolean initialized = false;

    public final static class Constants {
        public static final int EVENT_BATCH_LIMIT = 100;
        public static final int DISPATCH_INTERVAL_IN_SECONDS = 30;
    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamPersistanceAdapter optistreamPersistanceAdapter,
                             @NonNull OptitrackConfigs optitrackConfigs,
                             @Nullable AuthManager authManager) {
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamPersistanceAdapter = optistreamPersistanceAdapter;
        this.optitrackConfigs = optitrackConfigs;
        this.singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.optistreamGson = new Gson();
        this.dispatcher = new OptistreamDispatcher(httpClient, authManager);
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
        try {
            singleThreadScheduledExecutor.submit(() -> {
                boolean immediateEventFound = false;
                for (OptistreamEvent optistreamEvent: optistreamEvents) {
                    optistreamPersistanceAdapter.insertEvent(optistreamGson.toJson(optistreamEvent));
                    if (optistreamEvent.getMetadata().isRealtime()) {
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
        } catch (Throwable throwable) {
            OptiLoggerStreamsContainer.error("Error while submitting a command - %s", throwable.getMessage());
        }
    }

    private void dispatchBulkIfExists(){
        if (dispatchRequestWaitsForResponse) {
            return;
        }
        OptistreamDbHelper.EventsBulk eventsBulk = optistreamPersistanceAdapter.getFirstEvents(Constants.EVENT_BATCH_LIMIT);
        if (eventsBulk == null) {
            scheduleTheNextDispatch();
            return;
        }
        List<String> eventJsons = eventsBulk.getEventJsons();
        if (eventJsons != null && !eventJsons.isEmpty()) {
            try {
                List<JSONObject> parsedEvents = new ArrayList<>();
                for (String eventJson : eventJsons) {
                    parsedEvents.add(new JSONObject(eventJson));
                }
                dispatchRequestWaitsForResponse = true;

                dispatcher.sendBatch(
                        parsedEvents,
                        optitrackConfigs.getOptitrackEndpoint(),
                        null,
                        (groupEvents, success, error) -> {
                            if (!success) {
                                OptiLoggerStreamsContainer.error("Events dispatch group failed - %s",
                                        error != null ? error.getMessage() : "unknown");
                            }
                        },
                        () -> {
                            try {
                                singleThreadScheduledExecutor.submit(() -> {
                                    optistreamPersistanceAdapter.removeEvents(eventsBulk.getLastId());
                                    dispatchRequestWaitsForResponse = false;
                                    dispatchBulkIfExists();
                                });
                            } catch (Throwable throwable) {
                                dispatchRequestWaitsForResponse = false;
                                OptiLoggerStreamsContainer.error("Error while submitting a command - %s", throwable.getMessage());
                            }
                        }
                );
            } catch (Throwable e) {
                dispatchRequestWaitsForResponse = false;
                OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                        e.getMessage());
            }
        } else {
            dispatchRequestWaitsForResponse = false;
            scheduleTheNextDispatch();
        }
    }

    private void scheduleTheNextDispatch(){
        try {
            this.timerDispatchFuture = this.singleThreadScheduledExecutor.schedule(this::dispatchBulkIfExists,
                    Constants.DISPATCH_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Throwable e) {
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
        try {
            singleThreadScheduledExecutor.submit(this::dispatchBulkIfExists);
        } catch (Throwable throwable) {
            OptiLoggerStreamsContainer.error("Error while submitting a dispatch command - %s", throwable.getMessage());
        }
    }

}
