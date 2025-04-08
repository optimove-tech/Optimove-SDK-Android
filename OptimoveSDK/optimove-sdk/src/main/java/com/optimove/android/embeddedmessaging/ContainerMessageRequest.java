package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class ContainerMessageRequest {

    private String containerId;
    private int limit;

    public ContainerMessageRequest(String containerId, int limit) {
        this.containerId = containerId;
        this.limit = limit;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj =  new JSONObject();
        obj.put("containerId", containerId);
        obj.put("limit", limit);
        return obj;
    }
}
