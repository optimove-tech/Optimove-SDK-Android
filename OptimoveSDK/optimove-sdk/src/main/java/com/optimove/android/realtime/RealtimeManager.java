package com.optimove.android.realtime;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.android.AuthManager;
import com.optimove.android.main.events.core_events.SetEmailEvent;
import com.optimove.android.main.events.core_events.SetUserIdEvent;
import com.optimove.android.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.android.main.tools.networking.HttpClient;
import com.optimove.android.main.tools.networking.HttpClient.HttpStatusException;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.android.optistream.OptistreamEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.optimove.android.realtime.RealtimeConstants.FAILED_SET_EMAIL_EVENT_KEY;
import static com.optimove.android.realtime.RealtimeConstants.FAILED_SET_USER_EVENT_KEY;
import static com.optimove.android.realtime.RealtimeConstants.REALTIME_SP_NAME;

public final class RealtimeManager {

    @NonNull
    private final SharedPreferences realtimePreferences;
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final RealtimeConfigs realtimeConfigs;

    private final Gson realtimeGson;

    @Nullable
    private final AuthManager authManager;

    public RealtimeManager(@NonNull HttpClient httpClient, @NonNull RealtimeConfigs realtimeConfigs,
        @NonNull Context context, @Nullable AuthManager authManager) {
        this.httpClient = httpClient;
        this.realtimePreferences = context.getSharedPreferences(REALTIME_SP_NAME, Context.MODE_PRIVATE);
        this.realtimeConfigs = realtimeConfigs;
        this.realtimeGson = new Gson();
        this.authManager = authManager;
    }

    public void reportEvents(List<OptistreamEvent> optistreamEvents) {
        // if there was some failed important event, add them before this one
        List<OptistreamEvent> optistreamEventsToDispatch = new ArrayList<>();
        boolean setUserEventFound = false;
        boolean setEmailEventFound = false;
        for (OptistreamEvent optistreamEvent : optistreamEvents) {
            if (optistreamEvent.getName()
                    .equals(SetUserIdEvent.EVENT_NAME)) {
                setUserEventFound = true;
            } else if (optistreamEvent.getName()
                    .equals(SetEmailEvent.EVENT_NAME)) {
                setEmailEventFound = true;
            }
        }

        if (!setUserEventFound) {
            String serializedSetUserIdEvent = realtimePreferences.getString(FAILED_SET_USER_EVENT_KEY, null);
            if (serializedSetUserIdEvent != null) {
                // add set user id event
                optistreamEventsToDispatch.add(realtimeGson.fromJson(serializedSetUserIdEvent, OptistreamEvent.class));
            }
        }
        if (!setEmailEventFound) {
            String serializedSetEmailEvent = realtimePreferences.getString(FAILED_SET_EMAIL_EVENT_KEY, null);
            if (serializedSetEmailEvent != null) {
                // add set email id event
                optistreamEventsToDispatch.add(realtimeGson.fromJson(serializedSetEmailEvent, OptistreamEvent.class));
            }
        }
        optistreamEventsToDispatch.addAll(optistreamEvents);
        dispatchEventsGrouped(optistreamEventsToDispatch);
    }

    private static String userKey(@Nullable OptistreamEvent e) {
        if (e == null) {
            return "";
        }
        String uid = e.getUserId();
        if (uid == null) {
            return "";
        }
        String t = uid.trim();
        return t.isEmpty() ? "" : t;
    }

    private List<List<OptistreamEvent>> groupByUserId(List<OptistreamEvent> events) {
        Map<String, List<OptistreamEvent>> map = new LinkedHashMap<>();
        for (OptistreamEvent ev : events) {
            String key = userKey(ev);
            List<OptistreamEvent> group = map.get(key);
            if (group == null) {
                group = new ArrayList<>();
                map.put(key, group);
            }
            group.add(ev);
        }
        return new ArrayList<>(map.values());
    }

    private void dispatchEventsGrouped(List<OptistreamEvent> allEvents) {
        List<List<OptistreamEvent>> groups = groupByUserId(allEvents);
        dispatchGroupAtIndex(groups, 0);
    }

    private void dispatchGroupAtIndex(List<List<OptistreamEvent>> groups, int index) {
        if (index >= groups.size()) {
            realtimePreferences.edit()
                    .remove(FAILED_SET_USER_EVENT_KEY)
                    .remove(FAILED_SET_EMAIL_EVENT_KEY)
                    .apply();
            return;
        }
        List<OptistreamEvent> group = groups.get(index);
        String key = group.isEmpty() ? "" : userKey(group.get(0));

        if (authManager != null && !key.isEmpty()) {
            authManager.getToken(key, (token, error) -> {
                if (error != null || token == null) {
                    dispatchingFailed(error != null ? error : new Exception("null token"), group);
                    return;
                }
                httpClient.postJson(realtimeConfigs.getRealtimeGateway(), realtimeGson.toJson(group))
                        .userJwt(token)
                        .successListener(jsonResponse -> dispatchGroupAtIndex(groups, index + 1))
                        .errorListener(e -> onRealtimeRequestFailed(e, groups, index, group))
                        .destination("%s", RealtimeConstants.REPORT_EVENT_REQUEST_ROUTE)
                        .send();
            });
        } else {
            httpClient.postJson(realtimeConfigs.getRealtimeGateway(), realtimeGson.toJson(group))
                .userJwt(null)
                .successListener(jsonResponse -> dispatchGroupAtIndex(groups, index + 1))
                .errorListener(e -> onRealtimeRequestFailed(e, groups, index, group))
                .destination("%s", RealtimeConstants.REPORT_EVENT_REQUEST_ROUTE)
                .send();
        }
    }

    private void onRealtimeRequestFailed(
            @NonNull Exception e,
            @NonNull List<List<OptistreamEvent>> groups,
            int index,
            @NonNull List<OptistreamEvent> group) {
        if (authManager == null && e instanceof HttpStatusException && ((HttpStatusException) e).getCode() == 401) {
            OptiLoggerStreamsContainer.error(
                    "Realtime unauthorized (401) with auth not configured; discarding batch without retry");
            clearFailProtectedPrefsMatchingGroup(group);
            dispatchGroupAtIndex(groups, index + 1);
            return;
        }
        dispatchingFailed(e, group);
    }

    private void clearFailProtectedPrefsMatchingGroup(@NonNull List<OptistreamEvent> group) {
        boolean clearUser = false;
        boolean clearEmail = false;
        for (OptistreamEvent optistreamEvent : group) {
            if (optistreamEvent.getName()
                    .equals(SetUserIdEvent.EVENT_NAME)) {
                clearUser = true;
            }
            if (optistreamEvent.getName()
                    .equals(SetEmailEvent.EVENT_NAME)) {
                clearEmail = true;
            }
        }
        if (!clearUser && !clearEmail) {
            return;
        }
        SharedPreferences.Editor ed = realtimePreferences.edit();
        if (clearUser) {
            ed.remove(FAILED_SET_USER_EVENT_KEY);
        }
        if (clearEmail) {
            ed.remove(FAILED_SET_EMAIL_EVENT_KEY);
        }
        ed.apply();
    }

    private void dispatchingFailed(Exception e, List<OptistreamEvent> optistreamEvents) {
        //add failed to shared prefs (if important)
        OptiLoggerStreamsContainer.error("Events dispatching to RT failed - %s",
                e.getMessage());
        for (OptistreamEvent optistreamEvent : optistreamEvents) {
            if (optistreamEvent.getName()
                    .equals(SetUserIdEvent.EVENT_NAME)) {
                realtimePreferences.edit()
                        .putString(FAILED_SET_USER_EVENT_KEY, realtimeGson.toJson(optistreamEvent))
                        .apply();
            }
            if (optistreamEvent.getName()
                    .equals(SetEmailEvent.EVENT_NAME)) {
                realtimePreferences.edit()
                        .putString(FAILED_SET_EMAIL_EVENT_KEY, realtimeGson.toJson(optistreamEvent))
                        .apply();
            }
        }
    }
}
