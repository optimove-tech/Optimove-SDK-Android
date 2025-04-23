package com.optimove.android.embeddedmessaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class EmbeddedMessage {
    private String id;
    private String containerId;
    private int templateId;
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
        id = jsonMessage.optString("id");
        containerId = jsonMessage.optString("containerId");
        templateId = jsonMessage.optInt("templateId");
        createdAt = convertLongToDate(jsonMessage.optLong("createdAt"));
        updatedAt = convertLongToDate(jsonMessage.optLong("updatedAt"));
        readAt = convertLongToNullableDate(jsonMessage.optLong("readAt"));
        expiryDate = convertLongToNullableDate(jsonMessage.optLong("expiryDate"));
        customerId = jsonMessage.optString("customerId");
        isVisitor = jsonMessage.optBoolean("isVisitor");
        title = jsonMessage.optString("title");
        content = jsonMessage.optString("content");
        media = jsonMessage.optString("media");
        url = jsonMessage.optString("url");
        campaignKind = jsonMessage.optInt("campaignKind");
        payload = jsonMessage.optString("payload");
        engagementId = jsonMessage.optString("engagementId");
        executionDateTime = convertLongToDate(jsonMessage.optLong("executionDateTime"));
        messageLayoutType = jsonMessage.optInt("messageLayoutType");
    }

    public String getId() {
        return id;
    }

    public String getContainerId() {
        return containerId;
    }

    public int getTemplateId() {
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

    private Date convertLongToNullableDate(long unixTimestamp) {
        if(unixTimestamp <= 0) {
            return null;
        }
        return convertLongToDate(unixTimestamp);
    }
    private Date convertLongToDate(long unixTimestamp) {
        return new Date(unixTimestamp);
    }
}

