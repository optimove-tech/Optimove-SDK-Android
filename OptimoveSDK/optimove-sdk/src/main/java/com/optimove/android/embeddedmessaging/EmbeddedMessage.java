package com.optimove.android.embeddedmessaging;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private static final SimpleDateFormat[] ISO_DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),// millisecond precision
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),// second precision
    };

    public EmbeddedMessage(JSONObject jsonMessage) throws JSONException {
        id = jsonMessage.optString("id");
        containerId = jsonMessage.optString("containerId");
        templateId = jsonMessage.optLong("templateId");
        createdAt = parseDate(jsonMessage.optString("createdAt"));
        updatedAt = parseDate(jsonMessage.optString("updatedAt"));
        readAt = parseNullableDate(jsonMessage.optString("readAt"));
        expiryDate = parseNullableDate(jsonMessage.optString("expiryDate"));
        customerId = jsonMessage.optString("customerId");
        isVisitor = jsonMessage.optBoolean("isVisitor");
        title = jsonMessage.optString("title");
        content = jsonMessage.optString("content");
        media = jsonMessage.optString("media");
        url = jsonMessage.optString("url");
        campaignKind = jsonMessage.optInt("campaignKind");
        payload = jsonMessage.optString("payload");
        engagementId = jsonMessage.optString("engagementId");




        executionDateTime = parseDate(jsonMessage.optString("executionDateTime"));
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

    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("null")) return new Date(0);
        dateStr = trimMicroseconds(dateStr);
        for (SimpleDateFormat format : ISO_DATE_FORMATS) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // try next format
            }
        }
        return new Date(0);
    }

    private static Date parseNullableDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("null")) return null;
        dateStr = trimMicroseconds(dateStr);
        for (SimpleDateFormat format : ISO_DATE_FORMATS) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // try next format
            }
        }
        return null;
    }

    private static String trimMicroseconds(String dateStr) {
        // Date has millisecond precision. Datetimes like "2026-03-06T13:02:01.364324Z" are treated as having extra 364 sec by SimpleDateFormatter.
        // There are no easy to use parsers working with microseconds before API 26
        // This regex turns 2026-03-06T13:02:01.364324+00:00 --> 2026-03-06T13:02:01.364+00:00
        return dateStr.replaceFirst("(\\.\\d{3})\\d+(Z|[+-]\\d{2}:\\d{2})", "$1$2");
    }
}

