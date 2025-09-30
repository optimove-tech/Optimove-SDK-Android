package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.optimove.android.OptimoveConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


class InAppMessagePresenter implements AppStateWatcher.AppStateChangedListener {

    private static final String TAG = InAppMessagePresenter.class.getName();
    private final List<InAppMessage> messageQueue = new ArrayList<>();
    private final Context context;
    private final ScheduledExecutorService interceptorExecutor = Executors.newSingleThreadScheduledExecutor();

    @NonNull
    private OptimoveConfig.InAppDisplayMode displayMode;

    @Nullable
    private InAppMessageInterceptor messageInterceptor = null;

    @Nullable
    private Activity currentActivity;
    @Nullable
    private InAppMessageView view;

    private boolean interceptionInProgress = false;

    InAppMessagePresenter(Context context, @NonNull OptimoveConfig.InAppDisplayMode defaultDisplayMode) {
        this.context = context.getApplicationContext();
        this.displayMode = defaultDisplayMode;
        OptimobileInitProvider.getAppStateWatcher().registerListener(this);
    }

    @Override
    public void appEnteredForeground() {
        if (!OptimoveInApp.getInstance().isInAppEnabled()) {
            return;
        }

        InAppMessageService.readAndPresentMessages(context, true, null);
    }

    @Override
    public void activityAvailable(@NonNull Activity activity) {
        if (!OptimoveInApp.getInstance().isInAppEnabled()) {
            return;
        }

        if (currentActivity != activity) {
            disposeView();
            currentActivity = activity;
        }

        int tickleId = -1;

        Intent i = currentActivity.getIntent();
        if (null != i) {
            tickleId = i.getIntExtra(PushBroadcastReceiver.EXTRAS_KEY_TICKLE_ID, -1);
        }

        if (-1 != tickleId) {
            InAppMessageService.readAndPresentMessages(context, false, tickleId);
        }

        presentMessageToClient();
    }

    @Override
    public void activityUnavailable(@NonNull Activity activity) {
        if (!OptimoveInApp.getInstance().isInAppEnabled()) {
            return;
        }

        if (activity != currentActivity) {
            return;
        }

        disposeView();
        currentActivity = null;
    }

    @Override
    public void appEnteredBackground() {
        // noop
    }

    void setDisplayMode(@NonNull OptimoveConfig.InAppDisplayMode mode) {
        boolean resumed;

        synchronized (this) {
            resumed = displayMode != mode && mode != OptimoveConfig.InAppDisplayMode.PAUSED;
            displayMode = mode;
        }

        if (resumed) {
            presentMessageToClient();
        }
    }

    @NonNull
    synchronized OptimoveConfig.InAppDisplayMode getDisplayMode() {
        return displayMode;
    }

    @AnyThread
    synchronized void presentMessages(List<InAppMessage> itemsToPresent, List<Integer> tickleIds) {
        Optimobile.handler.post(() -> presentMessagesOnUiThread(itemsToPresent, tickleIds));
    }

    @UiThread
    void cancelCurrentPresentationQueue() {
        messageQueue.clear();
        disposeView();
    }

    @UiThread
    void messageClosed() {
        if (messageQueue.isEmpty()) {
            disposeView();
            return;
        }

        messageQueue.remove(0);

        presentMessageToClient();
    }

    @UiThread
    private void disposeView() {
        if (view == null) {
            return;
        }

        view.dispose();
        view = null;
    }

    @UiThread
    private void presentMessageToClient() {
        InAppMessage currentMessage = getCurrentMessage();

        if (null == currentMessage || getDisplayMode() == OptimoveConfig.InAppDisplayMode.PAUSED) {
            disposeView();
            return;
        }

        // If a view already exists, prefer reusing it.
        if (null != view) {
            if (interceptionInProgress) {
                return;
            }

            if (messageInterceptor != null) {
                interceptionInProgress = true;
                applyMessageInterception(currentMessage);
                return;
            }
       
            view.showMessage(currentMessage);
            return;
        }

        if (null == currentActivity) {
            return;
        }

        if (messageInterceptor != null) {
            if (interceptionInProgress) {
                return;
            }
            interceptionInProgress = true;
            applyMessageInterception(currentMessage);
            return;
        }

        showMessageDirectly(currentMessage);
    }

    @UiThread
    private void presentMessagesOnUiThread(List<InAppMessage> itemsToPresent, List<Integer> tickleIds) {
        if (itemsToPresent.isEmpty()) {
            return;
        }

        addMessagesToQueue(itemsToPresent);
        moveTicklesToFront(tickleIds);

        presentMessageToClient();
    }

    @UiThread
    private void moveTicklesToFront(List<Integer> tickleIds) {
        if (tickleIds == null || tickleIds.isEmpty()) {
            return;
        }

        for (Integer tickleId : tickleIds) {
            for (int i = 0; i < messageQueue.size(); i++) {
                InAppMessage next = messageQueue.get(i);
                if (tickleId == next.getInAppId()) {
                    messageQueue.remove(i);
                    messageQueue.add(0, next);

                    break;
                }
            }
        }
    }

    @UiThread
    private void addMessagesToQueue(List<InAppMessage> itemsToPresent) {
        for (InAppMessage messageToAppend : itemsToPresent) {
            boolean exists = false;
            for (InAppMessage messageFromQueue : messageQueue) {
                if (messageToAppend.getInAppId() == messageFromQueue.getInAppId()) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                messageQueue.add(messageToAppend);
            }
        }
    }

    @Nullable
    @UiThread
    private InAppMessage getCurrentMessage() {
        if (messageQueue.isEmpty()) {
            return null;
        }

        return messageQueue.get(0);
    }

    /**
     * Sets the message interceptor for conditional message display
     */
    void setInAppMessageInterceptor(@Nullable InAppMessageInterceptor interceptor) {
        this.messageInterceptor = interceptor;
    }

    /**
     * Calls the message interceptor which will also determine if the message should be shown
     */
    @UiThread
    private void applyMessageInterception(@NonNull InAppMessage message) {
        final Activity activity = currentActivity;
        if (activity == null || messageInterceptor == null) {
            return;
        }

        final AtomicBoolean processed = new AtomicBoolean(false);

        InAppMessageInterceptorCallback callback = new InAppMessageInterceptorCallback() {
            @Override
            public void show() {
                if (!processed.compareAndSet(false, true)) {
                    return;
                }
                Optimobile.handler.post(() -> {
                    interceptionInProgress = false;
                    if (view != null) {
                        view.showMessage(message);
                    } else {
                        showMessageDirectly(message);
                    }
                });
            }

            @Override
            public void suppress() {
                if (!processed.compareAndSet(false, true)) {
                    return;
                }
                Optimobile.handler.post(() -> {
                    interceptionInProgress = false;
                    InAppMessageService.handleMessageSuppressed(context, message);
                    messageQueue.remove(message);
                    presentMessageToClient();
                });
            }
        };

        long timeoutMs = messageInterceptor.getTimeoutMs();

        interceptorExecutor.schedule(() -> {
            if (processed.get()) {
                return;
            }
            callback.suppress();
        }, timeoutMs, TimeUnit.MILLISECONDS);

        try {
            messageInterceptor.processMessage(message.getData(), callback);
        } catch (Exception e) {
            Log.e(TAG, "Error in message interceptor", e);
            callback.suppress();
        }
    }

    @UiThread
    private void showMessageDirectly(@NonNull InAppMessage message) {
        if (currentActivity == null) {
            return;
        }
        view = new InAppMessageView(this, message, currentActivity);
    }
}
