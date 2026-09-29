package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Renders a Live Update as an ongoing notification. The host builds the layout (typically collapsed and
 * expanded {@link android.widget.RemoteViews}); the SDK posts, updates and dismisses the notification.
 * <p>
 * Invoked on start and on every applied update, on the thread that delivered the update (a background
 * thread for push). On end the SDK dismisses the notification without calling the handler.
 *
 * @see OptimoveLiveUpdates#registerNotificationHandler(String, LiveUpdateNotificationHandler)
 */
public interface LiveUpdateNotificationHandler {

    /**
     * Build the notification for the current state of the Live Update.
     * <p>
     * Use {@link OptimoveLiveUpdates#NOTIFICATION_CHANNEL_ID} unless you manage your own channel. The SDK
     * marks the notification ongoing, and only-alert-once unless the event carries an alert. If no content
     * intent is set, tapping opens the app's launch activity.
     *
     * @return the builder to post, or null to leave the current notification unchanged
     */
    @Nullable
    NotificationCompat.Builder onCreateNotification(@NonNull Context context,
                                                    @NonNull LiveUpdateEvent event,
                                                    @NonNull LiveUpdate liveUpdate);
}
