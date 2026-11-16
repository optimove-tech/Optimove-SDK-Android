package com.optimove.android.optimobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Optional helper for hosts that render a Live Update as a notification: posts, updates and cancels it by name,
 * applies the dismissal date as a timeout, and ends the Live Update when the user swipes it away.
 */
public final class LiveUpdateNotifications {

    private static final String TAG = LiveUpdateNotifications.class.getName();

    public static final int NOTIFICATION_ID = 1;
    static final String TAG_PREFIX = "optimove_live_update:";
    static final String DISMISS_URI_SCHEME = "optimove-live-update";
    static final long DEFAULT_TIMEOUT_MS = LiveUpdateManager.INACTIVITY_MS;
    static final long END_TIMEOUT_MS = 4 * 60 * 60 * 1000L;

    private LiveUpdateNotifications() {
    }

    public static @NonNull String tagFor(@NonNull String name) {
        return TAG_PREFIX + name;
    }

    public static void post(@NonNull Context context,
                            @NonNull LiveUpdateEvent event,
                            @NonNull LiveUpdate liveUpdate,
                            @NonNull NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (!notificationManager.areNotificationsEnabled()) {
            Optimobile.log(TAG, "Live Updates: notifications are disabled, not posting " + liveUpdate.getName());
            return;
        }

        String name = liveUpdate.getName();
        boolean ending = event == LiveUpdateEvent.END;

        builder.setOngoing(!ending);
        builder.setOnlyAlertOnce(!liveUpdate.hasAlert());

        if (ending) {
            if (liveUpdate.getEndReason() != LiveUpdateEndReason.SERVER) {
                cancel(context, name);
                return;
            }
            Long dismissalDateMillis = liveUpdate.getDismissalDateMillis();
            if (dismissalDateMillis == null) {
                builder.setTimeoutAfter(END_TIMEOUT_MS);
            } else {
                long remaining = dismissalDateMillis - System.currentTimeMillis();
                if (remaining <= 0) {
                    cancel(context, name);
                    return;
                }
                builder.setTimeoutAfter(remaining);
            }
        } else {
            builder.setTimeoutAfter(DEFAULT_TIMEOUT_MS);
        }

        Notification notification = builder.build();
        notification.deleteIntent = dismissIntent(context, name, notification.deleteIntent);

        notificationManager.notify(tagFor(name), NOTIFICATION_ID, notification);

        if (!isChannelBlocked(context, notification)) {
            reportShown(name);
        }
    }

    private static void reportShown(@NonNull String name) {
        try {
            OptimoveLiveUpdates.getInstance().reportShown(name);
        } catch (IllegalStateException e) {
            Optimobile.log(TAG, "Live Updates: posted " + name + " before Optimove was initialized, not reporting it");
        }
    }

    private static boolean isChannelBlocked(@NonNull Context context, @NonNull Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || notification.getChannelId() == null) {
            return false;
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(notification.getChannelId());
        return channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE;
    }

    public static void cancel(@NonNull Context context, @NonNull String name) {
        NotificationManagerCompat.from(context).cancel(tagFor(name), NOTIFICATION_ID);
    }

    private static @NonNull PendingIntent dismissIntent(@NonNull Context context,
                                                        @NonNull String name,
                                                        @Nullable PendingIntent original) {
        Intent intent = new Intent(context, LiveUpdateDismissReceiver.class)
                .setData(Uri.parse(DISMISS_URI_SCHEME + "://" + Uri.encode(name)))
                .putExtra(LiveUpdateDismissReceiver.EXTRA_NAME, name);
        if (original != null) {
            intent.putExtra(LiveUpdateDismissReceiver.EXTRA_ORIGINAL_DELETE_INTENT, original);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getBroadcast(context, name.hashCode(), intent, flags);
    }
}
