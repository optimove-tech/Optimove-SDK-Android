package com.optimove.android.embedded_messaging;

import com.optimove.android.embeddedmessaging.ContainerMessageRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ContainerMessageRequestTests {

    @Test
    public void shouldConvertToJson() throws JSONException {
        ContainerMessageRequest request = new ContainerMessageRequest("test-id", 2);
        JSONObject expected = new JSONObject("{\"limit\":2,\"containerId\":\"test-id\"}");
        JSONObject actual = request.toJSONObject();
        Assert.assertEquals(expected.getString("containerId"), actual.getString("containerId"));
        Assert.assertEquals(expected.getInt("limit"), actual.getInt("limit"));
    }
}
