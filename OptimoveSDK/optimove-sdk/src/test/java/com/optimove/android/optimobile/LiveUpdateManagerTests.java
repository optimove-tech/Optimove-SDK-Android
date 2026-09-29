package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotificationManager;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LiveUpdateManagerTests {

    private static final String ACTIVITY_ID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    private Context context;
    private ShadowNotificationManager notifications;
    private LiveUpdateManager manager;
    private RecordingNotificationHandler notificationHandler;

    private static final class Call {
        final LiveUpdateEvent event;
        final LiveUpdate liveUpdate;

        Call(LiveUpdateEvent event, LiveUpdate liveUpdate) {
            this.event = event;
            this.liveUpdate = liveUpdate;
        }
    }

    private static final class RecordingNotificationHandler implements LiveUpdateNotificationHandler {
        final List<Call> calls = new ArrayList<>();

        @Nullable
        @Override
        public NotificationCompat.Builder onCreateNotification(@NonNull Context context,
                                                               @NonNull LiveUpdateEvent event,
                                                               @NonNull LiveUpdate liveUpdate) {
            calls.add(new Call(event, liveUpdate));
            return new NotificationCompat.Builder(context, OptimoveLiveUpdates.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(liveUpdate.getContent().optString("status"));
        }
    }

    private static final class RecordingCustomHandler implements LiveUpdateCustomHandler {
        final List<Call> calls = new ArrayList<>();

        @Override
        public void onLiveUpdateEvent(@NonNull Context context,
                                      @NonNull LiveUpdateEvent event,
                                      @NonNull LiveUpdate liveUpdate) {
            calls.add(new Call(event, liveUpdate));
        }
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        notifications = Shadows.shadowOf(
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        manager = new LiveUpdateManager(context);
        notificationHandler = new RecordingNotificationHandler();
        manager.registerNotificationHandler("notification", notificationHandler);
    }

    private static JSONObject envelope(String event, long sequence, String status) throws JSONException {
        return new JSONObject()
                .put("event", event)
                .put("activity_id", ACTIVITY_ID)
                .put("type", "notification")
                .put("sequence", sequence)
                .put("content", new JSONObject().put("status", status));
    }

    private @Nullable Notification postedNotification() {
        return notifications.getNotification(
                LiveUpdateManager.notificationTag(ACTIVITY_ID), LiveUpdateManager.NOTIFICATION_ID);
    }

    // ========================= notification handler =========================

    @Test
    public void startPostsOngoingNotification() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));

        assertEquals(1, notificationHandler.calls.size());
        assertEquals(LiveUpdateEvent.START, notificationHandler.calls.get(0).event);

        Notification notification = postedNotification();
        assertNotNull(notification);
        assertTrue((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0);
        assertTrue((notification.flags & Notification.FLAG_ONLY_ALERT_ONCE) != 0);
        assertEquals("Kick-off", notification.extras.getString(Notification.EXTRA_TITLE));
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assertNotNull(notificationManager.getNotificationChannel(OptimoveLiveUpdates.NOTIFICATION_CHANNEL_ID));
    }

    @Test
    public void alertDisablesOnlyAlertOnce() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off")
                .put("alert", new JSONObject().put("title", "Match starting")));

        Notification notification = postedNotification();
        assertNotNull(notification);
        assertEquals(0, notification.flags & Notification.FLAG_ONLY_ALERT_ONCE);
    }

    @Test
    public void higherSequenceUpdatesNotification() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));
        manager.handlePush(envelope("update", 3, "15' Goal Spain"));

        assertEquals(2, notificationHandler.calls.size());
        assertEquals(LiveUpdateEvent.UPDATE, notificationHandler.calls.get(1).event);
        assertEquals("15' Goal Spain", postedNotification().extras.getString(Notification.EXTRA_TITLE));
    }

    @Test
    public void lowerOrEqualSequenceIsIgnored() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));
        manager.handlePush(envelope("update", 3, "15' Goal Spain"));
        manager.handlePush(envelope("update", 3, "duplicate"));
        manager.handlePush(envelope("update", 2, "late"));

        assertEquals(2, notificationHandler.calls.size());
        assertEquals("15' Goal Spain", postedNotification().extras.getString(Notification.EXTRA_TITLE));
        assertEquals(3, manager.getActiveLiveUpdates().get(0).getSequence());
    }

    @Test
    public void endDismissesNotificationAndBlocksLaterUpdates() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));
        manager.handlePush(envelope("end", 12, "Full time"));

        assertNull(postedNotification());
        assertTrue(manager.getActiveLiveUpdates().isEmpty());

        manager.handlePush(envelope("update", 13, "resurrected"));

        assertNull(postedNotification());
        assertEquals(1, notificationHandler.calls.size());
    }

    @Test
    public void updateForUnknownActivityStartsIt() throws JSONException {
        manager.handlePush(envelope("update", 5, "20'"));

        assertEquals(1, notificationHandler.calls.size());
        assertEquals(LiveUpdateEvent.START, notificationHandler.calls.get(0).event);
        assertNotNull(postedNotification());
    }

    @Test
    public void updateForUnknownActivityWithoutTypeIsIgnored() throws JSONException {
        JSONObject update = envelope("update", 5, "20'");
        update.remove("type");

        manager.handlePush(update);

        assertTrue(notificationHandler.calls.isEmpty());
        assertTrue(manager.getActiveLiveUpdates().isEmpty());
    }

    // ========================= persistence =========================

    @Test
    public void stateSurvivesProcessDeath() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off").put("name", "spain-vs-argentina"));

        LiveUpdateManager restarted = new LiveUpdateManager(context);
        RecordingNotificationHandler restartedHandler = new RecordingNotificationHandler();
        restarted.registerNotificationHandler("notification", restartedHandler);

        JSONObject update = envelope("update", 1, "5'");
        update.remove("type");
        restarted.handlePush(update);

        assertEquals(1, restartedHandler.calls.size());
        LiveUpdate applied = restartedHandler.calls.get(0).liveUpdate;
        assertEquals(LiveUpdateEvent.UPDATE, restartedHandler.calls.get(0).event);
        assertEquals("notification", applied.getType());
        assertEquals("spain-vs-argentina", applied.getName());
        assertEquals(1, applied.getSequence());

        restarted.handlePush(envelope("update", 0, "stale"));
        assertEquals(1, restartedHandler.calls.size());
    }

    @Test
    public void endedStateSurvivesProcessDeath() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));
        manager.handlePush(envelope("end", 2, "Full time"));

        LiveUpdateManager restarted = new LiveUpdateManager(context);
        RecordingNotificationHandler restartedHandler = new RecordingNotificationHandler();
        restarted.registerNotificationHandler("notification", restartedHandler);
        restarted.handlePush(envelope("update", 3, "late"));

        assertTrue(restartedHandler.calls.isEmpty());
    }

    // ========================= custom handler =========================

    @Test
    public void customHandlerReceivesEventsWithoutNotification() throws JSONException {
        RecordingCustomHandler customHandler = new RecordingCustomHandler();
        manager.registerCustomHandler("widget", customHandler);

        manager.handlePush(envelope("start", 0, "Kick-off").put("type", "widget"));
        manager.handlePush(envelope("update", 1, "5'").put("type", "widget"));
        manager.handlePush(envelope("end", 2, "Full time").put("type", "widget"));

        assertEquals(3, customHandler.calls.size());
        assertEquals(LiveUpdateEvent.START, customHandler.calls.get(0).event);
        assertEquals(LiveUpdateEvent.UPDATE, customHandler.calls.get(1).event);
        assertEquals(LiveUpdateEvent.END, customHandler.calls.get(2).event);
        assertEquals("Full time", customHandler.calls.get(2).liveUpdate.getContent().getString("status"));
        assertTrue(notificationHandler.calls.isEmpty());
        assertTrue(notifications.getAllNotifications().isEmpty());
    }

    @Test
    public void throwingHandlerDoesNotCrashOrLoseState() throws JSONException {
        manager.registerCustomHandler("notification", (ctx, event, liveUpdate) -> {
            throw new IllegalStateException("host bug");
        });

        manager.handlePush(envelope("start", 0, "Kick-off"));

        assertEquals(1, manager.getActiveLiveUpdates().size());
    }

    // ========================= malformed =========================

    @Test
    public void malformedEnvelopeIsDropped() throws JSONException {
        manager.handlePush("garbage");
        manager.handlePush(new JSONObject().put("event", "start"));

        assertTrue(notificationHandler.calls.isEmpty());
        assertTrue(manager.getActiveLiveUpdates().isEmpty());
    }

    @Test
    public void routerConsumesLiveUpdatesAndPassesThroughNormalPushes() throws JSONException {
        String normalPush = new JSONObject()
                .put("a", new JSONObject().put("k.message",
                        new JSONObject().put("type", 1).put("data", new JSONObject().put("id", 1))))
                .toString();
        String malformedLiveUpdate = new JSONObject()
                .put("a", new JSONObject().put(LiveUpdateEnvelope.KEY, "garbage"))
                .toString();

        assertEquals(false, OptimoveLiveUpdates.handlePushPayload(normalPush));
        assertEquals(true, OptimoveLiveUpdates.handlePushPayload(malformedLiveUpdate));
    }

    // ========================= local API =========================

    @Test
    public void localStartUpdateEnd() throws JSONException {
        manager.start(ACTIVITY_ID, "notification", new JSONObject().put("status", "Kick-off"));
        manager.update(ACTIVITY_ID, new JSONObject().put("status", "10'"));

        assertEquals("10'", postedNotification().extras.getString(Notification.EXTRA_TITLE));
        assertEquals(LiveUpdate.NO_SEQUENCE, manager.getActiveLiveUpdates().get(0).getSequence());

        manager.handlePush(envelope("update", 0, "server"));
        assertEquals("server", postedNotification().extras.getString(Notification.EXTRA_TITLE));

        manager.end(ACTIVITY_ID);
        assertNull(postedNotification());
    }

    @Test
    public void clearAllDismissesAndForgetsEverything() throws JSONException {
        manager.handlePush(envelope("start", 0, "Kick-off"));
        manager.clearAll();

        assertNull(postedNotification());
        assertTrue(manager.getActiveLiveUpdates().isEmpty());

        manager.handlePush(envelope("start", 0, "again"));
        assertNotNull(postedNotification());
    }
}
