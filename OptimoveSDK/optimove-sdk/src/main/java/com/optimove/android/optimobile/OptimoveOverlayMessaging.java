package com.optimove.android.optimobile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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
        @UiThread void hold();
    }

    public interface OverlayMessagingInterceptor {
        @UiThread
        void onMessageLoaded(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingInterceptorCallback callback);

        default long getTimeoutMs() {
            return 5000L;
        }
    }

    private OptimoveOverlayMessaging(@NonNull Application application, long sessionLengthMinutes) {
        this.manager = new OverlayMessagingManager(application);
        OverlayMessagingSessionManager.Listener sessionListener = () ->
                manager.onTriggerReceived(OverlayMessagingManager.MessageType.SESSION);
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
        manager.onTriggerReceived(OverlayMessagingManager.MessageType.IMMEDIATE);
    }

    static void initialize(@NonNull Application application, long sessionLengthMinutes) {
        shared = new OptimoveOverlayMessaging(application, sessionLengthMinutes);
    }

    boolean isOverlayMessagingEnabled() {
        return shared != null;
    }
}
