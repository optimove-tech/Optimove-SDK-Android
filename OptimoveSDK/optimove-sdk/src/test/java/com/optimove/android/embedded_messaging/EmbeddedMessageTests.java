package com.optimove.android.embedded_messaging;

import com.optimove.android.embeddedmessaging.EmbeddedMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import java.util.Date;

public class EmbeddedMessageTests {

    String testJSON = " {\n" +
            "        \"customerId\": \"adam_b@optimove.com\",\n" +
            "        \"isVisitor\": false,\n" +
            "        \"templateId\": 1744118753960,\n" +
            "        \"title\": \"Testing stuart container\",\n" +
            "        \"content\": \"here is a test template\",\n" +
            "        \"media\": \"B04wM4Y7/yGxgri37bzmGBknvP4wgQdDy123Z9HfEUqYw9gmN.png\",\n" +
            "        \"readAt\": null,\n" +
            "        \"url\": \"https://google.com\",\n" +
            "        \"engagementId\": \"0\",\n" +
            "        \"payload\": {},\n" +
            "        \"campaignKind\": 1,\n" +
            "        \"executionDateTime\": \"2025-04-08T13:32:16Z\",\n" +
            "        \"expiryDate\": \"2025-05-08T23:59:00Z\",\n" +
            "        \"containerId\": \"stuart\",\n" +
            "        \"id\": \"3842b5cd-9751-4cf1-9f9b-9636b38182c6\",\n" +
            "        \"createdAt\": 1744278486,\n" +
            "        \"updatedAt\": null,\n" +
            "        \"deletedAt\": null\n" +
            "      }";

    @Test
    public void shouldConvertIntsToDates() throws JSONException {
        EmbeddedMessage message = new EmbeddedMessage(new JSONObject(testJSON));
        Assert.assertEquals(new Date(1744278486), message.getCreatedAt());
    }
}
