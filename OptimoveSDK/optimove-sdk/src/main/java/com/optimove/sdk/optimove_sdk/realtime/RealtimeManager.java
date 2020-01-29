package com.optimove.sdk.optimove_sdk.realtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.DID_FAIL_SET_EMAIL_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.DID_FAIL_SET_USER_ID_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.FIRST_VISIT_TIMESTAMP_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.REALTIME_SP_NAME;

public final class RealtimeManager {

    @NonNull
    private SharedPreferences realtimePreferences;
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private RealtimeConfigs realtimeConfigs;
    @NonNull
    private ExecutorService reportingExecutor;
    @NonNull
    private Map<String, EventConfigs> eventConfigsMap;
    @NonNull
    private UserInfo userInfo;

    private final Semaphore semaphore = new Semaphore(1);


    private boolean initialized = false;

    public RealtimeManager(@NonNull HttpClient httpClient, @NonNull RealtimeConfigs realtimeConfigs,
                           @NonNull Map<String, EventConfigs> eventConfigsMap, @NonNull UserInfo userInfo,
                           @NonNull Context context) {
        this.httpClient = httpClient;
        this.realtimePreferences = context.getSharedPreferences(REALTIME_SP_NAME, Context.MODE_PRIVATE);
        this.realtimeConfigs = realtimeConfigs;
        this.eventConfigsMap = eventConfigsMap;
        this.reportingExecutor = Executors.newSingleThreadExecutor();
        this.userInfo = userInfo;
    }

    private void ensureInitialization(){
        if (!initialized) {
            if (!realtimePreferences.contains(FIRST_VISIT_TIMESTAMP_KEY)) {
                realtimePreferences.edit()
                        .putLong(FIRST_VISIT_TIMESTAMP_KEY, OptiUtils.currentTimeSeconds())
                        .apply();
            }
            initialized = true;
        }
    }

    public void reportEvent(OptimoveEvent event) {
        ensureInitialization();
        if (!event.getName().equals(SetUserIdEvent.EVENT_NAME)) {
            sendSetUserIdEventIfPreviouslyFailed();
        }
        if (!event.getName().equals(SetEmailEvent.EVENT_NAME)) {
            sendEmailEventIfPreviouslyFailed();
        }
        dispatchEvent(event, false);
    }


    private void handleDispatchEventResponse(OptimoveEvent optimoveEvent, RealtimeEventDispatchResponse response) {
        if (response.isSuccess()) {
            unmarkImportantEventFromRetry(optimoveEvent);
        } else {
            markImportantEventForRetry(optimoveEvent);
        }
    }


    private void markImportantEventForRetry(OptimoveEvent optimoveEvent) {
        SharedPreferences.Editor editor = realtimePreferences.edit();

        if (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME)) {
            OptiLogger.realtimeSetUserIdIsMarkedForRetry();
            editor.putBoolean(DID_FAIL_SET_USER_ID_KEY,
                    true);
        }
        if (optimoveEvent.getName()
                .equals(SetEmailEvent.EVENT_NAME)) {
            OptiLogger.realtimeSetEmailIsMarkedForRetry();
            editor.putBoolean(DID_FAIL_SET_EMAIL_KEY,
                    true);
        }
        editor.apply();
    }
    private void unmarkImportantEventFromRetry(OptimoveEvent event) {
        SharedPreferences.Editor editor = realtimePreferences.edit();

        if (event.getName()
                .equals(SetUserIdEvent.EVENT_NAME)) {
            editor.remove(DID_FAIL_SET_USER_ID_KEY);
        }
        if (event.getName()
                .equals(SetEmailEvent.EVENT_NAME)) {
            editor.remove(DID_FAIL_SET_EMAIL_KEY);
        }
        editor.apply();
    }

    private void dispatchEvent(OptimoveEvent optimoveEvent, boolean shouldBeDecorated) {
        EventConfigs eventConfigs = this.eventConfigsMap.get(optimoveEvent.getName());
        if (shouldBeDecorated) {
            optimoveEvent = new OptimoveEventDecorator(optimoveEvent, eventConfigs);
        }
        DispatchingCommand dispatchingCommand = new DispatchingCommand(optimoveEvent, eventConfigs.getId(),
                this::handleDispatchEventResponse, realtimePreferences.getLong(FIRST_VISIT_TIMESTAMP_KEY, -1));
        reportingExecutor.execute(dispatchingCommand);
    }

    private void sendSetUserIdEventIfPreviouslyFailed() {
        if (realtimePreferences.getBoolean(DID_FAIL_SET_USER_ID_KEY, false)) {
            SetUserIdEvent setUserIdEvent =
                    new SetUserIdEvent(userInfo.getInitialVisitorId(), userInfo.getUserId(), userInfo.getVisitorId());
            dispatchEvent(setUserIdEvent, true);
        }
    }
    private void sendEmailEventIfPreviouslyFailed(){
        if (realtimePreferences.getBoolean(DID_FAIL_SET_EMAIL_KEY, false)) {
            String email = userInfo.getEmail();
            SetEmailEvent setEmailEvent = new SetEmailEvent(email);
            dispatchEvent(setEmailEvent, true);
        }
    }

    private class DispatchingCommand implements Runnable {

        private final OptimoveEvent optimoveEvent;
        private final RealtimeResponseListener responseListener;
        private final int eventId;
        private final Long firstVisitorDate;

        public DispatchingCommand(OptimoveEvent optimoveEvent,
                                  int eventId,
                                  RealtimeResponseListener responseListener,
                                  Long firstVisitorDate) {
            this.optimoveEvent = optimoveEvent;
            this.eventId = eventId;
            this.responseListener = responseListener;
            this.firstVisitorDate = firstVisitorDate;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException exception) {
                OptiLoggerStreamsContainer.error("Realtime event dispatch failed due to %s", exception.getMessage());
            }
            RealtimeEvent realtimeEvent = RealtimeEvent.newInstance(optimoveEvent, eventId, firstVisitorDate);
            RealtimeDispatchEventRequest request =
                    new RealtimeDispatchEventRequest(realtimeConfigs.getRealtimeToken(), userInfo, realtimeEvent);
            JSONObject requestBody;
            try {
                requestBody = request.toJson();
            } catch (JSONException e) {
                OptiLogger.realtimeFailedReportingEvent_WhenSerializingEvent(eventId, e.getMessage());
                responseListener.onResponse(optimoveEvent, new RealtimeEventDispatchResponse());
                return;
            }

            OptiLogger.realtimeIsAboutToReportEvent(eventId, requestBody.toString());

            RealtimeManager.this.httpClient.postJson(realtimeConfigs.getRealtimeGateway(), requestBody)
                    .successListener(jsonResponse -> {
                        OptiLogger.realtimeFinishedReportingEventSuccessfully(eventId);
                        RealtimeEventDispatchResponse response = new RealtimeEventDispatchResponse(false, false);
                        try {
                            response = RealtimeEventDispatchResponse.fromJson(jsonResponse);
                        } catch (JSONException e) {
                            OptiLogger.realtimeFailed_WhenDeserializingReportEventResponse(eventId, e.getMessage());
                        }
                        responseListener.onResponse(optimoveEvent, response);
                        semaphore.release();
                    })
                    .errorListener(error -> {
                        OptiLoggerStreamsContainer.debug("Realtime failed to report event %d due to: %s", eventId, error.getMessage());
                        responseListener.onResponse(optimoveEvent, new RealtimeEventDispatchResponse());
                        semaphore.release();
                    })
                    .destination("%s", RealtimeConstants.REPORT_EVENT_REQUEST_ROUTE)
                    .send();
        }
    }
}
