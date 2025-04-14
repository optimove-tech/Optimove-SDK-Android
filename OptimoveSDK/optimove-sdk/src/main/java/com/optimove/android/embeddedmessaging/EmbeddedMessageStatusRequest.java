package com.optimove.android.embeddedmessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmbeddedMessageStatusRequest {
    private int tenantId;
    private String brandId;
    private String customerId;
    private Date now;
    private String engagementId;
    private Date executionDateTime;
    private CampaignKind campaignKind;
    private String messageId;
    private Date readAt;

    public EmbeddedMessageStatusRequest(Date now, String engagementId, CampaignKind campaignKind,
                                        String messageId, Date readAt, Date executionDateTime) {
        this.now = now;
        this.engagementId = engagementId;
        this.campaignKind = campaignKind;
        this.messageId = messageId;
        this.readAt = readAt;
        this.executionDateTime = executionDateTime;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        JSONArray metricsArray = new JSONArray();
        JSONObject metricsObj = new JSONObject();
        metricsObj.put("customerId", customerId);
        metricsObj.put("now", sdf.format(now));
        metricsObj.put("engagementId", engagementId);
        metricsObj.put("messageId", messageId);
        metricsObj.put("readAt", readAt.getTime());
        metricsObj.put("executionDateTime", sdf.format(executionDateTime));
        metricsObj.put("campaignKind", campaignKind.ordinal());
        metricsArray.put(metricsObj);
        JSONObject obj = new JSONObject();
        obj.put("tenantId", tenantId);
        obj.put("brandId", brandId);
        obj.put("statusMetrics", metricsArray);
        return obj;
    }
}

