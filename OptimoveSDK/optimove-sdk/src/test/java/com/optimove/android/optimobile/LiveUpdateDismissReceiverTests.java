package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
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
public class LiveUpdateDismissReceiverTests {

    private final List<LiveUpdate> ended = new ArrayList<>();
    private final LiveUpdateHandler handler = new LiveUpdateHandler() {
        @Override
        public void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate) {
            if (event == LiveUpdateEvent.END) {
                ended.add(liveUpdate);
            }
        }
    };
    private final LiveUpdateDismissReceiver receiver = new LiveUpdateDismissReceiver();
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE).edit().clear().commit();
        OptimoveLiveUpdates.reset();
    }

    @After
    public void tearDown() {
        OptimoveLiveUpdates.reset();
    }

    @Test
    public void dismissalEndsTheLiveUpdateLocally() throws JSONException {
        OptimoveLiveUpdatesTests.initializeWith(application, handler);
        OptimoveLiveUpdates.handlePushPayload(OptimoveLiveUpdatesTests.startPayload());

        receiver.onReceive(application, new Intent().putExtra(LiveUpdateDismissReceiver.EXTRA_NAME, "match"));

        assertEquals(1, ended.size());
        assertEquals(LiveUpdateEndReason.LOCAL, ended.get(0).getEndReason());
        assertTrue(OptimoveLiveUpdates.getInstance().getActiveLiveUpdates().isEmpty());
    }

    @Test
    public void dismissalBeforeInitializeOrWithoutANameDoesNotThrow() {
        receiver.onReceive(application, new Intent().putExtra(LiveUpdateDismissReceiver.EXTRA_NAME, "match"));
        receiver.onReceive(application, new Intent());
        receiver.onReceive(application, null);
    }
}
