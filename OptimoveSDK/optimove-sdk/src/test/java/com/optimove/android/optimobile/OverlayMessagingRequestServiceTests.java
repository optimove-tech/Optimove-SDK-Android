package com.optimove.android.optimobile;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OverlayMessagingRequestServiceTests {

    @Before
    public void setUp() {
        OverlayMessagingRequestService.resetRetry();
    }

    // ========================= parseScheduledResponse =========================

    @Test
    public void parsesTheFirstMessageAsScheduled() throws JSONException {
        OverlayMessagingRequestService.ScheduledFetchResult result = OverlayMessagingRequestService.parseScheduledResponse(
                "{\"messages\":[{\"id\":7,\"content\":{\"ver\":1,\"html\":\"<p>hi</p>\"},\"data\":{\"k\":\"v\"}}],\"nextFetchInSeconds\":0}");

        Assert.assertNotNull(result.message);
        Assert.assertEquals(7, result.message.getId());
        Assert.assertEquals("<p>hi</p>", result.message.getContent().getString("html"));
        Assert.assertEquals("v", result.message.getData().getString("k"));
        Assert.assertEquals(OverlayMessagingMessage.MessageType.SESSION, result.message.getType());
        Assert.assertEquals(0, result.nextFetchInSeconds);
    }

    @Test
    public void parsesNoMessageWithTheServersNextFetch() throws JSONException {
        OverlayMessagingRequestService.ScheduledFetchResult result = OverlayMessagingRequestService.parseScheduledResponse(
                "{\"messages\":[],\"nextFetchInSeconds\":2040}");

        Assert.assertNull(result.message);
        Assert.assertEquals(2040, result.nextFetchInSeconds);
    }

    @Test
    public void rejectsABodyWithoutNextFetch() {
        Assert.assertThrows(JSONException.class,
                () -> OverlayMessagingRequestService.parseScheduledResponse("{\"messages\":[]}"));
    }

    // ========================= retry backoff =========================

    @Test
    public void retriesAfterOneMinuteDoublingUpToAnHour() {
        long[] expected = {60, 120, 240, 480, 960, 1920, 3600, 3600};
        for (long seconds : expected) {
            OverlayMessagingRequestService.ScheduledFetchResult result = OverlayMessagingRequestService.retryLater();
            Assert.assertNull(result.message);
            Assert.assertEquals(seconds, result.nextFetchInSeconds);
        }
    }

    @Test
    public void aSuccessResetsTheBackoff() {
        OverlayMessagingRequestService.retryLater();
        OverlayMessagingRequestService.retryLater();

        OverlayMessagingRequestService.resetRetry();

        Assert.assertEquals(60, OverlayMessagingRequestService.retryLater().nextFetchInSeconds);
    }
}
