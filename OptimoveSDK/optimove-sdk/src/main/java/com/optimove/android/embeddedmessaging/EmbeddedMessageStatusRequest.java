package com.optimove.android.embeddedmessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class EmbeddedMessageStatusRequest {
    private int tenantId;
    private String brandId;
    private String customerId;
    private String now;
    private String engagementId;
    private CampaignKind campaignKind;
    private String messageId;
    private Date readAt;

    public EmbeddedMessageStatusRequest(String now, String engagementId, CampaignKind campaignKind,
                                        String messageId, Date readAt) {
        this.now = now;
        this.engagementId = engagementId;
        this.campaignKind = campaignKind;
        this.messageId = messageId;
        this.readAt = readAt;
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
        JSONArray metricsArray = new JSONArray();
        JSONObject metricsObj = new JSONObject();
        metricsObj.put("customerId", customerId);
        metricsObj.put("now", now);
        metricsObj.put("engagementId", engagementId);
        metricsObj.put("messageId", messageId);
        metricsObj.put("readAt", readAt.getTime());
        metricsObj.put("campaignKind", campaignKind.ordinal());
        metricsArray.put(metricsObj);
        JSONObject obj = new JSONObject();
        obj.put("tenantId", tenantId);
        obj.put("brandId", brandId);
        obj.put("statusMetrics", metricsArray);
        return obj;
    }
}

