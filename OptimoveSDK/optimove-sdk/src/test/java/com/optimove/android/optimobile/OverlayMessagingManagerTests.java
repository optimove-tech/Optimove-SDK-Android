package com.optimove.android.optimobile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

@RunWith(RobolectricTestRunner.class)
public class OverlayMessagingManagerTests {

    private static final OverlayMessagingSessionManager.Outcome DONE = OverlayMessagingSessionManager.Outcome.DONE;
    private static final OverlayMessagingSessionManager.Outcome DEFERRED = OverlayMessagingSessionManager.Outcome.DEFERRED;

    /** An interceptor that answers each message the same way, and counts how often it was asked. */
    private static final class Interceptor implements OptimoveOverlayMessaging.OverlayMessagingInterceptor {
        interface Answer {
            void answer(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback callback);
        }

        private final Answer answer;
        int calls = 0;

        Interceptor(Answer answer) {
            this.answer = answer;
        }

        @Override
        public void onMessageLoaded(@NonNull OverlayMessagingMessage message,
                                    @NonNull OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback callback) {
            calls++;
            answer.answer(callback);
        }

        @Override
        public long getTimeoutMs() {
            return 1;
        }
    }

    private MockedStatic<OverlayMessagingRequestService> requestService;
    private MockedStatic<Optimobile> optimobile;
    private MockedStatic<OptimobileInitProvider> initProvider;
    private ExecutorService previousExecutor;
    private final List<String> events = new ArrayList<>();
    private OverlayMessagingSessionManager session;
    private OverlayMessagingManager manager;

    @Before
    public void setUp() {
        requestService = mockStatic(OverlayMessagingRequestService.class);
        scheduledReturns(null, 3600);

        // Static methods only: Optimobile.handler, a field, stays the real main-thread handler
        optimobile = mockStatic(Optimobile.class);
        optimobile.when(() -> Optimobile.trackEventImmediately(any(), anyString(), any()))
                .thenAnswer(inv -> events.add(inv.getArgument(1) + ":" + ((JSONObject) inv.getArgument(2)).optString("outcome")));

        initProvider = mockStatic(OptimobileInitProvider.class);
        initProvider.when(OptimobileInitProvider::getAppStateWatcher).thenReturn(mock(AppStateWatcher.class));

        // Fetches run straight away on the test thread
        previousExecutor = Optimobile.executorService;
        Optimobile.executorService = mock(ExecutorService.class);
        when(Optimobile.executorService.submit(any(Runnable.class))).thenAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        });

        session = mock(OverlayMessagingSessionManager.class);
        manager = new OverlayMessagingManager(ApplicationProvider.getApplicationContext(), session);
    }

    @After
    public void tearDown() {
        Optimobile.executorService = previousExecutor;
        initProvider.close();
        optimobile.close();
        requestService.close();
    }

    private static OverlayMessagingMessage message(OverlayMessagingMessage.MessageType type) throws JSONException {
        return new OverlayMessagingMessage(1, new JSONObject("{\"ver\":1,\"html\":\"<p>hi</p>\"}"), null, type);
    }

    private void scheduledReturns(@Nullable OverlayMessagingMessage message, long nextFetchInSeconds) {
        requestService.when(() -> OverlayMessagingRequestService.readScheduledMessage(any()))
                .thenReturn(new OverlayMessagingRequestService.ScheduledFetchResult(message, nextFetchInSeconds));
    }

    private void immediateReturns(@Nullable OverlayMessagingMessage message) {
        requestService.when(() -> OverlayMessagingRequestService.readOverlayMessage(any(), any())).thenReturn(message);
    }

    /** The session manager found a scheduled fetch due, and took its slot. */
    private void triggerScheduled() {
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.SESSION);
        idleMain();
    }

    private static void idleMain() {
        shadowOf(Looper.getMainLooper()).idle();
    }

    /** Waits for the interceptor timeout, which fires on its own thread and posts to the main thread. */
    private void awaitRelease() throws InterruptedException {
        for (int i = 0; i < 200; i++) {
            idleMain();
            boolean released = mockingDetails(session).getInvocations().stream()
                    .anyMatch(inv -> inv.getMethod().getName().equals("release"));
            if (released) {
                return;
            }
            Thread.sleep(10);
        }
        Assert.fail("No release within 2 s");
    }

    private static List<String> intercepted(String outcome) {
        return Collections.singletonList(AnalyticsContract.EVENT_TYPE_OM_INTERCEPTED + ":" + outcome);
    }

    // ========================= scheduled fetch =========================

    @Test
    public void reportsNextFetchAndReleasesDoneWhenNothingIsReturned() {
        scheduledReturns(null, 2040);

        triggerScheduled();

        InOrder order = inOrder(session);
        order.verify(session).onFetchResult(2040);
        order.verify(session).release(DONE);
        requestService.verify(() -> OverlayMessagingRequestService.readOverlayMessage(any(), any()), never());
    }

    @Test
    public void releasesDoneOnADiscardAfterTrackingIt() throws JSONException {
        manager.setInterceptor(new Interceptor(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback::discard));
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);

        triggerScheduled();

        verify(session).release(DONE);
        verify(session, never()).release(DEFERRED);
        Assert.assertEquals(intercepted("discard"), events);
    }

    @Test
    public void releasesDeferredOnADeferAndOffersTheMessageAgain() throws JSONException {
        Interceptor interceptor = new Interceptor(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback::defer);
        manager.setInterceptor(interceptor);
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);

        triggerScheduled();
        verify(session).release(DEFERRED);
        verify(session, never()).release(DONE);

        triggerScheduled();
        Assert.assertEquals(2, interceptor.calls);
    }

    @Test
    public void releasesDeferredOnATimeoutAndOffersTheMessageAgain() throws Exception {
        Interceptor interceptor = new Interceptor(callback -> { /* never answers */ });
        manager.setInterceptor(interceptor);
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);

        triggerScheduled();
        awaitRelease();
        verify(session).release(DEFERRED);
        Assert.assertEquals(intercepted("timeout"), events);

        triggerScheduled();
        Assert.assertEquals(2, interceptor.calls);
    }

    @Test
    public void releasesDeferredWithoutAnEventWhenTheInterceptorThrows() throws JSONException {
        Interceptor interceptor = new Interceptor(callback -> {
            throw new IllegalStateException("interceptor bug");
        });
        manager.setInterceptor(interceptor);
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);

        triggerScheduled();
        verify(session).release(DEFERRED);
        Assert.assertTrue(events.isEmpty());

        triggerScheduled();
        Assert.assertEquals(2, interceptor.calls);
    }

    @Test
    public void anAnswerAfterTheInterceptorThrowsIsIgnored() throws JSONException {
        OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback[] saved = new OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback[1];
        manager.setInterceptor(new Interceptor(callback -> {
            saved[0] = callback;
            throw new IllegalStateException("interceptor bug");
        }));
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);
        triggerScheduled();

        saved[0].show();
        idleMain();

        verify(session).release(DEFERRED);
        Assert.assertTrue(events.isEmpty());
    }

    @Test
    public void keepsTheSlotForAMessageToShow() throws JSONException {
        manager.setInterceptor(new Interceptor(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback::show));
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);

        triggerScheduled();

        verify(session, never()).release(DONE);
        verify(session, never()).release(DEFERRED);
        Assert.assertEquals(intercepted("show"), events);
    }

    @Test
    public void releasesDeferredOnAMessageAlreadyHandledInThisProcess() throws JSONException {
        Interceptor interceptor = new Interceptor(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback::show);
        manager.setInterceptor(interceptor);
        scheduledReturns(message(OverlayMessagingMessage.MessageType.SESSION), 0);
        triggerScheduled();

        triggerScheduled();

        verify(session).release(DEFERRED);
        verify(session, never()).release(DONE);
        Assert.assertEquals(1, interceptor.calls);
    }

    // ========================= immediate messages =========================

    @Test
    public void immediateMessagesDoNotTouchTheSession() throws JSONException {
        immediateReturns(message(OverlayMessagingMessage.MessageType.IMMEDIATE));

        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
        idleMain();

        requestService.verify(() -> OverlayMessagingRequestService.readOverlayMessage(any(), any()));
        verifyNoInteractions(session);
    }

    @Test
    public void immediateMessagesDoNotTouchTheSessionWhenDeferred() throws JSONException {
        manager.setInterceptor(new Interceptor(OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback::defer));
        immediateReturns(message(OverlayMessagingMessage.MessageType.IMMEDIATE));

        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
        idleMain();

        verifyNoInteractions(session);
    }

    @Test
    public void immediateMessagesTakeOneSlotOfTheirOwn() throws JSONException {
        immediateReturns(message(OverlayMessagingMessage.MessageType.IMMEDIATE));

        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
        idleMain();
        manager.onTriggerReceived(OverlayMessagingMessage.MessageType.IMMEDIATE);
        idleMain();

        requestService.verify(() -> OverlayMessagingRequestService.readOverlayMessage(any(), any()));
    }
}
