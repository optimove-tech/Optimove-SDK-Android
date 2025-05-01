package com.optimove.android.embedded_messaging;

import com.optimove.android.embeddedmessaging.EmbeddedMessageMetricsRequest;
import com.optimove.android.embeddedmessaging.MetricEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmbeddedMessageMetricsRequestTests {
    @Test
    public void shouldConvertToJson() throws JSONException {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        EmbeddedMessageMetricsRequest request = new EmbeddedMessageMetricsRequest(
                dt, MetricEvent.CLICK, "engagement", dt, 1);
        request.setCustomerId("customer");
        request.setBrandId("brand");
        request.setTenantId(123);
        JSONObject actual = request.toJSONObject();
        Assert.assertEquals(123, actual.getInt("tenantId"));
        Assert.assertEquals("brand", actual.getString("brandId"));

        JSONObject actualMetrics = actual.getJSONArray("messageMetrics").getJSONObject(0) ;
        Assert.assertEquals("customer", actualMetrics.getString("customerId"));
        Assert.assertEquals(sdf.format(dt), actualMetrics.getString("now"));
        Assert.assertEquals("engagement", actualMetrics.getString("engagementId"));
        Assert.assertEquals(sdf.format(dt), actualMetrics.getString("executionDateTime"));
        Assert.assertEquals(1, actualMetrics.getInt("campaignKind"));

    }

    @Test
    public void shouldThrowErrorsOnMissingFields() {
        Date dt = new Date();
        EmbeddedMessageMetricsRequest request = new EmbeddedMessageMetricsRequest(
                dt, MetricEvent.CLICK, "engagement", dt, 1);
        Assert.assertThrows(IllegalArgumentException.class, request::toJSONObject);
    }
}
