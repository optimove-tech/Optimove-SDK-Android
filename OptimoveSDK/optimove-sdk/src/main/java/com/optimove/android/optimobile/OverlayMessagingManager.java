package com.optimove.android.optimobile;

import android.app.Activity;
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

class OverlayMessagingManager implements AppStateWatcher.AppStateChangedListener {

    private static final int SESSION_SLOT_CAPACITY = 1;
    private static final int IMMEDIATE_SLOT_CAPACITY = 1;

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
    @Nullable
    private OverlayMessagingView currentView;
    @Nullable
    private Activity currentActivity;

    private int sessionSlotCount = 0;
    private int immediateSlotCount = 0;

    OverlayMessagingManager(Context context) {
        this.context = context.getApplicationContext();
        OptimobileInitProvider.getAppStateWatcher().registerListener(this);
    }


    @UiThread
    void setInterceptor(@Nullable OptimoveOverlayMessaging.OverlayMessagingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @UiThread
    void onTriggerReceived(OverlayMessagingMessage.MessageType type) {
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
    void onSlotCleared(OverlayMessagingMessage.MessageType type) {
        switch (type) {
            case SESSION:
                sessionSlotCount = Math.max(0, sessionSlotCount - 1);
                break;
            case IMMEDIATE:
                immediateSlotCount = Math.max(0, immediateSlotCount - 1);
                break;
        }
    }

    private void loadMessage(OverlayMessagingMessage.MessageType type) {
        Optimobile.executorService.submit(() -> {
            OverlayMessagingMessage message = OverlayMessagingRequestService.readOverlayMessage(context, type);
            Optimobile.handler.post(() -> onMessageLoaded(type, message));
        });
    }

    @UiThread
    private void onMessageLoaded(OverlayMessagingMessage.MessageType type, @Nullable OverlayMessagingMessage message) {
        if (message == null) {
            onSlotCleared(type);
            return;
        }

        processMessage(message);
    }

    @UiThread
    private void processMessage(OverlayMessagingMessage message) {
        if (interceptor == null) {
            displayQueue.add(message);
            maybeShowNext();
            return;
        }

        AtomicBoolean processed = new AtomicBoolean(false);

        OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback callback = new OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback() {
            @Override
            public void show() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.SHOW));
            }

            @Override
            public void discard() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.DISCARD));
            }

            @Override
            public void hold() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.HOLD));
            }
        };

        interceptorExecutor.schedule(() -> {
            if (!processed.compareAndSet(false, true)) return;
            Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.TIMEOUT));
        }, interceptor.getTimeoutMs(), TimeUnit.MILLISECONDS);

        interceptor.onMessageLoaded(message, callback);
    }

    @UiThread
    private void handleInterceptorOutcome(
            @NonNull OverlayMessagingMessage message,
            @NonNull InterceptorOutcome outcome) {
        switch (outcome) {
            case SHOW:
                displayQueue.add(message);
                maybeShowNext();
                trackInterceptedEvent(message.getId(), outcome);
                break;
            case DISCARD:
                onSlotCleared(message.getType());
                trackInterceptedEvent(message.getId(), outcome);
                break;
            case HOLD:
                onSlotCleared(message.getType());
                trackInterceptedEvent(message.getId(), outcome);
                break;
            case TIMEOUT:
                onSlotCleared(message.getType());
                trackInterceptedEvent(message.getId(), outcome);
                break;
        }
    }

    @UiThread
    private void maybeShowNext() {
        OverlayMessagingMessage next = displayQueue.peek();

        if (next == null) {
            if (currentView != null) {
                currentView.dispose();
                currentView = null;
            }
            return;
        }

        if (currentView != null) {
            currentView.showMessage(next);
            return;
        }

        if (currentActivity == null) {
            return;
        }

        String iarUrl;
        try {
            iarUrl = Optimobile.urlForService(UrlBuilder.Service.IAR, "");
        } catch (Optimobile.PartialInitialisationException e) {
            return;
        }

        currentView = new OverlayMessagingView(next, currentActivity, iarUrl, new OverlayMessagingView.Listener() {
            @Override
            public void onMessageClosed(OverlayMessagingMessage closedMessage) {
                displayQueue.poll();
                onSlotCleared(closedMessage.getType());
                maybeShowNext();
            }

            @Override
            public void onClicked(OverlayMessagingMessage message, JSONObject props) {
                trackClickedEvent(message.getId(), props);
            }

            @Override
            public void onDismissed(OverlayMessagingMessage message) {
                trackDismissedEvent(message.getId());
            }

            @Override
            public void onViewError(OverlayMessagingMessage failedMessage) {
                currentView.dispose();
                currentView = null;
            }
        });
    }

    private void trackClickedEvent(long messageId, JSONObject props) {
        try {
            props.put("id", messageId);
            Optimobile.trackEventImmediately(context, AnalyticsContract.EVENT_TYPE_OM_CLICKED, props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void trackDismissedEvent(long messageId) {
        try {
            JSONObject props = new JSONObject();
            props.put("id", messageId);
            Optimobile.trackEventImmediately(context, AnalyticsContract.EVENT_TYPE_OM_DISMISSED, props);
        } catch (JSONException e) {
            e.printStackTrace();
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

    //==============================================================================================
    //-- AppStateChangedListener

    @Override
    @UiThread
    public void activityAvailable(@NonNull Activity activity) {
        if (currentActivity != activity) {
            if (currentView != null) {
                currentView.dispose();
                currentView = null;
            }
            currentActivity = activity;
        }
        maybeShowNext();
    }

    @Override
    @UiThread
    public void activityUnavailable(@NonNull Activity activity) {
        if (activity != currentActivity) {
            return;
        }
        currentActivity = null;
        if (currentView != null) {
            currentView.dispose();
            currentView = null;
        }
    }

    @Override
    public void appEnteredForeground() { /* noop */ }

    @Override
    public void appEnteredBackground() { /* noop */ }

}
