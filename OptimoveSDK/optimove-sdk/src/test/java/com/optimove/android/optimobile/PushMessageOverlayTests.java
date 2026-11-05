package com.optimove.android.optimobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PushMessageOverlayTests {

    private static PushMessage push(String data) throws JSONException {
        return new PushMessage(1, null, null, new JSONObject(data), 0L, null, true, null, null, null, null);
    }

    @Test
    public void readsTheNextFetchFromAScheduledSignal() throws JSONException {
        PushMessage push = push("{\"k.deepLink\":{\"type\":3,\"data\":{\"nextFetchInSeconds\":600}}}");

        Assert.assertTrue(push.isOverlayScheduledSignal());
        Assert.assertEquals(Long.valueOf(600), push.getOverlayScheduledNextFetchInSeconds());
        Assert.assertFalse(push.isOverlayTriggeredSignal());
    }

    @Test
    public void readsASignalFor0() throws JSONException {
        PushMessage push = push("{\"k.deepLink\":{\"type\":3,\"data\":{\"nextFetchInSeconds\":0}}}");

        Assert.assertEquals(Long.valueOf(0), push.getOverlayScheduledNextFetchInSeconds());
    }

    @Test
    public void ignoresAScheduledSignalWithoutTheValue() throws JSONException {
        Assert.assertNull(push("{\"k.deepLink\":{\"type\":3,\"data\":{}}}").getOverlayScheduledNextFetchInSeconds());
        Assert.assertNull(push("{\"k.deepLink\":{\"type\":3}}").getOverlayScheduledNextFetchInSeconds());
    }

    @Test
    public void aTriggeredSignalIsNotAScheduledSignal() throws JSONException {
        PushMessage push = push("{\"k.deepLink\":{\"type\":2}}");

        Assert.assertTrue(push.isOverlayTriggeredSignal());
        Assert.assertFalse(push.isOverlayScheduledSignal());
        Assert.assertNull(push.getOverlayScheduledNextFetchInSeconds());
    }

    @Test
    public void aPushWithoutADeepLinkIsNotAScheduledSignal() throws JSONException {
        PushMessage push = push("{}");

        Assert.assertFalse(push.isOverlayScheduledSignal());
        Assert.assertNull(push.getOverlayScheduledNextFetchInSeconds());
    }
}
