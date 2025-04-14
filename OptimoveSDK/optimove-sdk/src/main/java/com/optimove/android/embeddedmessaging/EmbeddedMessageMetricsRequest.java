package com.optimove.android.embeddedmessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmbeddedMessageMetricsRequest {
    private int tenantId;
    private String brandId;
    private String customerId;
    private Date now;
    private MetricEvent event;
    private String engagementId;
    private Date executionDateTime;
    private CampaignKind campaignKind;


    public EmbeddedMessageMetricsRequest(
            Date now, MetricEvent event, String engagementId, Date executionDateTime,
            CampaignKind campaignKind) {
        this.now = now;
        this.event = event;
        this.engagementId = engagementId;
        this.executionDateTime = executionDateTime;
        this.campaignKind = campaignKind;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public JSONObject toJSONObject() throws JSONException {
        if(tenantId == 0) throw new IllegalArgumentException("Tenant Id is required");
        if(brandId.isEmpty()) throw new IllegalArgumentException("Brand Id is required");
        if(customerId.isEmpty()) throw new IllegalArgumentException("customer Id is required");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        JSONObject metricsObj = new JSONObject();
        metricsObj.put("customerId", customerId);
        metricsObj.put("now", sdf.format(now));
        metricsObj.put("event", event.ordinal());
        metricsObj.put("engagementId", engagementId);
        metricsObj.put("executionDateTime", sdf.format(executionDateTime));
        metricsObj.put("campaignKind", campaignKind.ordinal());
        JSONObject obj = new JSONObject();
        obj.put("tenantId", tenantId);
        obj.put("brandId", brandId);
        JSONArray metricsArray = new JSONArray();
        metricsArray.put(metricsObj);
        obj.put("messageMetrics", metricsArray);
        return obj;
    }
}
