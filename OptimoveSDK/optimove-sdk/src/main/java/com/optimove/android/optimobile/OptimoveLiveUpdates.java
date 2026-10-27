package com.optimove.android.optimobile;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.optimove.android.OptimoveConfig;

import java.util.Collections;
import java.util.List;

/**
 * Live Updates: server-driven content (e.g. a live score) delivered over FCM and rendered by the host app.
 * Enable with {@link OptimoveConfig.Builder#enableLiveUpdates(LiveUpdateHandler)}.
 */
public final class OptimoveLiveUpdates {

    private static final String TAG = OptimoveLiveUpdates.class.getName();

    private static volatile @Nullable OptimoveLiveUpdates shared;

    private final @NonNull Context context;
    private final @Nullable LiveUpdateManager manager;

    private OptimoveLiveUpdates(@NonNull Context context, @Nullable LiveUpdateManager manager) {
        this.context = context;
        this.manager = manager;
    }

    static void initialize(@NonNull Application application, @NonNull OptimoveConfig config) {
        Context context = application.getApplicationContext();
        LiveUpdateHandler handler = config.getLiveUpdateHandler();
        if (handler == null) {
            shared = new OptimoveLiveUpdates(context, null);
            return;
        }

        LiveUpdateStore store = new LiveUpdateStore(
                context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE));
        LiveUpdateManager manager = new LiveUpdateManager(
                context,
                handler,
                store,
                new FirebaseTopicSubscriber(),
                optimobileEvents(context),
                System::currentTimeMillis);

        initialize(context, manager);
        Optimobile.executorService.submit(manager::sweep);
    }

    static void initialize(@NonNull Context context, @NonNull LiveUpdateManager manager) {
        shared = new OptimoveLiveUpdates(context.getApplicationContext(), manager);
    }

    @VisibleForTesting
    static void reset() {
        shared = null;
    }

    public static @NonNull OptimoveLiveUpdates getInstance() {
        OptimoveLiveUpdates instance = shared;
        if (instance == null) {
            throw new IllegalStateException("OptimoveLiveUpdates is not initialized; call Optimove.initialize first");
        }
        return instance;
    }

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
        if (instance.manager == null) {
            Optimobile.log(TAG, "Live Updates: received a Live Update but enableLiveUpdates was not called, dropping");
            return true;
        }

        instance.manager.handlePush(rawEnvelope);
        return true;
    }

    public boolean isEnabled() {
        return manager != null;
    }

    public void end(@NonNull String name) {
        if (manager != null) {
            manager.end(name);
        }
    }

    public @NonNull List<LiveUpdate> getActiveLiveUpdates() {
        return manager != null ? manager.getActive() : Collections.<LiveUpdate>emptyList();
    }

    public void clearAll() {
        if (manager == null) {
            return;
        }
        for (String name : manager.clearAll()) {
            LiveUpdateNotifications.cancel(context, name);
        }
    }

    void endFromNotificationDismissal(@NonNull String name) {
        if (manager != null) {
            manager.endFromNotificationDismissal(name);
        }
    }

    private static @NonNull EventTracker optimobileEvents(@NonNull Context context) {
        return (eventType, properties) -> {
            if (!Optimobile.isInitialized()) {
                Optimobile.log(TAG, "Live Updates: Optimobile not initialized, dropping " + eventType);
                return;
            }
            Optimobile.trackEventImmediately(context, eventType, properties);
        };
    }
}
