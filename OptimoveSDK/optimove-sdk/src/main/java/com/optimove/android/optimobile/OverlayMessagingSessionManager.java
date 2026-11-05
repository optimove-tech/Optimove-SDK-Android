package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;

import java.util.HashSet;
import java.util.Set;

/**
 * Owns scheduled fetching: decides when the next one is due, and runs at most one at a time. Inputs:
 * - the session: started by a displayed message when none is active, lasting the session length, holding at most
 * one display. Persisted so it survives restarts: the start under the key earlier versions used for it, and the
 * count next to it. Nothing but a display starts or fills it.
 * - {@code nextFetchInSeconds} from a fetch (the server's, or the request service's backoff after a failure) or a
 * Signal. The server already caps it, so there is no client-side refresh cap.
 * - the slot: one place, taken when {@code onFetchDue} is called, until the caller calls {@link #release} once that
 * message is done with. Nothing is fetched while it's full; {@link #release} then fetches if the planned time has
 * passed.
 * <p>
 * The planned fetch is the later of the two times when the session is full. {@code onFetchDue} is called when it's
 * due, the slot has room and the app is in the foreground. Planning starts once registered with
 * {@link AppStateWatcher}, which reports the foreground straight away.
 */
class OverlayMessagingSessionManager implements AppStateWatcher.AppStateChangedListener {

    interface Listener {
        @UiThread
        void onFetchDue();
    }

    @VisibleForTesting
    interface Clock {
        long now();
    }

    /**
     * How a scheduled fetch ended, for {@link #release}:
     * - DONE: the message was closed or discarded (the server deletes it), or nothing was fetched; the server's next
     * time stands, but no sooner than the settle delay, so a close or discard has time to reach the server.
     * - DEFERRED: the message is still on the server and wasn't shown (deferred, timed out, the interceptor threw, it
     * failed to render, or it was already seen); wait the defer delay, so the same message doesn't come straight back.
     */
    enum Outcome {
        DONE, DEFERRED
    }

    private static final long MS_PER_SECOND = 1_000L;
    private static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
    private static final int DISPLAYS_PER_SESSION = 1;
    private static final int SLOT_CAPACITY = 1;
    // After DONE, best effort time for the server to apply a close or discard before the next fetch
    private static final long SETTLE_DELAY_MS = 10 * MS_PER_SECOND;
    private static final String PREFS_FILE = "optimove_overlay_messaging";
    private static final String KEY_SESSION_START = "last_session_start";
    private static final String KEY_SESSION_COUNT = "session_count";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SharedPreferences prefs;
    private final Listener listener;
    private final Clock clock;
    private final long durationMs;
    private final long deferDelayMs;
    // When the server (or a Signal) says to fetch next; 0 means straight away, as at start, and
    // Long.MAX_VALUE not yet known, while a fetch is in flight
    private long nextFetchAt = 0;
    // The display slot: scheduled fetches, or the messages they returned, in progress
    private int slotCount = 0;
    // Displayed messages counted toward the session and still on screen. When the activity is recreated, the view is
    // rebuilt and reports the same message as displayed again; this keeps it from counting twice toward the session limit
    private final Set<Long> countedMessageIds = new HashSet<>();
    private boolean appInForeground = false;

    private final Runnable planRunnable = this::plan;

    OverlayMessagingSessionManager(@NonNull Context context, long sessionLengthMinutes, long deferDelayMinutes,
                                   @NonNull Listener listener) {
        this(context, sessionLengthMinutes, deferDelayMinutes, listener, System::currentTimeMillis);
    }

    @VisibleForTesting
    OverlayMessagingSessionManager(@NonNull Context context, long sessionLengthMinutes, long deferDelayMinutes,
                                   @NonNull Listener listener, @NonNull Clock clock) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        this.durationMs = sessionLengthMinutes * MS_PER_MINUTE;
        this.deferDelayMs = deferDelayMinutes * MS_PER_MINUTE;
        this.listener = listener;
        this.clock = clock;
    }

    /**
     * The result of a scheduled fetch: when to fetch next. A Signal or {@link #reset} during the fetch keeps its time
     * if it's sooner: the server may have read the messages before the Signal's one was created.
     */
    @UiThread
    void onFetchResult(long nextFetchInSeconds) {
        nextFetchAt = Math.min(nextFetchAt, clock.now() + nextFetchInSeconds * MS_PER_SECOND);
        plan();
    }

    /**
     * A scheduled message was displayed: starts a session if none is active, and counts toward it, once while it stays
     * on screen.
     */
    @UiThread
    void onDisplayed(long messageId) {
        if (!countedMessageIds.add(messageId)) {
            return;
        }
        Session session = activeSession();
        writeSession(session == null ? new Session(clock.now(), 1) : new Session(session.start, session.count + 1));
        plan();
    }

    /**
     * The scheduled fetch started by {@code onFetchDue}, and its message, are done with: frees the slot and fetches if
     * the planned time has passed. On DEFERRED it first holds back for the defer delay, unless the server's next time
     * is still ahead and sooner (a different message is due then). The server's 0 ("more due") has passed by then and
     * is ignored: fetching at once would return the same message again. On DONE it holds back for the settle delay at
     * least. {@code displayedMessageId} is the message, if it was displayed: a later display of it counts again.
     */
    @UiThread
    void release(@NonNull Outcome outcome, @Nullable Long displayedMessageId) {
        if (displayedMessageId != null) {
            countedMessageIds.remove(displayedMessageId);
        }
        slotCount = Math.max(0, slotCount - 1);
        long now = clock.now();
        switch (outcome) {
            case DEFERRED:
                long end = now + deferDelayMs;
                nextFetchAt = nextFetchAt > now ? Math.min(nextFetchAt, end) : end;
                break;
            case DONE:
                nextFetchAt = Math.max(nextFetchAt, now + SETTLE_DELAY_MS);
                break;
        }
        plan();
    }

    /** A scheduled Signal: moves the next fetch earlier. During a full session the fetch still waits for the session end. */
    @UiThread
    void onSignal(long nextFetchInSeconds) {
        nextFetchAt = Math.min(nextFetchAt, clock.now() + nextFetchInSeconds * MS_PER_SECOND);
        plan();
    }

    /** Ends the session and fetches, for {@code resetSession()} and user change. */
    @UiThread
    void reset() {
        prefs.edit().remove(KEY_SESSION_START).remove(KEY_SESSION_COUNT).apply();
        nextFetchAt = 0;
        plan();
    }

    /**
     * Fetches now if the planned time has passed, otherwise sets the timer for it. Nothing in the background, or while
     * the slot is full: {@link #release} plans again.
     */
    private void plan() {
        handler.removeCallbacks(planRunnable);
        if (!appInForeground || slotCount >= SLOT_CAPACITY) {
            return;
        }
        long delay = plannedFetchAt() - clock.now();
        if (delay <= 0) {
            slotCount++;
            // The time that made this fetch due has passed; until the result, only a Signal or reset() sets it
            nextFetchAt = Long.MAX_VALUE;
            listener.onFetchDue();
            return;
        }
        handler.postDelayed(planRunnable, delay);
    }

    private long plannedFetchAt() {
        Session session = activeSession();
        if (session == null || session.count < DISPLAYS_PER_SESSION) {
            return nextFetchAt;
        }
        return Math.max(nextFetchAt, session.start + durationMs);
    }

    /** A start without a count, as earlier versions stored it, is a session with no display yet. */
    @Nullable
    private Session activeSession() {
        long start = prefs.getLong(KEY_SESSION_START, 0L);
        if (start == 0L || clock.now() >= start + durationMs) {
            return null;
        }
        return new Session(start, prefs.getInt(KEY_SESSION_COUNT, 0));
    }

    private void writeSession(Session session) {
        prefs.edit().putLong(KEY_SESSION_START, session.start).putInt(KEY_SESSION_COUNT, session.count).apply();
    }

    private static final class Session {
        final long start;
        final int count;

        Session(long start, int count) {
            this.start = start;
            this.count = count;
        }
    }

    //==============================================================================================
    //-- AppStateChangedListener

    @Override
    @UiThread
    public void appEnteredForeground() {
        appInForeground = true;
        plan();
    }

    @Override
    @UiThread
    public void appEnteredBackground() {
        appInForeground = false;
        handler.removeCallbacks(planRunnable);
    }

    @Override
    public void activityAvailable(@NonNull Activity activity) { /* noop */ }

    @Override
    public void activityUnavailable(@NonNull Activity activity) { /* noop */ }
}
