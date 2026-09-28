package com.optimove.android.optimobile;

import android.app.Application;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.optimove.android.OptimoveConfig;

public class OptimoveOverlayMessaging {

    private static OptimoveOverlayMessaging shared;

    @Nullable
    private OverlayMessagingSessionManager sessionManager;
    private final OverlayMessagingManager manager;
    private final Application application;
    private final long sessionLengthMinutes;

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

    private OptimoveOverlayMessaging(@NonNull Application application, long sessionLengthMinutes) {
        this.application = application;
        this.sessionLengthMinutes = sessionLengthMinutes;
        this.manager = new OverlayMessagingManager(application);
    }

    private void startSessionManager() {
        OverlayMessagingSessionManager.Listener sessionListener = () ->
                manager.onTriggerReceived(OverlayMessagingMessage.MessageType.SESSION);
        this.sessionManager = new OverlayMessagingSessionManager(application, sessionLengthMinutes, sessionListener);
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
        if (sessionManager != null) {
            sessionManager.resetSession();
        }
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

    @UiThread
    void onPushTriggerReceived() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
    }

    static void initialize(@NonNull Application application, @NonNull OptimoveConfig config) {
        shared = new OptimoveOverlayMessaging(application, config.getOverlayMessagingSessionLengthMinutes());
        if (!config.usesDelayedOptimobileConfiguration()) {
            shared.startSessionManager();
        }
    }

    @AnyThread
    void onCredentialsAvailable() {
        Optimobile.handler.post(this::startSessionManager);
    }
}
