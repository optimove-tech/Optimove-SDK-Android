package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.optimove.android.OptimoveConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OptimoveLiveUpdatesTests {

    private static final class RecordingHandler implements LiveUpdateHandler {
        final List<LiveUpdateEvent> events = new ArrayList<>();

        @Override
        public void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate) {
            events.add(event);
        }
    }

    private Application application;
    private RecordingHandler handler;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE).edit().clear().commit();
        handler = new RecordingHandler();
        OptimoveLiveUpdates.reset();
    }

    @After
    public void tearDown() {
        OptimoveLiveUpdates.reset();
    }

    static void initializeWith(Application application, LiveUpdateHandler handler) {
        LiveUpdateStore store = new LiveUpdateStore(
                application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE));
        TopicSubscriber topics = new TopicSubscriber() {
            @Override
            public void subscribe(@NonNull String topic) {
            }

            @Override
            public void unsubscribe(@NonNull String topic) {
            }
        };
        OptimoveLiveUpdates.initialize(application, new LiveUpdateManager(
                application, handler, store, topics, (type, props) -> { }, System::currentTimeMillis));
    }

    static String startPayload() throws JSONException {
        JSONObject start = new JSONObject()
                .put("event", "start")
                .put("name", "match")
                .put("type", "SportsActivityAttributes")
                .put("timestamp", 1786024800L)
                .put("topic", "lu_1")
                .put("content_state", new JSONObject());
        return new JSONObject().put("a", new JSONObject().put(LiveUpdateEnvelope.KEY, start)).toString();
    }

    @Test
    public void liveUpdatesAreRoutedToTheHandlerAndNormalPushesAreNot() throws JSONException {
        initializeWith(application, handler);
        String normalPush = new JSONObject()
                .put("a", new JSONObject().put("k.message", new JSONObject().put("data", new JSONObject().put("id", 1))))
                .toString();

        assertFalse(OptimoveLiveUpdates.handlePushPayload(normalPush));
        assertTrue(OptimoveLiveUpdates.handlePushPayload(startPayload()));

        assertEquals(1, handler.events.size());
        assertEquals("match", OptimoveLiveUpdates.getInstance().getActiveLiveUpdates().get(0).getName());
    }

    @Test
    public void liveUpdatesAreConsumedButDroppedWhenNotInitializedOrNotEnabled() throws JSONException {
        assertTrue(OptimoveLiveUpdates.handlePushPayload(startPayload()));

        OptimoveConfig config = new OptimoveConfig.Builder(new OptimoveConfig.FeatureSet().withOptimobile()).build();
        OptimoveLiveUpdates.initialize(application, config);
        assertTrue(OptimoveLiveUpdates.handlePushPayload(startPayload()));

        OptimoveLiveUpdates instance = OptimoveLiveUpdates.getInstance();
        assertFalse(instance.isEnabled());
        assertTrue(instance.getActiveLiveUpdates().isEmpty());
        instance.end("match");
        instance.clearAll();
    }
}
