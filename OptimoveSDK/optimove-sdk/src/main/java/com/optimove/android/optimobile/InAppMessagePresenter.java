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


class InAppMessagePresenter implements AppStateWatcher.AppStateChangedListener {

    private static final String TAG = InAppMessagePresenter.class.getName();

    private final List<InAppMessage> messageQueue = new ArrayList<>();
    private final Context context;

    @NonNull
    private OptimoveConfig.InAppDisplayMode displayMode;

    @Nullable
    private Activity currentActivity;
    @Nullable
    private InAppMessageView view;

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

        if (null != view) {
            view.showMessage(currentMessage);
            return;
        }

        if (null == currentActivity) {
            return;
        }
        view = new InAppMessageView(this, currentMessage, currentActivity);
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
}
