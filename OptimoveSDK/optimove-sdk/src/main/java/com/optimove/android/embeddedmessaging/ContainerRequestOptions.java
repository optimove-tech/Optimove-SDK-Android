package com.optimove.android.embeddedmessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class ContainerRequestOptions {

    private String containerId;
    private int limit;

    public ContainerRequestOptions(String containerId, int limit) {
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
