package com.optimove.android.optistream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.android.AuthManager;
import com.optimove.android.main.common.LifecycleObserver;
import com.optimove.android.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.android.main.tools.networking.HttpClient;
import com.optimove.android.main.tools.networking.HttpClient.HttpStatusException;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final AuthManager authManager;

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
        this(httpClient, lifecycleObserver, optistreamPersistanceAdapter, optitrackConfigs, null);
    }

    public OptistreamHandler(@NonNull HttpClient httpClient,
                             @NonNull LifecycleObserver lifecycleObserver,
                             @NonNull OptistreamPersistanceAdapter optistreamPersistanceAdapter,
                             @NonNull OptitrackConfigs optitrackConfigs,
                             @Nullable AuthManager authManager) {
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.optistreamPersistanceAdapter = optistreamPersistanceAdapter;
        this.optitrackConfigs = optitrackConfigs;
        this.authManager = authManager;
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
        try {
            singleThreadScheduledExecutor.submit(() -> {
                boolean immediateEventFound = false;
                for (OptistreamEvent optistreamEvent : optistreamEvents) {
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

    private void dispatchBulkIfExists() {
        if (dispatchRequestWaitsForResponse) {
            return;
        }
        OptistreamPersistanceAdapter.EventsBulk eventsBulk = optistreamPersistanceAdapter.getFirstEvents(Constants.EVENT_BATCH_LIMIT);
        if (eventsBulk == null) {
            scheduleTheNextDispatch();
            return;
        }
        List<OptistreamPersistanceAdapter.QueuedEvent> queue = eventsBulk.getEvents();
        if (queue != null && !queue.isEmpty()) {
            try {
                dispatchRequestWaitsForResponse = true;
                List<List<OptistreamPersistanceAdapter.QueuedEvent>> groups = groupByCustomer(queue);
                sendCustomerGroups(groups, 0);
            } catch (Throwable e) {
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

    private static String customerKeyFromJson(String json) {
        try {
            JSONObject o = new JSONObject(json);
            if (!o.has("customer") || o.isNull("customer")) {
                return "";
            }
            String c = o.optString("customer", "");
            return c == null ? "" : c.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static List<List<OptistreamPersistanceAdapter.QueuedEvent>> groupByCustomer(
            List<OptistreamPersistanceAdapter.QueuedEvent> events) {
        Map<String, List<OptistreamPersistanceAdapter.QueuedEvent>> map = new LinkedHashMap<>();
        for (OptistreamPersistanceAdapter.QueuedEvent e : events) {
            String key = customerKeyFromJson(e.getEventJson());
            List<OptistreamPersistanceAdapter.QueuedEvent> group = map.get(key);
            if (group == null) {
                group = new ArrayList<>();
                map.put(key, group);
            }
            group.add(e);
        }
        return new ArrayList<>(map.values());
    }

    private void sendCustomerGroups(List<List<OptistreamPersistanceAdapter.QueuedEvent>> groups, int index) {
        if (index >= groups.size()) {
            dispatchRequestWaitsForResponse = false;
            dispatchBulkIfExists();
            return;
        }
        List<OptistreamPersistanceAdapter.QueuedEvent> group = groups.get(index);
        String customerKey = group.isEmpty() ? "" : customerKeyFromJson(group.get(0).getEventJson());

        Runnable postOnExecutor = () -> postGroupJson(group, groups, index, null);

        if (authManager != null && !customerKey.isEmpty()) {
            authManager.getToken(customerKey, (token, error) ->
                    singleThreadScheduledExecutor.submit(() -> {
                        if (error != null || token == null) {
                            OptiLoggerStreamsContainer.error("Optistream auth token failed - %s",
                                    error != null ? error.getMessage() : "null token");
                            dispatchRequestWaitsForResponse = false;
                            scheduleTheNextDispatch();
                            return;
                        }
                        postGroupJson(group, groups, index, token);
                    }));
        } else {
            postOnExecutor.run();
        }
    }

    private void postGroupJson(List<OptistreamPersistanceAdapter.QueuedEvent> group,
                               List<List<OptistreamPersistanceAdapter.QueuedEvent>> allGroups,
                               int index,
                               @Nullable String jwt) {
        try {
            JSONArray jsonArrayToDispatch = new JSONArray();
            for (OptistreamPersistanceAdapter.QueuedEvent qe : group) {
                jsonArrayToDispatch.put(new JSONObject(qe.getEventJson()));
            }
            List<Long> ids = new ArrayList<>(group.size());
            for (OptistreamPersistanceAdapter.QueuedEvent qe : group) {
                ids.add(qe.getRowId());
            }
            httpClient.postJson(optitrackConfigs.getOptitrackEndpoint(), jsonArrayToDispatch.toString())
                    .userJwt(jwt)
                    .errorListener(error -> {
                        boolean authNotConfigured = authManager == null
                                && error instanceof HttpStatusException
                                && ((HttpStatusException) error).getCode() == 401;
                        if (authNotConfigured) {
                            OptiLoggerStreamsContainer.error(
                                    "Optistream unauthorized (401) with auth not configured; dropping %d event(s)",
                                    ids.size());
                            try {
                                singleThreadScheduledExecutor.submit(() -> {
                                    optistreamPersistanceAdapter.removeEventsByIds(ids);
                                    sendCustomerGroups(allGroups, index + 1);
                                });
                            } catch (Throwable throwable) {
                                OptiLoggerStreamsContainer.error("Error while submitting a command - %s",
                                        throwable.getMessage());
                            }
                            return;
                        }
                        OptiLoggerStreamsContainer.error("Events dispatching failed - %s",
                                error.getMessage());
                        dispatchRequestWaitsForResponse = false;
                        scheduleTheNextDispatch();
                    })
                    .successListener(response -> {
                        try {
                            singleThreadScheduledExecutor.submit(() -> {
                                optistreamPersistanceAdapter.removeEventsByIds(ids);
                                sendCustomerGroups(allGroups, index + 1);
                            });
                        } catch (Throwable throwable) {
                            OptiLoggerStreamsContainer.error("Error while submitting a command - %s", throwable.getMessage());
                        }
                    })
                    .send();
        } catch (Throwable e) {
            dispatchRequestWaitsForResponse = false;
            OptiLoggerStreamsContainer.error("Events dispatching failed - %s", e.getMessage());
            scheduleTheNextDispatch();
        }
    }

    private void scheduleTheNextDispatch() {
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
