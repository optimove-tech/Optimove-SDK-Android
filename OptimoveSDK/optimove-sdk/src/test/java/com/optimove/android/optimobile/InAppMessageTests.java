package com.optimove.android.optimobile;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class InAppMessageTests {

    private static final long ONE_HOUR_MS = 60 * 60 * 1000;

    private InAppMessage messageExpiringAt(Date expiresAt) {
        return new InAppMessage(1, "immediately", new JSONObject(), null, null, null, expiresAt);
    }

    @Test
    public void messageWithoutExpiryIsNotExpired() {
        Assert.assertFalse(messageExpiringAt(null).isExpired());
    }

    @Test
    public void messageWithFutureExpiryIsNotExpired() {
        Assert.assertFalse(messageExpiringAt(new Date(System.currentTimeMillis() + ONE_HOUR_MS)).isExpired());
    }

    @Test
    public void messageWithPastExpiryIsExpired() {
        Assert.assertTrue(messageExpiringAt(new Date(System.currentTimeMillis() - ONE_HOUR_MS)).isExpired());
    }

    @Test
    public void clearingExpiryMakesMessageNotExpired() {
        InAppMessage message = messageExpiringAt(new Date(System.currentTimeMillis() - ONE_HOUR_MS));

        message.setExpiresAt(null);

        Assert.assertFalse(message.isExpired());
    }
}
