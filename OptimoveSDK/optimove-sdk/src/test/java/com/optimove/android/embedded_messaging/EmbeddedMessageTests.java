package com.optimove.android.embedded_messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.optimove.android.embeddedmessaging.EmbeddedMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

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
            "        \"payload\": \"{'key1': 'value1', 'key2': 'value2'}\",\n" +
            "        \"campaignKind\": 1,\n" +
            "        \"executionDateTime\": \"2025-04-08T13:32:16Z\",\n" +
            "        \"expiryDate\": \"2025-05-08T23:59:00Z\",\n" +
            "        \"containerId\": \"stuart\",\n" +
            "        \"id\": \"3842b5cd-9751-4cf1-9f9b-9636b38182c6\",\n" +
            "        \"createdAt\": 1744278486,\n" +
            "        \"updatedAt\": null,\n" +
            "        \"deletedAt\": null\n" +
            "      }";
    EmbeddedMessage message;
    @Before
    public void setUp() throws JSONException {
       message = new EmbeddedMessage(new JSONObject(testJSON));
    }
    @Test
    public void shouldConvertIntsToDates(){
        Assert.assertEquals(new Date(1744278486), message.getCreatedAt());
    }

    @Test
    public void shouldAllowNullReadAt() {
        Assert.assertNull(message.getReadAt());
    }

    @Test
    public void shouldGetPayloadAsJsonObject() throws JSONException {
        JSONObject payloadData = message.getJSONPayload();
        Assert.assertEquals("value1", payloadData.getString("key1"));
        Assert.assertEquals("value2", payloadData.getString("key2"));
    }
    
    @Test
    public void shouldGetPayloadAsCustomObject() throws JSONException {
        TestPayload payloadData = message.getPayload(TestPayload.class);
        Assert.assertEquals("value1", payloadData.getKey1());
        Assert.assertEquals("value2", payloadData.getKey2());
    }

    class TestPayload {
        private String key1;
        private String key2;
        public TestPayload(String key1, String key2) {
            this.key1 = key1;
            this.key2 = key2;
        }
        public String getKey1() {
            return this.key1;
        }
        public String getKey2() {
            return this.key2;
        }
    }


}
