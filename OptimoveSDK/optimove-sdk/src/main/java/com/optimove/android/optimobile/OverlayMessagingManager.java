package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Orchestrates the overlay lifecycle: fetch -> interceptor -> queue -> display.
 * Scheduled (session) fetches are started by the session manager, which runs one at a time; each ends with exactly
 * one {@code release()} to it. Immediate fetches have their own slot here. Both feed the same presentation queue.
 */
class OverlayMessagingManager implements AppStateWatcher.AppStateChangedListener {

    private static final String TAG = "OverlayMessagingManager";
    private static final int IMMEDIATE_SLOT_CAPACITY = 1;

    enum InterceptorOutcome {
        SHOW, DISCARD, DEFER, TIMEOUT;

        String toEventValue() {
            switch (this) {
                case SHOW:
                    return "show";
                case DISCARD:
                    return "discard";
                case DEFER:
                    return "defer";
                case TIMEOUT:
                    return "timeout";
                default:
                    throw new IllegalStateException("Unhandled outcome: " + this);
            }
        }
    }

    private final Context context;
    private final OverlayMessagingSessionManager sessionManager;
    private final Queue<OverlayMessagingMessage> displayQueue = new ArrayDeque<>();
    private final ScheduledExecutorService interceptorExecutor = Executors.newSingleThreadScheduledExecutor();

    @Nullable
    private OptimoveOverlayMessaging.OverlayMessagingInterceptor interceptor;
    private final OverlayActionDispatcher actionDispatcher = new OverlayActionDispatcher();
    @Nullable
    private OverlayMessagingClosedListener closedListener;
    @Nullable
    private OverlayMessagingView currentView;
    @Nullable
    private Activity currentActivity;

    private int immediateSlotCount = 0;
    private boolean hidden = false;
    // Prevents re-processing the same message when triggers arrive before backend events propagate
    private final Set<Long> seenMessageIds = new HashSet<>();

    OverlayMessagingManager(Context context, OverlayMessagingSessionManager sessionManager) {
        this.context = context.getApplicationContext();
        this.sessionManager = sessionManager;
        OptimobileInitProvider.getAppStateWatcher().registerListener(this);
    }


    @UiThread
    void setInterceptor(@Nullable OptimoveOverlayMessaging.OverlayMessagingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @UiThread
    void setActionHandler(@Nullable OverlayMessagingActionHandler handler) {
        actionDispatcher.setHandler(handler);
    }

    @UiThread
    void setClosedListener(@Nullable OverlayMessagingClosedListener listener) {
        closedListener = listener;
    }

    //==============================================================================================
    //-- Visibility

    @UiThread
    void setHidden(boolean hidden) {
        if (this.hidden == hidden) {
            return;
        }
        this.hidden = hidden;

        if (hidden) {
            if (currentView != null) {
                currentView.setHidden(true);
            }
            return;
        }

        maybeShowNext();
        if (currentView != null) {
            currentView.setHidden(false);
        }
    }

    /**
     * A scheduled trigger only ever comes from the session manager, with its slot taken for us; an immediate one takes
     * the immediate slot here.
     */
    @UiThread
    void onTriggerReceived(OverlayMessagingMessage.MessageType type) {
        switch (type) {
            case SESSION:
                Optimobile.executorService.submit(() -> {
                    OverlayMessagingRequestService.ScheduledFetchResult result = OverlayMessagingRequestService.readScheduledMessage(context);
                    Optimobile.handler.post(() -> {
                        sessionManager.onFetchResult(result.nextFetchInSeconds);
                        onMessageLoaded(type, result.message);
                    });
                });
                break;
            case IMMEDIATE:
                if (immediateSlotCount >= IMMEDIATE_SLOT_CAPACITY) {
                    return;
                }
                immediateSlotCount++;
                Optimobile.executorService.submit(() -> {
                    OverlayMessagingMessage message = OverlayMessagingRequestService.readOverlayMessage(context, type);
                    Optimobile.handler.post(() -> onMessageLoaded(type, message));
                });
                break;
        }
    }

    /**
     * Frees the slot of a message's type: for a scheduled one, the session manager's, saying how it ended and, if it
     * was displayed, which message, so a later display of it counts again.
     */
    @UiThread
    private void release(OverlayMessagingMessage.MessageType type, OverlayMessagingSessionManager.Outcome outcome,
                         @Nullable Long displayedMessageId) {
        switch (type) {
            case SESSION:
                sessionManager.release(outcome, displayedMessageId);
                break;
            case IMMEDIATE:
                immediateSlotCount = Math.max(0, immediateSlotCount - 1);
                break;
        }
    }

    @UiThread
    private void onMessageLoaded(OverlayMessagingMessage.MessageType type, @Nullable OverlayMessagingMessage message) {
        if (message == null) {
            release(type, OverlayMessagingSessionManager.Outcome.DONE, null);
            return;
        }
        if (seenMessageIds.contains(message.getId())) {
            release(type, OverlayMessagingSessionManager.Outcome.DEFERRED, null);
            return;
        }

        seenMessageIds.add(message.getId());
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
            public void defer() {
                if (!processed.compareAndSet(false, true)) return;
                Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.DEFER));
            }
        };

        interceptorExecutor.schedule(() -> {
            if (!processed.compareAndSet(false, true)) return;
            Optimobile.handler.post(() -> handleInterceptorOutcome(message, InterceptorOutcome.TIMEOUT));
        }, interceptor.getTimeoutMs(), TimeUnit.MILLISECONDS);

        try {
            interceptor.onMessageLoaded(message, callback);
        } catch (RuntimeException e) {
            if (!processed.compareAndSet(false, true)) return;
            // Failing to answer, like a timeout, but not reported: the Intercepted outcomes don't include it
            Optimobile.log(TAG, "Interceptor threw, treating it as a timeout: " + e);
            seenMessageIds.remove(message.getId());
            release(message.getType(), OverlayMessagingSessionManager.Outcome.DEFERRED, null);
        }
    }

    @UiThread
    private void handleInterceptorOutcome(@NonNull OverlayMessagingMessage message, @NonNull InterceptorOutcome outcome) {
        switch (outcome) {
            case SHOW:
                displayQueue.add(message);
                maybeShowNext();
                trackInterceptedEvent(message.getId(), outcome);
                break;
            case DEFER:
            case TIMEOUT:
                seenMessageIds.remove(message.getId());
                trackInterceptedEvent(message.getId(), outcome);
                release(message.getType(), OverlayMessagingSessionManager.Outcome.DEFERRED, null);
                break;
            case DISCARD:
                // The server deletes the message when it gets this event; the session manager's settle delay gives it time
                trackInterceptedEvent(message.getId(), outcome);
                release(message.getType(), OverlayMessagingSessionManager.Outcome.DONE, null);
                break;
        }
    }

    @UiThread
    private void maybeShowNext() {
        if (hidden) {
            return;
        }

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
            public void onMessageClosed(OverlayMessagingMessage closedMessage, OverlayMessagingCloseReason reason) {
                displayQueue.poll();
                release(closedMessage.getType(), OverlayMessagingSessionManager.Outcome.DONE, closedMessage.getId());
                notifyClosed(closedMessage, reason);
                maybeShowNext();
            }

            @Override
            public void onMessageOpened(OverlayMessagingMessage openedMessage) {
                if (openedMessage.getType() == OverlayMessagingMessage.MessageType.SESSION) {
                    sessionManager.onDisplayed(openedMessage.getId());
                }
            }

            @Override
            public void onEvents(OverlayMessagingMessage message, List<OverlayMessagingRendererEvent> events) {
                trackOverlayMessagingRendererEvents(message.getId(), events);
            }

            @Override
            public void onDismissed(OverlayMessagingMessage message) {
                trackDismissedEvent(message.getId());
            }

            @Override
            public void onViewError(OverlayMessagingMessage failedMessage) {
                currentView.dispose();
                currentView = null;
                // Immediate messages are short-lived. In case of an error we dont want them to stay on queue and surface later
                displayQueue.poll();
                // Still on the server: fetching again at once would return it again
                release(failedMessage.getType(), OverlayMessagingSessionManager.Outcome.DEFERRED, failedMessage.getId());
                notifyClosed(failedMessage, OverlayMessagingCloseReason.ERROR);
                maybeShowNext();
            }

            @Override
            public void onLinkAction(OverlayMessagingMessage message, JSONObject data) {
                if (currentActivity == null) return;
                actionDispatcher.dispatch(OverlayActionDispatcher.ActionType.LINK_ACTION, currentActivity, message, data);
            }
        });
    }

    private void notifyClosed(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingCloseReason reason) {
        if (closedListener == null) return;
        try {
            closedListener.onClosed(message, new OverlayMessagingClosedInfo(reason));
        } catch (Exception e) {
            Log.e(TAG, "Overlay closed listener threw", e);
        }
    }

    private void trackOverlayMessagingRendererEvents(long messageId, List<OverlayMessagingRendererEvent> events) {
        for (OverlayMessagingRendererEvent event : events) {
            try {
                JSONObject data = event.data != null ? event.data : new JSONObject();
                data.put("id", messageId);
                Optimobile.trackEvent(context, event.type, data, System.currentTimeMillis(), event.immediateFlush);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
