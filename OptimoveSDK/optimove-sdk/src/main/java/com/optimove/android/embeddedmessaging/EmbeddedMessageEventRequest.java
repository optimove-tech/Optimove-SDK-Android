package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmbeddedMessageEventRequest {
    private Date timestamp;
    private String uuid;
    private EventType eventType;
    private String customerId;
    private String visitorId;

    private EmbeddedMessageMetricEventContext context;

    public EmbeddedMessageEventRequest(
            Date timestamp, String uuid, EventType eventType,
            EmbeddedMessageMetricEventContext context, String customerId,
            String visitorId) {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.eventType = eventType;
        this.context = context;
        this.customerId = customerId;
        this.visitorId = visitorId;
    }
    public JSONObject toJSONObject() throws JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        JSONObject metricsObj = new JSONObject();
        metricsObj.put("timestamp", sdf.format(timestamp));
        metricsObj.put("uuid", uuid);
        metricsObj.put("eventType", eventType.toString());
        metricsObj.put("context", context.toJSONObject());
        metricsObj.put("customerId", customerId);
        metricsObj.put("visitorId", visitorId);
        return metricsObj;
    }
}
