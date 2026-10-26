package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LiveUpdateManagerTests {

    private static final String NAME = "spain-vs-argentina";
    private static final String TYPE = "SportsActivityAttributes";
    private static final String TOPIC = "lu_1_abc";
    private static final long T0 = 1_786_024_800L;
    private static final long NOW = T0 * 1000L;

    private static final class RecordingHandler implements LiveUpdateHandler {
        final List<LiveUpdateEvent> events = new ArrayList<>();
        final List<LiveUpdate> updates = new ArrayList<>();
        boolean throwOnCall;

        @Override
        public void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate) {
            events.add(event);
            updates.add(liveUpdate);
            if (throwOnCall) {
                throw new IllegalStateException("host bug");
            }
        }

        LiveUpdate last() {
            return updates.get(updates.size() - 1);
        }
    }

    private static final class FakeTopics implements TopicSubscriber {
        final List<String> subscribed = new ArrayList<>();
        final List<String> unsubscribed = new ArrayList<>();

        @Override
        public void subscribe(@NonNull String topic) {
            subscribed.add(topic);
        }

        @Override
        public void unsubscribe(@NonNull String topic) {
            unsubscribed.add(topic);
        }
    }

    private static final class FakeEvents implements EventTracker {
        final List<String> types = new ArrayList<>();
        final List<JSONObject> props = new ArrayList<>();

        @Override
        public void track(@NonNull String eventType, @Nullable JSONObject properties) {
            types.add(eventType);
            props.add(properties);
        }

        String lastReason() {
            return props.get(props.size() - 1).optString(LiveUpdateManager.PROP_REASON);
        }
    }

    private static final class FakeClock implements Clock {
        long now = NOW;

        @Override
        public long nowMillis() {
            return now;
        }
    }

    private Context context;
    private SharedPreferences prefs;
    private RecordingHandler handler;
    private FakeTopics topics;
    private FakeEvents events;
    private FakeClock clock;
    private LiveUpdateManager manager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        handler = new RecordingHandler();
        topics = new FakeTopics();
        events = new FakeEvents();
        clock = new FakeClock();
        manager = newManager(handler);
    }

    private LiveUpdateManager newManager(LiveUpdateHandler h) {
        return new LiveUpdateManager(context, h, new LiveUpdateStore(prefs), topics, events, clock);
    }

    private static JSONObject envelope(String event, long timestamp, String status) throws JSONException {
        return new JSONObject()
                .put("event", event)
                .put("name", NAME)
                .put("type", TYPE)
                .put("timestamp", timestamp)
                .put("content_state", new JSONObject().put("status", status));
    }

    private static JSONObject start(long timestamp) throws JSONException {
        return envelope("start", timestamp, "Kick-off").put("topic", TOPIC);
    }

    private static JSONObject update(long timestamp, String status) throws JSONException {
        return envelope("update", timestamp, status);
    }

    private static JSONObject end(long timestamp) throws JSONException {
        return envelope("end", timestamp, "Full time");
    }

    private static String status(LiveUpdate liveUpdate) {
        return liveUpdate.getContentState().optString("status");
    }

    @Test
    public void startPersistsSubscribesDispatchesAndTracks() throws JSONException {
        manager.handlePush(start(T0));

        assertEquals(Arrays.asList(LiveUpdateEvent.START), handler.events);
        assertEquals("Kick-off", status(handler.last()));
        assertEquals(Arrays.asList(TOPIC), topics.subscribed);
        assertEquals(1, manager.getActive().size());
        assertEquals(Arrays.asList(AnalyticsContract.EVENT_TYPE_LIVE_ACTIVITY_STARTED), events.types);
        assertEquals(NAME, events.props.get(0).getString(LiveUpdateManager.PROP_ACTIVITY_ID));
        assertEquals(TYPE, events.props.get(0).getString(LiveUpdateManager.PROP_TYPE));

        manager.handlePush(start(T0 + 5));
        assertEquals(1, handler.events.size());
    }

    @Test
    public void updatesApplyUnlessOlderThanTheLastApplied() throws JSONException {
        manager.handlePush(start(T0));

        manager.handlePush(update(T0 + 60, "1-0"));
        manager.handlePush(update(T0 + 60, "1-0 again"));
        manager.handlePush(update(T0 + 30, "stale"));

        assertEquals(Arrays.asList(LiveUpdateEvent.START, LiveUpdateEvent.UPDATE, LiveUpdateEvent.UPDATE), handler.events);
        assertEquals("1-0 again", status(manager.getActive().get(0)));
        assertEquals(1, events.types.size());
    }

    @Test
    public void updateForUnknownNameIsIgnored() throws JSONException {
        manager.handlePush(update(T0, "1-0"));

        assertTrue(handler.events.isEmpty());
        assertTrue(topics.subscribed.isEmpty());
        assertFalse(prefs.contains(SharedPrefs.KEY_LIVE_UPDATES));
    }

    @Test
    public void endUnsubscribesBlocksLaterMessagesAndAllowsANewerStart() throws JSONException {
        manager.handlePush(start(T0));

        manager.handlePush(end(T0 + 100).put("dismissal_date", T0 + 200));

        assertEquals(LiveUpdateEvent.END, handler.events.get(1));
        assertEquals(LiveUpdateEndReason.SERVER, handler.last().getEndReason());
        assertEquals("Full time", status(handler.last()));
        assertEquals(Long.valueOf((T0 + 200) * 1000L), handler.last().getDismissalDateMillis());
        assertEquals(Arrays.asList(TOPIC), topics.unsubscribed);
        assertEquals(AnalyticsContract.EVENT_TYPE_LIVE_ACTIVITY_ENDED, events.types.get(1));
        assertEquals("server", events.lastReason());
        assertTrue(manager.getActive().isEmpty());

        manager.handlePush(update(T0 + 200, "ghost"));
        manager.handlePush(end(T0 + 300));
        manager.handlePush(start(T0 + 100));
        assertEquals(2, handler.events.size());

        manager.handlePush(start(T0 + 101));
        assertEquals(LiveUpdateEvent.START, handler.events.get(2));
    }

    @Test
    public void localEndAndDismissalEndWithTheRightReason() throws JSONException {
        manager.handlePush(start(T0));
        manager.end(NAME);
        assertEquals(LiveUpdateEndReason.LOCAL, handler.last().getEndReason());
        assertEquals("local", events.lastReason());
        manager.handlePush(update(T0 + 10, "ghost"));
        assertEquals(2, handler.events.size());

        manager.handlePush(start(T0 + 20));
        clock.now += LiveUpdateManager.INACTIVITY_MS + 1;
        manager.endFromNotificationDismissal(NAME);
        assertEquals(LiveUpdateEndReason.EXPIRED, handler.last().getEndReason());
        assertEquals("expired", events.lastReason());

        manager.endFromNotificationDismissal(NAME);
        manager.end(NAME);
        assertEquals(4, handler.events.size());
    }

    @Test
    public void sweepExpiresInactiveActivitiesAndPrunesOldEndedRecords() throws JSONException {
        manager.handlePush(start(T0));
        manager.handlePush(start(T0).put("name", "finished").put("topic", "lu_finished"));
        manager.handlePush(end(T0 + 1).put("name", "finished"));
        clock.now += LiveUpdateStore.ENDED_RETENTION_MS + 1;

        manager.sweep();

        assertEquals(LiveUpdateEndReason.EXPIRED, handler.last().getEndReason());
        assertEquals(Arrays.asList("lu_finished", TOPIC), topics.unsubscribed);
        assertTrue(manager.getActive().isEmpty());
        assertNull(new LiveUpdateStore(prefs).getEnded("finished"));
    }

    @Test
    public void stateSurvivesANewManagerOnTheSamePrefs() throws JSONException {
        manager.handlePush(start(T0));
        manager.handlePush(start(T0).put("name", "finished").put("topic", "lu_finished"));
        manager.handlePush(end(T0 + 1).put("name", "finished"));

        RecordingHandler second = new RecordingHandler();
        LiveUpdateManager reloaded = newManager(second);
        reloaded.handlePush(update(T0 + 60, "1-0"));
        reloaded.handlePush(update(T0 + 60, "ghost").put("name", "finished"));

        assertEquals(Arrays.asList(LiveUpdateEvent.UPDATE), second.events);
        assertEquals(NOW, second.last().getStartedAtMillis());
    }

    @Test
    public void handlerThrowingDoesNotLoseStateOrTheEvent() throws JSONException {
        handler.throwOnCall = true;

        manager.handlePush(start(T0));

        assertEquals(1, manager.getActive().size());
        assertEquals(1, events.types.size());
    }

    @Test
    public void clearAllEndsEverythingLocallyAndForgetsEndedNames() throws JSONException {
        manager.handlePush(start(T0));
        manager.handlePush(start(T0).put("name", "other").put("topic", "lu_other"));

        assertEquals(Arrays.asList(NAME, "other"), manager.clearAll());

        assertEquals(LiveUpdateEndReason.CLEARED, handler.last().getEndReason());
        assertEquals("cleared", events.lastReason());
        assertEquals(Arrays.asList(TOPIC, "lu_other"), topics.unsubscribed);
        assertFalse(prefs.contains(SharedPrefs.KEY_LIVE_UPDATES));

        manager.handlePush(start(T0));
        assertEquals(LiveUpdateEvent.START, handler.events.get(handler.events.size() - 1));
    }

    @Test
    public void malformedEnvelopeIsDropped() throws JSONException {
        manager.handlePush("garbage");
        manager.handlePush(start(T0).put("event", "pause"));

        assertTrue(handler.events.isEmpty());
        assertTrue(manager.getActive().isEmpty());
    }
}
