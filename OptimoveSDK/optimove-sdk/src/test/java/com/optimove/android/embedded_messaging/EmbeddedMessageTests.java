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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class EmbeddedMessageTests {


    String testJSON = " {\n" +
            "        \"customerId\": \"adam_b@optimove.com\",\n" +
            "        \"isVisitor\": false,\n" +
            "        \"templateId\": 1744118753960,\n" +
            "        \"title\": \"Testing stuart container\",\n" +
            "        \"content\": \"here is a test template\",\n" +
            "        \"media\": \"B04wM4Y7/yGxgri37bzmGBknvP4wgQdDy123Z9HfEUqYw9gmN.png\",\n" +
            "        \"readAt\": \"2026-03-08T12:13:51.305+00:00\",\n" +
            "        \"url\": \"https://google.com\",\n" +
            "        \"engagementId\": \"1000\",\n" +
            "        \"payload\": \"{'key1': 'value1', 'key2': 'value2'}\",\n" +
            "        \"campaignKind\": 1,\n" +
            "        \"executionDateTime\": \"2026-03-08T11:58:54.739155Z\",\n" +
            "        \"messageLayoutType\": 0,\n" +
            "        \"expiryDate\": \"2026-04-07T23:59:00Z\",\n" +
            "        \"containerId\": \"stuart\",\n" +
            "        \"id\": \"3842b5cd-9751-4cf1-9f9b-9636b38182c6\",\n" +
            "        \"createdAt\": \"2025-04-08T13:32:16.000+00:00\",\n" +
            "        \"updatedAt\": \"2026-03-08T12:13:50.57+00:00\",\n" +
            "        \"deletedAt\": null\n" +
            "      }";
    EmbeddedMessage message;
    @Before
    public void setUp() throws JSONException {
       message = new EmbeddedMessage(new JSONObject(testJSON));
    }
    @Test
    public void shouldParseCompletePayload() throws JSONException {
        // dates in the plethora of formats
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals("2025-04-08T13:32:16", fmt.format(message.getCreatedAt()));
        Assert.assertEquals("2026-03-08T12:13:50", fmt.format(message.getUpdatedAt()));
        Assert.assertEquals("2026-03-08T12:13:51", fmt.format(message.getReadAt()));
        Assert.assertEquals("2026-04-07T23:59:00", fmt.format(message.getExpiryDate()));
        Assert.assertEquals("2026-03-08T11:58:54", fmt.format(message.getExecutionDateTime()));

        // rest
        Assert.assertEquals("adam_b@optimove.com", message.getCustomerId());
        Assert.assertFalse( message.isVisitor());
        Assert.assertEquals(1744118753960L, message.getTemplateId());
        Assert.assertEquals("Testing stuart container", message.getTitle());
        Assert.assertEquals("here is a test template", message.getContent());
        Assert.assertEquals("B04wM4Y7/yGxgri37bzmGBknvP4wgQdDy123Z9HfEUqYw9gmN.png", message.getMedia());
        Assert.assertEquals("https://google.com", message.getUrl());
        Assert.assertEquals("1000", message.getEngagementId());
        Assert.assertEquals("{'key1': 'value1', 'key2': 'value2'}", message.getPayload());
        Assert.assertEquals(1, message.getCampaignKind());
        Assert.assertEquals(0, message.getMessageLayoutType());
        Assert.assertEquals("stuart", message.getContainerId());
        Assert.assertEquals("3842b5cd-9751-4cf1-9f9b-9636b38182c6", message.getId());

    }

    @Test
    public void shouldAllowNullReadAtAndExpiryDate() throws JSONException {
        String json = "{\n" +
                "  \"customerId\": \"user-vlad\",\n" +
                "  \"isVisitor\": false,\n" +
                "  \"templateId\": 1,\n" +
                "  \"readAt\": null,\n" +
                "  \"expiryDate\": null,\n" +
                "  \"payload\": \"{}\",\n" +
                "  \"campaignKind\": 1,\n" +
                "  \"executionDateTime\": \"2026-03-08T11:58:54.739+00:00\",\n" +
                "  \"createdAt\": \"2026-03-08T11:58:54.739+00:00\"\n" +
                "}";
        EmbeddedMessage msg = new EmbeddedMessage(new JSONObject(json));
        Assert.assertNull(msg.getReadAt());
        Assert.assertNull(msg.getExpiryDate());
    }


    @Test
    public void shouldParseNullableAndEmptyStrings() throws JSONException {
        String json = "{" +
                "  \"customerId\": \"user-vlad\"," +
                "  \"isVisitor\": false," +
                "  \"templateId\": 1," +
                "  \"campaignKind\": 1," +
                "  \"engagementId\": null," +
                "  \"url\": \"\"," +
                "  \"media\": \"\"," +
                "  \"executionDateTime\": \"2026-03-08T11:58:54.739+00:00\"," +
                "  \"createdAt\": \"2026-03-08T11:58:54.739+00:00\"" +
                "}";
        EmbeddedMessage msg = new EmbeddedMessage(new JSONObject(json));
        // On JVM (test env) optString returns "" for JSON null as uses org.json;
        // on Android runtime it returns "null".
        // We cannot use org.json elsewhere as breaks contract.
        // We cannot use Android's built-in JSONObject in tests
        Assert.assertEquals("", msg.getEngagementId());
        Assert.assertEquals("", msg.getUrl());
        Assert.assertEquals("", msg.getMedia());
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
