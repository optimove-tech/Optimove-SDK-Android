package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;
public class EmbeddedMessageMetricEventContext {
    private String messageId;

    private String containerId;

    public EmbeddedMessageMetricEventContext(String messageId, String containerId) {
        this.messageId = messageId;
        this.containerId = containerId;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("messageId", messageId);
        obj.put("containerId", containerId);
        return obj;
    }
}
