package com.optimove.android.embedded_messaging;

import com.optimove.android.embeddedmessaging.ContainerRequestOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ContainerMessageRequestTests {

    @Test
    public void shouldConvertToJson() throws JSONException {
        ContainerRequestOptions request = new ContainerRequestOptions("test-id", 2);
        JSONObject actual = request.toJSONObject();
        Assert.assertEquals("test-id", actual.getString("containerId"));
        Assert.assertEquals(2, actual.getInt("limit"));
    }
}
