package com.optimove.android.optimobile;

import org.json.JSONObject;
import org.junit.Assert;
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
public class OverlayLifecycleTrackerTests {

    private OverlayLifecycleTracker tracker;
    private List<String> calls;

    private static OverlayMessagingMessage msg(long id) {
        return new OverlayMessagingMessage(id, new JSONObject(), null, OverlayMessagingMessage.MessageType.SESSION);
    }

    @Before
    public void setUp() {
        tracker = new OverlayLifecycleTracker();
        calls = new ArrayList<>();
        tracker.setListener(new OverlayMessagingLifecycleListener() {
            @Override
            public void onShown(OverlayMessagingMessage message) {
                calls.add("shown:" + message.getId());
            }

            @Override
            public void onClosed(OverlayMessagingMessage message, OverlayMessagingClosedInfo info) {
                calls.add("closed:" + message.getId() + ":" + info.getReason());
            }
        });
    }

    @Test
    public void showThenDismiss() {
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onClosed(m, OverlayMessagingCloseReason.DISMISSED);
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:dismissed"), calls);
    }

    @Test
    public void representingSameMessageDoesNotRefireShown() {
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onPresented(m); // e.g. view recreated after an activity change
        Assert.assertEquals(Arrays.asList("shown:1"), calls);
    }

    @Test
    public void closedFiresExactlyOnce() {
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onClosed(m, OverlayMessagingCloseReason.DISMISSED);
        tracker.onClosed(m, OverlayMessagingCloseReason.ERROR);
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:dismissed"), calls);
    }

    @Test
    public void closedNotFiredForMessageNeverShown() {
        tracker.onClosed(msg(1), OverlayMessagingCloseReason.ERROR);
        Assert.assertTrue(calls.isEmpty());
    }

    @Test
    public void errorReason() {
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onClosed(m, OverlayMessagingCloseReason.ERROR);
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:error"), calls);
    }

    @Test
    public void actionedIsStickyAndPerMessage() {
        OverlayMessagingMessage a = msg(1);
        OverlayMessagingMessage b = msg(2);
        tracker.onPresented(a);
        tracker.onLinkActionDispatched(a);
        tracker.onClosed(a, OverlayMessagingCloseReason.DISMISSED);
        tracker.onPresented(b);
        tracker.onClosed(b, OverlayMessagingCloseReason.DISMISSED);
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:actioned", "shown:2", "closed:2:dismissed"), calls);
    }

    @Test
    public void linkActionForOtherMessageIgnored() {
        OverlayMessagingMessage a = msg(1);
        tracker.onPresented(a);
        tracker.onLinkActionDispatched(msg(2));
        tracker.onClosed(a, OverlayMessagingCloseReason.DISMISSED);
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:dismissed"), calls);
    }

    @Test
    public void presentingNextWithoutCloseClosesPreviousFirst() {
        tracker.onPresented(msg(1));
        tracker.onPresented(msg(2));
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1:error", "shown:2"), calls);
    }

    @Test
    public void throwingListenerDoesNotBreakPairing() {
        List<String> seen = new ArrayList<>();
        tracker.setListener(new OverlayMessagingLifecycleListener() {
            @Override
            public void onShown(OverlayMessagingMessage message) {
                seen.add("shown:" + message.getId());
                throw new RuntimeException("boom");
            }

            @Override
            public void onClosed(OverlayMessagingMessage message, OverlayMessagingClosedInfo info) {
                seen.add("closed:" + message.getId());
                throw new RuntimeException("boom");
            }
        });
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onClosed(m, OverlayMessagingCloseReason.DISMISSED);
        tracker.onPresented(msg(2));
        Assert.assertEquals(Arrays.asList("shown:1", "closed:1", "shown:2"), seen);
    }

    @Test
    public void defaultMethodsAreOptionalAndNullListenerIsSafe() {
        tracker.setListener(new OverlayMessagingLifecycleListener() {});
        OverlayMessagingMessage m = msg(1);
        tracker.onPresented(m);
        tracker.onClosed(m, OverlayMessagingCloseReason.DISMISSED);
        tracker.setListener(null);
        tracker.onPresented(msg(2));
        Assert.assertTrue(calls.isEmpty());
    }
}
