package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotificationManager;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LiveUpdateNotificationsTests {

    private static final String NAME = "spain-vs-argentina";
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000L;

    private Context context;
    private ShadowNotificationManager notifications;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        notifications = shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
    }

    private static LiveUpdate liveUpdate(boolean alert, Long dismissalDateMillis, LiveUpdateEndReason endReason) {
        long now = System.currentTimeMillis();
        return new LiveUpdate(NAME, "T", 1, new JSONObject(), alert ? "Goal!" : null, null,
                dismissalDateMillis, now, now, endReason);
    }

    private NotificationCompat.Builder builder() {
        return new NotificationCompat.Builder(context, "channel").setSmallIcon(android.R.drawable.ic_dialog_info);
    }

    private Notification posted() {
        return notifications.getNotification(LiveUpdateNotifications.tagFor(NAME), LiveUpdateNotifications.NOTIFICATION_ID);
    }

    private static boolean hasFlag(Notification notification, int flag) {
        return (notification.flags & flag) != 0;
    }

    @Test
    public void startAndUpdateAreOngoingWithTimeoutAndAlertOnlyWhenAsked() {
        LiveUpdateNotifications.post(context, LiveUpdateEvent.START, liveUpdate(false, null, null), builder());
        Notification start = posted();
        assertTrue(hasFlag(start, Notification.FLAG_ONGOING_EVENT));
        assertTrue(hasFlag(start, Notification.FLAG_ONLY_ALERT_ONCE));
        assertEquals(LiveUpdateNotifications.DEFAULT_TIMEOUT_MS, start.getTimeoutAfter());

        LiveUpdateNotifications.post(context, LiveUpdateEvent.UPDATE, liveUpdate(true, null, null), builder());
        assertFalse(hasFlag(posted(), Notification.FLAG_ONLY_ALERT_ONCE));
        assertEquals(1, notifications.getAllNotifications().size());
    }

    @Test
    public void endIsNotOngoingAndHonoursTheDismissalDate() {
        LiveUpdateNotifications.post(context, LiveUpdateEvent.END, liveUpdate(false, null, LiveUpdateEndReason.SERVER), builder());
        assertFalse(hasFlag(posted(), Notification.FLAG_ONGOING_EVENT));
        assertEquals(0, posted().getTimeoutAfter());

        long dismissal = System.currentTimeMillis() + FOUR_HOURS;
        LiveUpdateNotifications.post(context, LiveUpdateEvent.END, liveUpdate(false, dismissal, LiveUpdateEndReason.SERVER), builder());
        long timeout = posted().getTimeoutAfter();
        assertTrue(timeout > FOUR_HOURS - 5_000 && timeout <= FOUR_HOURS);

        LiveUpdateNotifications.post(context, LiveUpdateEvent.END,
                liveUpdate(false, System.currentTimeMillis() - 1000, LiveUpdateEndReason.SERVER), builder());
        assertNull(posted());

        LiveUpdateNotifications.post(context, LiveUpdateEvent.START, liveUpdate(false, null, null), builder());
        LiveUpdateNotifications.post(context, LiveUpdateEvent.END, liveUpdate(false, dismissal, LiveUpdateEndReason.LOCAL), builder());
        assertNull(posted());
    }

    @Test
    public void deleteIntentTargetsTheDismissReceiverAndForwardsTheHostsOwn() {
        PendingIntent original = PendingIntent.getBroadcast(context, 7, new Intent("host.DELETE"), PendingIntent.FLAG_IMMUTABLE);

        LiveUpdateNotifications.post(context, LiveUpdateEvent.START, liveUpdate(false, null, null),
                builder().setDeleteIntent(original));

        Intent saved = shadowOf(posted().deleteIntent).getSavedIntent();
        assertEquals(new ComponentName(context, LiveUpdateDismissReceiver.class), saved.getComponent());
        assertEquals(NAME, saved.getStringExtra(LiveUpdateDismissReceiver.EXTRA_NAME));
        PendingIntent forwarded = saved.getParcelableExtra(LiveUpdateDismissReceiver.EXTRA_ORIGINAL_DELETE_INTENT);
        assertNotNull(forwarded);
        assertEquals("host.DELETE", shadowOf(forwarded).getSavedIntent().getAction());
    }

    @Test
    public void cancelRemovesByNameAndDisabledNotificationsAreSkipped() {
        LiveUpdateNotifications.post(context, LiveUpdateEvent.START, liveUpdate(false, null, null), builder());
        LiveUpdateNotifications.cancel(context, NAME);
        assertNull(posted());

        notifications.setNotificationsEnabled(false);
        LiveUpdateNotifications.post(context, LiveUpdateEvent.START, liveUpdate(false, null, null), builder());
        assertNull(posted());
    }
}
