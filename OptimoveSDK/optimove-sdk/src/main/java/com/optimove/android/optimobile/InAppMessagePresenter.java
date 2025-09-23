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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class InAppMessagePresenter implements AppStateWatcher.AppStateChangedListener {

    private static final String TAG = InAppMessagePresenter.class.getName();
    private static final long FILTER_TIMEOUT_MS = 5000; // 5 seconds

    private final List<InAppMessage> messageQueue = new ArrayList<>();
    private final Context context;
    private final ScheduledExecutorService filterExecutor = Executors.newSingleThreadScheduledExecutor();

    @NonNull
    private OptimoveConfig.InAppDisplayMode displayMode;
    
    @Nullable
    private InAppMessageDisplayFilter messageDisplayFilter = null;

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
        
        // Check if a filter is set and apply it
        if (messageDisplayFilter != null) {
            applyMessageFilter(currentMessage);
        } else {
            // No filter set, show the message directly
            showMessageDirectly(currentMessage);
        }
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
     * Sets the message display filter for conditional message display.
     */
    void setInAppMessageDisplayFilter(@Nullable InAppMessageDisplayFilter filter) {
        this.messageDisplayFilter = filter;
    }
    
    /**
     * Applies the message filter to determine if the message should be shown.
     */
    @UiThread
    private void applyMessageFilter(@NonNull InAppMessage message) {
        final Activity activity = currentActivity; // Capture current activity
        if (activity == null) {
            return;
        }
        
        // Use atomic boolean to ensure callback is only processed once
        final AtomicBoolean callbackProcessed = new AtomicBoolean(false);
        
        // Create the callback that will handle the filter result
        InAppMessageFilterCallback callback = new InAppMessageFilterCallback() {
            @Override
            public void onFilterResult(@NonNull InAppMessageDisplayFilter.FilterResult result) {
                if (!callbackProcessed.compareAndSet(false, true)) {
                    // Callback already processed, ignore
                    return;
                }
                
                // Switch back to UI thread to handle the result
                Optimobile.handler.post(() -> {
                    if (result == InAppMessageDisplayFilter.FilterResult.SHOW) {
                        showMessageDirectly(message);
                    } else {
                        // Message suppressed, move to next message
                        messageQueue.remove(0);
                        presentMessageToClient();
                    }
                });
            }
        };
        
        // Execute filter on background thread with timeout
        filterExecutor.execute(() -> {
            try {
                messageDisplayFilter.shouldDisplayMessage((InAppMessageInfo) message, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error in message display filter", e);
                // On error, suppress the message
                callback.onFilterResult(InAppMessageDisplayFilter.FilterResult.SUPPRESS);
            }
        });
        
        // Set up timeout to prevent indefinite waiting
        filterExecutor.schedule(() -> {
            if (callbackProcessed.compareAndSet(false, true)) {
                Log.w(TAG, "Message display filter timed out, suppressing message");
                // Timeout reached, suppress the message
                Optimobile.handler.post(() -> {
                    messageQueue.remove(0);
                    presentMessageToClient();
                });
            }
        }, FILTER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Shows the message directly without filtering.
     */
    @UiThread
    private void showMessageDirectly(@NonNull InAppMessage message) {
        if (currentActivity == null) {
            return;
        }
        view = new InAppMessageView(this, message, currentActivity);
    }
    
    /**
     * Cleans up resources used by this presenter.
     * This method should be called when the presenter is no longer needed to prevent memory leaks.
     */
    void cleanup() {
        // Unregister from AppStateWatcher to prevent memory leaks
        OptimobileInitProvider.getAppStateWatcher().unregisterListener(this);
        
        // Shutdown the filter executor to prevent thread leaks
        if (filterExecutor != null && !filterExecutor.isShutdown()) {
            filterExecutor.shutdown();
            try {
                // Wait a bit for existing tasks to terminate
                if (!filterExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    Log.w(TAG, "Filter executor did not terminate gracefully, forcing shutdown");
                    filterExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for filter executor shutdown");
                filterExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Clear any remaining messages and dispose view
        messageQueue.clear();
        disposeView();
        
        Log.d(TAG, "InAppMessagePresenter cleanup completed");
    }
}
