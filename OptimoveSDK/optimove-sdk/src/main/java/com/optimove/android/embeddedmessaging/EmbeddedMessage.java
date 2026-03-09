package com.optimove.android.embeddedmessaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class EmbeddedMessage {
    private String id;
    private String containerId;
    private long templateId;
    private Date createdAt;
    private Date updatedAt;
    private Date readAt;
    private Date expiryDate;
    private String customerId;
    private boolean isVisitor;
    private String title;
    private String content;
    private String media;
    private String url;
    private String payload;
    private int campaignKind;
    private String engagementId;
    private Date executionDateTime;
    private int messageLayoutType;

    public EmbeddedMessage(JSONObject jsonMessage) throws JSONException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
        id = jsonMessage.optString("id");
        containerId = jsonMessage.optString("containerId");
        templateId = jsonMessage.optLong("templateId");
        createdAt = parseDate(jsonMessage.optString("createdAt"), isoFormat);
        updatedAt = parseDate(jsonMessage.optString("updatedAt"), isoFormat);
        readAt = parseNullableDate(jsonMessage.optString("readAt"), isoFormat);
        expiryDate = parseNullableDate(jsonMessage.optString("expiryDate"), isoFormat);
        customerId = jsonMessage.optString("customerId");
        isVisitor = jsonMessage.optBoolean("isVisitor");
        title = jsonMessage.optString("title");
        content = jsonMessage.optString("content");
        media = jsonMessage.optString("media");
        url = jsonMessage.optString("url");
        campaignKind = jsonMessage.optInt("campaignKind");
        payload = jsonMessage.optString("payload");
        engagementId = jsonMessage.optString("engagementId");
        executionDateTime = parseDate(jsonMessage.optString("executionDateTime"), isoFormat);
        messageLayoutType = jsonMessage.optInt("messageLayoutType");
    }

    public String getId() {
        return id;
    }

    public String getContainerId() {
        return containerId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getReadAt() {
        return readAt;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isVisitor() {
        return isVisitor;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getMedia() {
        return media;
    }

    public String getUrl() {
        return url;
    }

    public String getPayload() {
        return payload;
    }

    public JSONObject getJSONPayload() throws JSONException {
        return new JSONObject(payload);
    }

    public <T> T getPayload(Class<T> cls) {
        return new Gson().fromJson(payload, cls);
    }

    public int getCampaignKind() {
        return campaignKind;
    }

    public String getEngagementId() {
        return engagementId;
    }

    public Date getExecutionDateTime() {
        return executionDateTime;
    }

    public int getMessageLayoutType() {
        return messageLayoutType;
    }

    private static Date parseDate(String dateStr, SimpleDateFormat format) {
        if (dateStr == null || dateStr.isEmpty()) return new Date(0);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private static Date parseNullableDate(String dateStr, SimpleDateFormat format) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}

