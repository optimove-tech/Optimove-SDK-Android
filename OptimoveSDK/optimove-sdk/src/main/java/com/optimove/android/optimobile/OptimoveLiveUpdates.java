package com.optimove.android.optimobile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;

/**
 * Live Updates: long-running, server-driven content (e.g. a live score) rendered by the host app.
 * <p>
 * Live Updates arrive as FCM / HMS data messages on the existing push token. The SDK recognises them,
 * keeps them off the normal push path, persists their state across process death, and hands the opaque
 * content to the handler registered for the Live Update's type.
 * <p>
 * Register handlers in {@code Application#onCreate} after {@code Optimove.initialize}, so they are in
 * place when a push cold-starts the process.
 */
public final class OptimoveLiveUpdates {

    private static final String TAG = OptimoveLiveUpdates.class.getName();

    /**
     * Channel the SDK creates for Live Update notifications. Use it in
     * {@link LiveUpdateNotificationHandler#onCreateNotification} unless you manage your own channel.
     */
    public static final String NOTIFICATION_CHANNEL_ID = "optimobile_ch_live_updates";

    private static @Nullable OptimoveLiveUpdates shared;

    private final LiveUpdateManager manager;

    private OptimoveLiveUpdates(@NonNull Application application) {
        this.manager = new LiveUpdateManager(application);
    }

    static synchronized void initialize(@NonNull Application application) {
        if (shared == null) {
            shared = new OptimoveLiveUpdates(application);
        }
    }

    public static @NonNull OptimoveLiveUpdates getInstance() {
        OptimoveLiveUpdates instance = shared;
        if (instance == null) {
            throw new IllegalStateException("OptimoveLiveUpdates is not initialized");
        }
        return instance;
    }

    /**
     * @return true if the payload carried a Live Update, in which case it must not be shown as a normal push
     */
    static boolean handlePushPayload(@Nullable String customStr) {
        Object rawEnvelope = LiveUpdateEnvelope.extract(customStr);
        if (rawEnvelope == null) {
            return false;
        }

        OptimoveLiveUpdates instance = shared;
        if (instance == null) {
            Optimobile.log(TAG, "Live Updates: received a Live Update before Optimove was initialized, dropping");
            return true;
        }

        instance.manager.handlePush(rawEnvelope);
        return true;
    }

    //==============================================================================================
    //-- Public API

    /**
     * Render Live Updates of the given type as an ongoing notification built by {@code handler}.
     * Replaces any handler previously registered for the type.
     */
    public void registerNotificationHandler(@NonNull String type, @NonNull LiveUpdateNotificationHandler handler) {
        manager.registerNotificationHandler(type, handler);
    }

    /**
     * Deliver Live Updates of the given type to {@code handler}, e.g. to drive a widget or in-app UI.
     * The SDK does not post a notification for this type. Replaces any handler previously registered for the type.
     */
    public void registerCustomHandler(@NonNull String type, @NonNull LiveUpdateCustomHandler handler) {
        manager.registerCustomHandler(type, handler);
    }

    /**
     * Start a Live Update from the app. If it is already active, this updates its type and content.
     */
    public void start(@NonNull String activityId, @NonNull String type, @NonNull JSONObject content) {
        manager.start(activityId, type, content);
    }

    /**
     * Update an active Live Update from the app. Ignored if the activity is not active on this device.
     */
    public void update(@NonNull String activityId, @NonNull JSONObject content) {
        manager.update(activityId, content);
    }

    /**
     * End an active Live Update from the app, dismissing its notification. Later pushes for it are ignored.
     */
    public void end(@NonNull String activityId) {
        manager.end(activityId);
    }

    public @NonNull List<LiveUpdate> getActiveLiveUpdates() {
        return manager.getActiveLiveUpdates();
    }

    /**
     * Ends every active Live Update and forgets all state, including ended activities. Intended for development.
     */
    public void clearAll() {
        manager.clearAll();
    }
}
