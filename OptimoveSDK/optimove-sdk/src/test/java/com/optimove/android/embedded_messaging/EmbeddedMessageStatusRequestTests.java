package com.optimove.android.embedded_messaging;

import com.optimove.android.embeddedmessaging.EmbeddedMessageStatusRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmbeddedMessageStatusRequestTests {
    @Test
    public void shouldConvertToJson() throws JSONException {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        EmbeddedMessageStatusRequest request = new EmbeddedMessageStatusRequest(
                dt, "engagement", 1, "message", dt, dt);
        request.setCustomerId("customer");
        request.setBrandId("brand");
        request.setTenantId(123);
        JSONObject actual = request.toJSONObject();
        Assert.assertEquals(123, actual.getInt("tenantId"));
        Assert.assertEquals("brand", actual.getString("brandId"));

        JSONObject actualMetrics = actual.getJSONArray("statusMetrics").getJSONObject(0);
        Assert.assertEquals("customer", actualMetrics.getString("customerId"));
        Assert.assertEquals(sdf.format(dt), actualMetrics.getString("now"));
        Assert.assertEquals("engagement", actualMetrics.getString("engagementId"));
        Assert.assertEquals(sdf.format(dt), actualMetrics.getString("executionDateTime"));
        Assert.assertEquals(1, actualMetrics.getInt("campaignKind"));
        Assert.assertEquals("message", actualMetrics.getString("messageId"));
        Assert.assertEquals(dt.getTime(), actualMetrics.getLong("readAt"));
    }

    @Test
    public void shouldThrowErrorsOnMissingFields() {
        Date dt = new Date();
        EmbeddedMessageStatusRequest request = new EmbeddedMessageStatusRequest(
                dt, "engagement", 1, "message", dt, dt);
        Assert.assertThrows(IllegalArgumentException.class, request::toJSONObject);
    }

    @Test
    public void shouldAllowNullReadAt() throws JSONException {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        EmbeddedMessageStatusRequest request = new EmbeddedMessageStatusRequest(
                dt, "engagement", 1, "message", null, dt);
        request.setCustomerId("customer");
        request.setBrandId("brand");
        request.setTenantId(123);
        JSONObject actual = request.toJSONObject();
        JSONObject actualMetrics = actual.getJSONArray("statusMetrics").getJSONObject(0);
        Assert.assertEquals(-1, actualMetrics.optLong("readAt", -1));
    }
}
