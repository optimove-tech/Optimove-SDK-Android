package com.optimove.android.realtime;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.optimove.android.main.events.core_events.SetEmailEvent;
import com.optimove.android.main.events.core_events.SetUserIdEvent;
import com.optimove.android.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.android.main.tools.networking.HttpClient;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.android.optistream.OptistreamEvent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.optimove.android.realtime.RealtimeConstants.FAILED_SET_EMAIL_EVENT_KEY;
import static com.optimove.android.realtime.RealtimeConstants.FAILED_SET_USER_EVENT_KEY;
import static com.optimove.android.realtime.RealtimeConstants.REALTIME_SP_NAME;

public final class RealtimeManager {

    @NonNull
    private SharedPreferences realtimePreferences;
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private RealtimeConfigs realtimeConfigs;

    private Gson realtimeGson;

    public RealtimeManager(@NonNull HttpClient httpClient, @NonNull RealtimeConfigs realtimeConfigs,
                           @NonNull Context context) {
        this.httpClient = httpClient;
        this.realtimePreferences = context.getSharedPreferences(REALTIME_SP_NAME, Context.MODE_PRIVATE);
        this.realtimeConfigs = realtimeConfigs;
        this.realtimeGson = new Gson();
    }

    public void reportEvents(List<OptistreamEvent> optistreamEvents) {
        // if there was some failed important event, add them before this one
        List<OptistreamEvent> optistreamEventsToDispatch = new ArrayList<>();
        boolean setUserEventFound = false;
        boolean setEmailEventFound = false;
        for (OptistreamEvent optistreamEvent: optistreamEvents) {
            if (optistreamEvent.getName()
                    .equals(SetUserIdEvent.EVENT_NAME)) {
                setUserEventFound = true;
            } else if (optistreamEvent.getName()
                    .equals(SetEmailEvent.EVENT_NAME)){
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
        dispatchEvents(optistreamEventsToDispatch);
    }

    private void dispatchEvents(List<OptistreamEvent> optistreamEvents) {
        httpClient.postData(realtimeConfigs.getRealtimeGateway(), new Gson().toJson(optistreamEvents))
                .successListener(jsonResponse ->
                        realtimePreferences.edit()
                                .remove(FAILED_SET_USER_EVENT_KEY)
                                .remove(FAILED_SET_EMAIL_EVENT_KEY)
                                .apply()
                )
                .errorListener(e -> dispatchingFailed(e, optistreamEvents))
                .destination("%s", RealtimeConstants.REPORT_EVENT_REQUEST_ROUTE)
                .send();
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
                    .equals(SetEmailEvent.EVENT_NAME)){
                realtimePreferences.edit()
                        .putString(FAILED_SET_EMAIL_EVENT_KEY, realtimeGson.toJson(optistreamEvent))
                        .apply();
            }
        }
    }
}
