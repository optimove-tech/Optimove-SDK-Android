package com.optimove.android.embeddedmessaging;

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
    private Map<String, String> payload;
    private int campaignKind;
    private String engagementId;
    private Date executionDateTime;

    public EmbeddedMessage(JSONObject jsonMessage) throws JSONException {
        id = jsonMessage.optString("id");
        containerId = jsonMessage.optString("containerId");
        templateId = jsonMessage.optInt("templateId");
        createdAt = convertIntToDate(jsonMessage.optLong("createdAt"));
        updatedAt = convertIntToDate(jsonMessage.optLong("updatedAt"));
        readAt = convertIntToDate(jsonMessage.optLong("readAt"));
        expiryDate = convertIntToDate(jsonMessage.optLong("expiryDate"));
        customerId = jsonMessage.optString("customerId");
        isVisitor = jsonMessage.optBoolean("isVisitor");
        title = jsonMessage.optString("title");
        content = jsonMessage.optString("content");
        media = jsonMessage.optString("media");
        url = jsonMessage.optString("url");
        campaignKind = jsonMessage.optInt("campaignKind");
        engagementId = jsonMessage.optString("engagementId");
        executionDateTime = convertIntToDate(jsonMessage.optLong("executionDateTime"));
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

    public Map<String, String> getPayload() {
        return payload;
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

    private Date convertIntToDate(long unixTimestamp) {
        return new Date(unixTimestamp);
    }
}

