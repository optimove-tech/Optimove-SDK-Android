package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class OverlayMessagingManager {

    private static final int SESSION_SLOT_CAPACITY = 1;
    private static final int IMMEDIATE_SLOT_CAPACITY = 1;

    enum MessageType {
        SESSION,
        IMMEDIATE
    }

    enum InterceptorOutcome {
        SHOW,
        DISCARD,
        HOLD,
        TIMEOUT;

        String toEventValue() {
            switch (this) {
                case SHOW:    return "shown";
                case DISCARD: return "discarded";
                case HOLD:    return "held";
                case TIMEOUT: return "timeout";
                default:      throw new IllegalStateException("Unhandled outcome: " + this);
            }
        }
    }

    private final Context context;
    private final Queue<OverlayMessagingMessage> displayQueue = new ArrayDeque<>();
    private final ScheduledExecutorService interceptorExecutor = Executors.newSingleThreadScheduledExecutor();

    @Nullable
    private OptimoveOverlayMessaging.OverlayMessagingInterceptor interceptor;

    private int sessionSlotCount = 0;
    private int immediateSlotCount = 0;

    OverlayMessagingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @UiThread
    void setInterceptor(@Nullable OptimoveOverlayMessaging.OverlayMessagingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @UiThread
    void onTriggerReceived(MessageType type) {
        switch (type) {
            case SESSION:
                if (sessionSlotCount >= SESSION_SLOT_CAPACITY) return;
                sessionSlotCount++;
                loadMessage(type);
                break;
            case IMMEDIATE:
                if (immediateSlotCount >= IMMEDIATE_SLOT_CAPACITY) return;
                immediateSlotCount++;
                loadMessage(type);
                break;
        }
    }

    @UiThread
    void onSlotCleared(MessageType type) {
        switch (type) {
            case SESSION:
                sessionSlotCount = Math.max(0, sessionSlotCount - 1);
                break;
            case IMMEDIATE:
                immediateSlotCount = Math.max(0, immediateSlotCount - 1);
                break;
        }
    }

    private void loadMessage(MessageType type) {
        Optimobile.executorService.submit(() -> {
            OverlayMessagingMessage message = OverlayMessagingRequestService.readOverlayMessage(context, type);
            Optimobile.handler.post(() -> onMessageLoaded(type, message));
        });
    }

    @UiThread
    private void onMessageLoaded(MessageType type, @Nullable OverlayMessagingMessage message) {
        if (message == null) {
            onSlotCleared(type);
            return;
        }

        processMessage(type, message);
    }

    @UiThread
    private void processMessage(MessageType type, OverlayMessagingMessage message) {
        if (interceptor == null) {
            displayQueue.add(message);
            // TODO: notify OverlayMessagingView to display next message
            return;
        }

        AtomicBoolean processed = new AtomicBoolean(false);

        OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback callback = new OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback() {
            @Override
            public void show() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(type, message, InterceptorOutcome.SHOW));
            }

            @Override
            public void discard() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(type, message, InterceptorOutcome.DISCARD));
            }

            @Override
            public void hold() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(type, message, InterceptorOutcome.HOLD));
            }
        };

        interceptorExecutor.schedule(() -> {
            if (!processed.compareAndSet(false, true)) return;
            Optimobile.handler.post(() -> handleInterceptorOutcome(type, message, InterceptorOutcome.TIMEOUT));
        }, interceptor.getTimeoutMs(), TimeUnit.MILLISECONDS);

        interceptor.onMessageLoaded(message, callback);
    }

    @UiThread
    private void handleInterceptorOutcome(
            @NonNull MessageType type,
            @NonNull OverlayMessagingMessage message,
            @NonNull InterceptorOutcome outcome) {
        switch (outcome) {
            case SHOW:
                displayQueue.add(message);
                // TODO: notify OverlayMessagingView to display next message
                trackInterceptedEvent(message.id, outcome);
                break;
            case DISCARD:
                onSlotCleared(type);
                trackInterceptedEvent(message.id, outcome);
                break;
            case HOLD:
                onSlotCleared(type);
                trackInterceptedEvent(message.id, outcome);
                break;
            case TIMEOUT:
                onSlotCleared(type);
                trackInterceptedEvent(message.id, outcome);
                break;
        }
    }

    private void trackInterceptedEvent(long messageId, @NonNull InterceptorOutcome outcome) {
        try {
            JSONObject props = new JSONObject();
            props.put("outcome", outcome.toEventValue());
            props.put("id", messageId);
            Optimobile.trackEventImmediately(context, AnalyticsContract.EVENT_TYPE_OM_INTERCEPTED, props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
