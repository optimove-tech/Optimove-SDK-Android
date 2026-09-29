package com.optimove.android.optimobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class LiveUpdateManager {

    private static final String TAG = LiveUpdateManager.class.getName();

    static final String NOTIFICATION_TAG_PREFIX = "optimobile_live_update:";
    static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final LiveUpdateStore store;
    private final Map<String, LiveUpdateNotificationHandler> notificationHandlers = new ConcurrentHashMap<>();
    private final Map<String, LiveUpdateCustomHandler> customHandlers = new ConcurrentHashMap<>();

    LiveUpdateManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.store = new LiveUpdateStore(
                this.context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE));
    }

    void registerNotificationHandler(@NonNull String type, @NonNull LiveUpdateNotificationHandler handler) {
        customHandlers.remove(type);
        notificationHandlers.put(type, handler);
    }

    void registerCustomHandler(@NonNull String type, @NonNull LiveUpdateCustomHandler handler) {
        notificationHandlers.remove(type);
        customHandlers.put(type, handler);
    }

    //==============================================================================================
    //-- Push

    void handlePush(@Nullable Object rawEnvelope) {
        LiveUpdateEnvelope envelope;
        try {
            envelope = LiveUpdateEnvelope.parse(rawEnvelope);
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: dropping malformed " + LiveUpdateEnvelope.KEY + ": " + e.getMessage());
            return;
        }

        Optimobile.log(TAG, "Live Updates: routing " + envelope.event + " for " + envelope.activityId
                + " (sequence " + envelope.sequence + ")");

        applyEnvelope(envelope);
    }

    private synchronized void applyEnvelope(@NonNull LiveUpdateEnvelope envelope) {
        String activityId = envelope.activityId;

        if (store.isEnded(activityId)) {
            Optimobile.log(TAG, "Live Updates: " + activityId + " has already ended, ignoring " + envelope.event);
            return;
        }

        LiveUpdate existing = store.getActive(activityId);
        if (existing != null && envelope.sequence <= existing.getSequence()) {
            Optimobile.log(TAG, "Live Updates: stale sequence " + envelope.sequence + " for " + activityId
                    + " (last applied " + existing.getSequence() + "), ignoring");
            return;
        }

        long now = System.currentTimeMillis();

        if (envelope.event == LiveUpdateEvent.END) {
            store.markEnded(activityId, now);
            if (existing == null) {
                return;
            }
            LiveUpdate last = withContent(existing, envelope.sequence, envelope.content,
                    envelope.alertTitle, envelope.alertBody, now);
            dispatchEnd(last);
            return;
        }

        String type = envelope.type != null ? envelope.type : (existing != null ? existing.getType() : null);
        if (type == null) {
            Optimobile.log(TAG, "Live Updates: " + envelope.event + " for unknown activity " + activityId
                    + " has no type, ignoring");
            return;
        }

        LiveUpdate liveUpdate = new LiveUpdate(
                activityId,
                type,
                envelope.name != null ? envelope.name : (existing != null ? existing.getName() : null),
                envelope.sequence,
                envelope.content,
                envelope.alertTitle,
                envelope.alertBody,
                existing != null ? existing.getStartedAt() : now,
                now);

        store.putActive(liveUpdate);
        dispatch(existing == null ? LiveUpdateEvent.START : LiveUpdateEvent.UPDATE, liveUpdate);
    }

    //==============================================================================================
    //-- Local

    synchronized void start(@NonNull String activityId, @NonNull String type, @NonNull JSONObject content) {
        LiveUpdate existing = store.getActive(activityId);
        long now = System.currentTimeMillis();

        LiveUpdate liveUpdate = new LiveUpdate(
                activityId,
                type,
                existing != null ? existing.getName() : null,
                existing != null ? existing.getSequence() : LiveUpdate.NO_SEQUENCE,
                content,
                null,
                null,
                existing != null ? existing.getStartedAt() : now,
                now);

        store.putActive(liveUpdate);
        dispatch(existing == null ? LiveUpdateEvent.START : LiveUpdateEvent.UPDATE, liveUpdate);
    }

    synchronized void update(@NonNull String activityId, @NonNull JSONObject content) {
        LiveUpdate existing = store.getActive(activityId);
        if (existing == null) {
            Optimobile.log(TAG, "Live Updates: update for unknown activity " + activityId + ", ignoring");
            return;
        }

        LiveUpdate liveUpdate = withContent(existing, existing.getSequence(), content, null, null,
                System.currentTimeMillis());
        store.putActive(liveUpdate);
        dispatch(LiveUpdateEvent.UPDATE, liveUpdate);
    }

    synchronized void end(@NonNull String activityId) {
        LiveUpdate existing = store.getActive(activityId);
        if (existing == null) {
            Optimobile.log(TAG, "Live Updates: end for unknown activity " + activityId + ", ignoring");
            return;
        }

        store.markEnded(activityId, System.currentTimeMillis());
        dispatchEnd(existing);
    }

    synchronized @NonNull List<LiveUpdate> getActiveLiveUpdates() {
        return store.getAllActive();
    }

    synchronized void clearAll() {
        for (LiveUpdate liveUpdate : store.getAllActive()) {
            dispatchEnd(liveUpdate);
        }
        store.clear();
    }

    //==============================================================================================
    //-- Dispatch

    private void dispatch(@NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate) {
        String type = liveUpdate.getType();

        LiveUpdateNotificationHandler notificationHandler = notificationHandlers.get(type);
        if (notificationHandler != null) {
            postNotification(notificationHandler, event, liveUpdate);
            return;
        }

        LiveUpdateCustomHandler customHandler = customHandlers.get(type);
        if (customHandler != null) {
            invokeCustomHandler(customHandler, event, liveUpdate);
            return;
        }

        Optimobile.log(TAG, "Live Updates: no handler registered for type '" + type + "'; state saved for "
                + liveUpdate.getActivityId());
    }

    private void dispatchEnd(@NonNull LiveUpdate liveUpdate) {
        cancelNotification(liveUpdate.getActivityId());

        LiveUpdateCustomHandler customHandler = customHandlers.get(liveUpdate.getType());
        if (customHandler != null) {
            invokeCustomHandler(customHandler, LiveUpdateEvent.END, liveUpdate);
        }
    }

    private void invokeCustomHandler(@NonNull LiveUpdateCustomHandler handler,
                                     @NonNull LiveUpdateEvent event,
                                     @NonNull LiveUpdate liveUpdate) {
        try {
            handler.onLiveUpdateEvent(context, event, liveUpdate);
        } catch (RuntimeException e) {
            Optimobile.log(TAG, "Live Updates: custom handler for '" + liveUpdate.getType() + "' threw: " + e);
        }
    }

    private void postNotification(@NonNull LiveUpdateNotificationHandler handler,
                                  @NonNull LiveUpdateEvent event,
                                  @NonNull LiveUpdate liveUpdate) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Optimobile.log(TAG, "Live Updates: notifications are disabled, not posting " + liveUpdate.getActivityId());
            return;
        }

        ensureNotificationChannel(notificationManager);

        NotificationCompat.Builder builder;
        try {
            builder = handler.onCreateNotification(context, event, liveUpdate);
        } catch (RuntimeException e) {
            Optimobile.log(TAG, "Live Updates: notification handler for '" + liveUpdate.getType() + "' threw: " + e);
            return;
        }

        if (builder == null) {
            return;
        }

        builder.setOngoing(true);
        builder.setOnlyAlertOnce(!liveUpdate.hasAlert());

        Notification notification = builder.build();
        if (notification.contentIntent == null) {
            notification.contentIntent = getLaunchPendingIntent(liveUpdate.getActivityId());
        }

        notificationManager.notify(notificationTag(liveUpdate.getActivityId()), NOTIFICATION_ID, notification);
    }

    private void cancelNotification(@NonNull String activityId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationTag(activityId), NOTIFICATION_ID);
        }
    }

    private @Nullable PendingIntent getLaunchPendingIntent(@NonNull String activityId) {
        Intent launchIntent;
        try {
            launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        } catch (RuntimeException e) {
            return null;
        }

        if (launchIntent == null) {
            return null;
        }

        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, activityId.hashCode(), launchIntent, flags);
    }

    private static void ensureNotificationChannel(@NonNull NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (notificationManager.getNotificationChannel(OptimoveLiveUpdates.NOTIFICATION_CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                OptimoveLiveUpdates.NOTIFICATION_CHANNEL_ID,
                "Live Updates",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    static @NonNull String notificationTag(@NonNull String activityId) {
        return NOTIFICATION_TAG_PREFIX + activityId;
    }

    private static @NonNull LiveUpdate withContent(@NonNull LiveUpdate existing,
                                                   long sequence,
                                                   @NonNull JSONObject content,
                                                   @Nullable String alertTitle,
                                                   @Nullable String alertBody,
                                                   long now) {
        return new LiveUpdate(
                existing.getActivityId(),
                existing.getType(),
                existing.getName(),
                sequence,
                content,
                alertTitle,
                alertBody,
                existing.getStartedAt(),
                now);
    }
}
