package com.optimove.android.optimobile;

import android.app.Application;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.optimove.android.OptimoveConfig;

public class OptimoveOverlayMessaging {

    private static OptimoveOverlayMessaging shared;

    private final OverlayMessagingSessionManager sessionManager;
    private final OverlayMessagingManager manager;

    public interface OverlayMessagingInterceptorCallback {
        @UiThread
        void show();

        @UiThread
        void discard();

        @UiThread
        void defer();
    }

    public interface OverlayMessagingInterceptor {
        @UiThread
        void onMessageLoaded(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingInterceptorCallback callback);

        default long getTimeoutMs() {
            return 5000L;
        }
    }

    private OptimoveOverlayMessaging(@NonNull Application application, long sessionLengthMinutes, long deferDelayMinutes) {
        this.sessionManager = new OverlayMessagingSessionManager(application, sessionLengthMinutes,
                deferDelayMinutes, this::onScheduledFetchDue);
        this.manager = new OverlayMessagingManager(application, sessionManager);
    }

    @UiThread
    private void onScheduledFetchDue() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.SESSION);
    }

    /** Scheduled fetching starts here, once credentials are available: the watcher reports the foreground straight away. */
    private void startSessionManager() {
        OptimobileInitProvider.getAppStateWatcher().registerListener(sessionManager);
    }

    //==============================================================================================
    //-- Public API

    public static OptimoveOverlayMessaging getInstance() {
        if (shared == null) {
            throw new IllegalStateException("OptimoveOverlayMessaging is not initialized");
        }
        return shared;
    }

    @AnyThread
    public void setInterceptor(@Nullable OverlayMessagingInterceptor interceptor) {
        Optimobile.handler.post(() -> manager.setInterceptor(interceptor));
    }

    @UiThread
    public void resetSession() {
        sessionManager.reset();
    }

    @AnyThread
    public void setActionHandler(@Nullable OverlayMessagingActionHandler handler) {
        Optimobile.handler.post(() -> manager.setActionHandler(handler));
    }

    @AnyThread
    public void hide() {
        Optimobile.handler.post(() -> manager.setHidden(true));
    }

    @AnyThread
    public void show() {
        Optimobile.handler.post(() -> manager.setHidden(false));
    }

    //==============================================================================================
    //-- Internal

    /** A different user is associated, or none any more: ends the session and fetches for them. */
    @AnyThread
    void onUserChanged() {
        Optimobile.handler.post(sessionManager::reset);
    }

    @UiThread
    void onTriggeredSignalReceived() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
    }

    /** A scheduled Signal: moves the next scheduled fetch earlier. */
    @UiThread
    void onScheduledSignalReceived(long nextFetchInSeconds) {
        sessionManager.onSignal(nextFetchInSeconds);
    }

    static void initialize(@NonNull Application application, @NonNull OptimoveConfig config) {
        shared = new OptimoveOverlayMessaging(application, config.getOverlayMessagingSessionLengthMinutes(),
                config.getOverlayMessagingDeferDelayMinutes());
        if (!config.usesDelayedOptimobileConfiguration()) {
            shared.startSessionManager();
        }
    }

    @AnyThread
    void onCredentialsAvailable() {
        Optimobile.handler.post(this::startSessionManager);
    }
}
