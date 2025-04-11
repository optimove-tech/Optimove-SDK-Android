package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class EmbeddedMessageMetricsRequest {
    //    {
//        "tenantId": 1,
//            "brandId": "f29e20e6-53eb-4a79-a004-09fe308d63de",
//            "messageMetrics": [
//        {
//            "customerId": "string",
//                "now": "2025-04-10T13:15:01.706Z",
//                "event": 0,
//                "engagementId": "string",
//                "executionDateTime": "2025-04-10T13:15:01.706Z",
//                "campaignKind": 0,
//                "additionalProp1": "string",
//                "additionalProp2": "string",
//                "additionalProp3": "string"
//        }
//  ],
    private int tenantId;
    private String brandId;
    private String customerId;
    private String now;
    private int event;
    private String engagementId;
    private String executionDateTime;
    private CampaignKind campaignKind;


    public EmbeddedMessageMetricsRequest(
            int tenantId, String brandId, String customerId, String now, int event,
            String engagementId, String executionDateTime, CampaignKind campaignKind) {
        this.tenantId = tenantId;
        this.brandId = brandId;
        this.customerId = customerId;
        this.now = now;
        this.event = event;
        this.engagementId = engagementId;
        this.executionDateTime = executionDateTime;
        this.campaignKind = campaignKind;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject metricsObj = new JSONObject();
        metricsObj.put("customerId", customerId);
        metricsObj.put("now", now);
        metricsObj.put("event", event);
        metricsObj.put("engagementId", engagementId);
        metricsObj.put("executionDateTime", executionDateTime);
        metricsObj.put("campaignKind", campaignKind);
        JSONObject obj = new JSONObject();
        obj.put("tenandId", tenantId);
        obj.put("brandId", brandId);
        obj.put("messageMetrics", metricsObj);
        return obj;
    }
}
