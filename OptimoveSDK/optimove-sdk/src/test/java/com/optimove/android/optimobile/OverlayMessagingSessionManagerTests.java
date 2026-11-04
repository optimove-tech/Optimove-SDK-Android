package com.optimove.android.optimobile;

import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.time.Duration;

@RunWith(RobolectricTestRunner.class)
public class OverlayMessagingSessionManagerTests {

    private static final long SECOND_MS = 1_000L;
    private static final long MINUTE_MS = 60 * SECOND_MS;
    private static final long HOUR_MS = 60 * MINUTE_MS;
    private static final long DAY_MS = 24 * HOUR_MS;
    private static final long SETTLE_MS = 10 * SECOND_MS;
    // 2026-10-28T10:00:00Z
    private static final long TEN_AM = 1_793_181_600_000L;

    private Context context;
    private SharedPreferences prefs;
    private long now;
    private int fetches;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences("optimove_overlay_messaging", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        now = TEN_AM;
        fetches = 0;
    }

    private OverlayMessagingSessionManager makeManager(long sessionLengthMinutes) {
        return new OverlayMessagingSessionManager(context, sessionLengthMinutes, 60, () -> fetches++, () -> now);
    }

    private void advance(long ms) {
        now += ms;
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms));
    }

    /**
     * A manager in the foreground whose first fetch, at 10:00, returned {@code nextFetchInSeconds} and a message the
     * caller is still handling: the slot is taken until {@code release()}.
     */
    private OverlayMessagingSessionManager fetching(long nextFetchInSeconds) {
        return fetching(nextFetchInSeconds, 60);
    }

    private OverlayMessagingSessionManager fetching(long nextFetchInSeconds, long sessionLengthMinutes) {
        OverlayMessagingSessionManager manager = makeManager(sessionLengthMinutes);
        manager.appEnteredForeground();
        Assert.assertEquals(1, fetches);
        manager.onFetchResult(nextFetchInSeconds);
        fetches = 0;
        return manager;
    }

    /** As {@code fetching}, with nothing to show: the slot is free again. */
    private OverlayMessagingSessionManager fetched(long nextFetchInSeconds) {
        OverlayMessagingSessionManager manager = fetching(nextFetchInSeconds);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        return manager;
    }

    /** As {@code fetching}, with the message displayed at 10:00 and still open. */
    private OverlayMessagingSessionManager displayed(long nextFetchInSeconds) {
        return displayed(nextFetchInSeconds, 60);
    }

    private OverlayMessagingSessionManager displayed(long nextFetchInSeconds, long sessionLengthMinutes) {
        OverlayMessagingSessionManager manager = fetching(nextFetchInSeconds, sessionLengthMinutes);
        manager.onDisplayed();
        return manager;
    }

    /** As {@code fetching}, with the message not shown. */
    private OverlayMessagingSessionManager deferred(long nextFetchInSeconds) {
        OverlayMessagingSessionManager manager = fetching(nextFetchInSeconds);
        manager.release(OverlayMessagingSessionManager.Outcome.DEFERRED);
        return manager;
    }

    // ========================= session =========================

    @Test
    public void aDisplayStartsTheSessionAndHoldsTheNextFetchUntilItEnds() {
        OverlayMessagingSessionManager manager = displayed(0);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);

        advance(HOUR_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
        Assert.assertEquals(TEN_AM, prefs.getLong("last_session_start", 0));
        Assert.assertEquals(1, prefs.getInt("session_count", 0));
    }

    @Test
    public void noSessionStartsWhenNothingIsDisplayed() {
        fetching(0).release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);

        Assert.assertEquals(1, fetches);
        Assert.assertFalse(prefs.contains("last_session_start"));
    }

    @Test
    public void theSessionSurvivesARestart() {
        displayed(0);

        advance(2 * MINUTE_MS);
        makeManager(60).appEnteredForeground();
        Assert.assertEquals(0, fetches);

        advance(HOUR_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aStartWithoutACountFromEarlierVersionsHasRoom() {
        prefs.edit().putLong("last_session_start", TEN_AM - MINUTE_MS).commit();

        makeManager(60).appEnteredForeground();

        Assert.assertEquals(1, fetches);
    }

    // ========================= slot =========================

    @Test
    public void fetchesOneAtATimeEvenWhenDue() {
        OverlayMessagingSessionManager manager = fetching(0);
        manager.onSignal(0);
        manager.appEnteredBackground();
        manager.appEnteredForeground();
        advance(DAY_MS);
        Assert.assertEquals(0, fetches);

        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void onReleaseKeepsAPlannedTimeThatIsStillAhead() {
        fetched(600);
        Assert.assertEquals(0, fetches);

        advance(600 * SECOND_MS);
        Assert.assertEquals(1, fetches);
    }

    // ========================= fetch triggers =========================

    @Test
    public void fetchesOnEnteringTheForeground() {
        makeManager(60).appEnteredForeground();
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void doesNotFetchInTheBackground() {
        OverlayMessagingSessionManager manager = fetched(600);
        manager.appEnteredBackground();
        advance(DAY_MS);
        Assert.assertEquals(0, fetches);
    }

    @Test
    public void fetchesOnReturningToTheForegroundAfterThePlannedTime() {
        OverlayMessagingSessionManager manager = fetched(600);
        manager.appEnteredBackground();
        advance(20 * MINUTE_MS);

        manager.appEnteredForeground();
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void keepsThePlannedTimeOnReturningToTheForegroundBeforeIt() {
        OverlayMessagingSessionManager manager = fetched(600);
        manager.appEnteredBackground();
        advance(5 * MINUTE_MS);
        manager.appEnteredForeground();
        Assert.assertEquals(0, fetches);

        advance(5 * MINUTE_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void resetEndsTheSessionAndFetches() {
        OverlayMessagingSessionManager manager = displayed(3600);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);

        manager.reset();

        Assert.assertFalse(prefs.contains("last_session_start"));
        Assert.assertFalse(prefs.contains("session_count"));
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void resetWhileTheSlotIsFullFetchesAfterRelease() {
        OverlayMessagingSessionManager manager = displayed(3600);
        manager.reset();
        Assert.assertEquals(0, fetches);

        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void resetDuringTheFetchIsNotUndoneByItsResult() {
        OverlayMessagingSessionManager manager = makeManager(60);
        manager.appEnteredForeground();
        fetches = 0;

        manager.reset();
        manager.onFetchResult(3600);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);
        Assert.assertEquals(1, fetches);
    }

    // ========================= planned fetch time =========================

    @Test
    public void plansNextFetchInSecondsAhead() {
        fetched(2040);

        advance(2040 * SECOND_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void afterDoneWaitsTheSettleDelayEvenFor0() {
        fetching(0).release(OverlayMessagingSessionManager.Outcome.DONE);

        advance(SETTLE_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void waitsForTheSessionEndWhenFullEvenIfTheNextMessageIsDueSooner() {
        displayed(600).release(OverlayMessagingSessionManager.Outcome.DONE);

        advance(HOUR_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void waitsForNextFetchInSecondsWhenLaterThanTheSessionEnd() {
        displayed(3 * 3600).release(OverlayMessagingSessionManager.Outcome.DONE);

        advance(3 * HOUR_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void fetchesAtCloseWhenTheMessageStayedOpenPastTheSessionEnd() {
        OverlayMessagingSessionManager manager = displayed(0);
        advance(90 * MINUTE_MS);
        Assert.assertEquals(0, fetches);

        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void withSessionLength0ADueFetchRunsAtTheClose() {
        OverlayMessagingSessionManager manager = displayed(0, 0);

        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        advance(SETTLE_MS);
        Assert.assertEquals(1, fetches);
    }

    // ========================= scheduled Signals =========================

    @Test
    public void aSignalMovesThePlannedFetchEarlier() {
        OverlayMessagingSessionManager manager = fetched(20 * 3600);

        manager.onSignal(3600);
        Assert.assertEquals(0, fetches);

        advance(HOUR_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalFor0FetchesStraightAway() {
        fetched(20 * 3600).onSignal(0);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalIsIgnoredWhenThePlannedFetchIsAlreadyEarlier() {
        OverlayMessagingSessionManager manager = fetched(600);
        manager.onSignal(3600);

        advance(600 * SECOND_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalDuringTheSettleDelayFetchesStraightAway() {
        OverlayMessagingSessionManager manager = fetched(20 * 3600);
        manager.onSignal(0);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalDuringTheFetchWinsOverALaterResult() {
        OverlayMessagingSessionManager manager = makeManager(60);
        manager.appEnteredForeground();
        fetches = 0;

        manager.onSignal(600);
        manager.onFetchResult(20 * 3600);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);
        Assert.assertEquals(0, fetches);

        advance(600 * SECOND_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalDuringAFullSessionBringsTheFetchToTheSessionEnd() {
        OverlayMessagingSessionManager manager = displayed(20 * 3600);
        manager.release(OverlayMessagingSessionManager.Outcome.DONE);

        manager.onSignal(0);
        Assert.assertEquals(0, fetches);

        advance(HOUR_MS);
        Assert.assertEquals(1, fetches);
    }

    // ========================= defer delay =========================

    @Test
    public void deferredIgnoresTheServers0AndWaitsTheDelay() {
        deferred(0);

        advance(HOUR_MS - 1);
        Assert.assertEquals(0, fetches);
        advance(1);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void deferredKeepsAServerTimeThatIsSooner() {
        deferred(600);
        advance(600 * SECOND_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void deferredCutsALaterServerTimeToTheDelay() {
        deferred(3 * 3600);
        advance(HOUR_MS);
        Assert.assertEquals(1, fetches);
    }

    @Test
    public void aSignalEndsTheDeferDelayEarly() {
        OverlayMessagingSessionManager manager = deferred(0);
        advance(10 * MINUTE_MS);

        manager.onSignal(300);
        advance(5 * MINUTE_MS);
        Assert.assertEquals(1, fetches);
    }
}
