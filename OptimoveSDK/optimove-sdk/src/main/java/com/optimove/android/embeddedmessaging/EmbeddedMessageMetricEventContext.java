package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;
public class EmbeddedMessageMetricEventContext {
    private String messageId;

    public EmbeddedMessageMetricEventContext(String messageId){
        this.messageId = messageId;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("messageId", messageId);
        return obj;
    }
}
