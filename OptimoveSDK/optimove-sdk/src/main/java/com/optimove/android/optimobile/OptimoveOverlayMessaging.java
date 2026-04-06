package com.optimove.android.optimobile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class OptimoveOverlayMessaging {

    private static OptimoveOverlayMessaging shared;

    private final OverlayMessagingSessionManager sessionManager;
    private final OverlayMessagingManager manager;

    public interface OverlayMessagingInterceptorCallback {
        @UiThread void show();
        @UiThread void discard();
        @UiThread void defer();
    }

    public interface OverlayMessagingInterceptor {
        @UiThread
        void onMessageLoaded(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingInterceptorCallback callback);

        default long getTimeoutMs() {
            return 5000L;
        }
    }

    private OptimoveOverlayMessaging(@NonNull Application application, long sessionLengthHours) {
        this.manager = new OverlayMessagingManager(application);
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
        sessionManager.resetSession();
    }

    //==============================================================================================
    //-- Internal

    @UiThread
    void onPushTriggerReceived() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
    }

    static void initialize(@NonNull Application application, long sessionLengthHours) {
        shared = new OptimoveOverlayMessaging(application, sessionLengthHours);
    }
}
