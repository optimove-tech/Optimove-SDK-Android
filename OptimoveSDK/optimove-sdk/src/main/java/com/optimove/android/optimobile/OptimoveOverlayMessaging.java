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
    private final long sessionLengthHours;

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

    private OptimoveOverlayMessaging(@NonNull Application application, long sessionLengthHours) {
        this.application = application;
        this.sessionLengthHours = sessionLengthHours;
        this.manager = new OverlayMessagingManager(application);
    }

    private void startSessionManager() {
        OverlayMessagingSessionManager.Listener sessionListener = () ->
                manager.onTriggerReceived(OverlayMessagingMessage.MessageType.SESSION);
        this.sessionManager = new OverlayMessagingSessionManager(application, sessionLengthHours, sessionListener);
    }

    //==============================================================================================
    //-- Public API

    public static OptimoveOverlayMessaging getInstance() {
        if (shared == null) {
            throw new IllegalStateException("OptimoveOverlayMessaging is not initialized");
        }
        return shared;
    }

    public void setInterceptor(@Nullable OverlayMessagingInterceptor interceptor) {
        manager.setInterceptor(interceptor);
    }

    @UiThread
    public void resetSession() {
        if (sessionManager != null) {
            sessionManager.resetSession();
        }
    }

    //==============================================================================================
    //-- Internal

    @UiThread
    void onPushTriggerReceived() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
    }

    static void initialize(@NonNull Application application, @NonNull OptimoveConfig config) {
        shared = new OptimoveOverlayMessaging(application, config.getOverlayMessagingSessionLengthHours());
        if (!config.usesDelayedOptimobileConfiguration()) {
            shared.startSessionManager();
        }
    }

    @AnyThread
    void onCredentialsAvailable() {
        Optimobile.handler.post(this::startSessionManager);
    }
}
