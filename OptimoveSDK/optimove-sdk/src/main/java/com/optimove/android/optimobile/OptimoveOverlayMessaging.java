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

    @Nullable
    private OverlayMessagingInterceptor interceptor;

    public interface OverlayMessagingInterceptor {
        @UiThread
        OverlayMessagingInterceptorOutcome onMessageLoaded(@NonNull OverlayMessagingMessage message);
    }

    public enum OverlayMessagingInterceptorOutcome {
        SHOW,
        DISCARD,
        HOLD
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
        this.interceptor = interceptor;
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
