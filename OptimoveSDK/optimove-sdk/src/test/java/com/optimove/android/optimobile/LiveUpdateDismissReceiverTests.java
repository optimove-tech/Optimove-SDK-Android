package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LiveUpdateDismissReceiverTests {

    private final List<LiveUpdate> ended = new ArrayList<>();
    private final List<Thread> handlerThreads = new ArrayList<>();
    private final LiveUpdateHandler handler = new LiveUpdateHandler() {
        @Override
        public void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate) {
            handlerThreads.add(Thread.currentThread());
            if (event == LiveUpdateEvent.END) {
                ended.add(liveUpdate);
            }
        }
    };
    private final LiveUpdateDismissReceiver receiver = new LiveUpdateDismissReceiver();
    private Application application;
    private ExecutorService executor;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE).edit().clear().commit();
        executor = Executors.newSingleThreadExecutor();
        Optimobile.executorService = executor;
        OptimoveLiveUpdates.reset();
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
        Optimobile.executorService = null;
        OptimoveLiveUpdates.reset();
    }

    @Test
    public void dismissalEndsTheLiveUpdateLocallyOffTheCallingThread() throws Exception {
        OptimoveLiveUpdatesTests.initializeWith(application, handler);
        OptimoveLiveUpdates.handlePushPayload(OptimoveLiveUpdatesTests.startPayload());

        receiver.onReceive(application, new Intent().putExtra(LiveUpdateDismissReceiver.EXTRA_NAME, "match"));
        drain();

        assertEquals(1, ended.size());
        assertEquals(LiveUpdateEndReason.LOCAL, ended.get(0).getEndReason());
        assertNotSame(Thread.currentThread(), handlerThreads.get(handlerThreads.size() - 1));
        assertTrue(OptimoveLiveUpdates.getInstance().getActiveLiveUpdates().isEmpty());
    }

    @Test
    public void dismissalBeforeInitializeOrWithoutANameDoesNotThrow() {
        receiver.onReceive(application, new Intent().putExtra(LiveUpdateDismissReceiver.EXTRA_NAME, "match"));
        receiver.onReceive(application, new Intent());
        receiver.onReceive(application, null);
    }

    private void drain() throws Exception {
        executor.submit(() -> { }).get(5, TimeUnit.SECONDS);
    }
}
