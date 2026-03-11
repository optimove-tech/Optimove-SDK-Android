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

    String testJSON = "{\n" +
            "  \"customerId\": \"user-vlad\",\n" +
            "  \"isVisitor\": false,\n" +
            "  \"templateId\": 1772970898590,\n" +
            "  \"title\": \"template 2\",\n" +
            "  \"content\": \"because template 1 needs a friend\",\n" +
            "  \"media\": \"\",\n" +
            "  \"readAt\": \"2026-03-08T12:13:51.305+00:00\",\n" +
            "  \"url\": \"\",\n" +
            "  \"engagementId\": null,\n" +
            "  \"payload\": \"{}\",\n" +
            "  \"campaignKind\": 2,\n" +
            "  \"executionDateTime\": \"2026-03-08T11:58:54.739155Z\",\n" +
            "  \"messageLayoutType\": 0,\n" +
            "  \"expiryDate\": \"2026-04-07T23:59:00Z\",\n" +
            "  \"containerId\": \"vv-container-id\",\n" +
            "  \"id\": \"08bdd0d1-1ad8-4680-9c22-cc29cecd8dd0\",\n" +
            "  \"createdAt\": \"2026-03-08T11:58:52.739+00:00\",\n" +
            "  \"updatedAt\": \"2026-03-08T12:13:50.57+00:00\",\n" +
            "  \"deletedAt\": null\n" +
            "}";
    EmbeddedMessage message;
    @Before
    public void setUp() throws JSONException {
       message = new EmbeddedMessage(new JSONObject(testJSON));
    }
    @Test
    public void shouldParseDatesFromISOStrings() throws JSONException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals("2026-03-08T11:58:52", fmt.format(message.getCreatedAt()));
        Assert.assertEquals("2026-03-08T12:13:50", fmt.format(message.getUpdatedAt()));
        Assert.assertEquals("2026-03-08T12:13:51", fmt.format(message.getReadAt()));
        Assert.assertEquals("2026-04-07T23:59:00", fmt.format(message.getExpiryDate()));
        Assert.assertEquals("2026-03-08T11:58:54", fmt.format(message.getExecutionDateTime()));


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
