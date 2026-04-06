package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

class OverlayMessagingSessionManager implements AppStateWatcher.AppStateChangedListener {

    interface Listener {
        @UiThread
        void onSessionStarted();
    }

    private static final long SCHEDULE_BUFFER_MS = 1_000L;
    private static final String PREFS_FILE = "optimove_overlay_messaging";
    private static final String KEY_LAST_SESSION_START = "last_session_start";

    private final Handler handler;
    private final long sessionLengthMs;
    private final Listener listener;
    private final SharedPreferences prefs;
    private boolean appInForeground;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            startNewSession();
            scheduleNextTick();
        }
    };

    OverlayMessagingSessionManager(@NonNull Context context, long sessionLengthHours, @NonNull Listener listener) {
        this.handler = new Handler(Looper.getMainLooper());
        this.sessionLengthMs = sessionLengthHours * 3_600_000L;
        this.listener = listener;
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        OptimobileInitProvider.getAppStateWatcher().registerListener(this);
    }

    @UiThread
    void resetSession() {
        prefs.edit().remove(KEY_LAST_SESSION_START).apply();
        if (!appInForeground) {
            return;
        }
        handler.removeCallbacks(ticker);
        startNewSession();
        scheduleNextTick();
    }

    @Override
    @UiThread
    public void appEnteredForeground() {
        appInForeground = true;
        long lastSessionStart = prefs.getLong(KEY_LAST_SESSION_START, 0L);
        boolean noPreviousSession = lastSessionStart == 0L;
        boolean sessionExpired = (System.currentTimeMillis() - lastSessionStart) >= sessionLengthMs;

        if (noPreviousSession || sessionExpired) {
            startNewSession();
        }
        scheduleNextTick();
    }

    @Override
    @UiThread
    public void appEnteredBackground() {
        appInForeground = false;
        handler.removeCallbacks(ticker);
    }

    private void scheduleNextTick() {

        long lastSessionStart = prefs.getLong(KEY_LAST_SESSION_START, 0L);
        long nextSessionAt = lastSessionStart + sessionLengthMs + SCHEDULE_BUFFER_MS;
        long delay = Math.max(0, nextSessionAt - System.currentTimeMillis());
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, delay);
    }

    private void startNewSession() {
        prefs.edit().putLong(KEY_LAST_SESSION_START, System.currentTimeMillis()).apply();
        listener.onSessionStarted();
    }


    @Override
    public void activityAvailable(@NonNull Activity activity) { /* noop */ }

    @Override
    public void activityUnavailable(@NonNull Activity activity) { /* noop */ }
}
