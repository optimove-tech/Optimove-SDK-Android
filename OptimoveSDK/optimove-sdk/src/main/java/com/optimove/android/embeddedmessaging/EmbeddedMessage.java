package com.optimove.android.embeddedmessaging;

import android.annotation.TargetApi;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
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
    private URL media;
    private URL url;
    private Map<String, String> payload;
    private CampaignKind campaignKind;
    private String engagementId;
    private Date executionDateTime;

    public EmbeddedMessage(JSONObject jsonMessage) throws JSONException, URISyntaxException, MalformedURLException {
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
        media = safelyMapUrl(jsonMessage.optString("media"));
        url = safelyMapUrl(jsonMessage.optString("url"));
        campaignKind = CampaignKind.values()[jsonMessage.optInt("campaignKind")];
        engagementId = jsonMessage.optString("engagementId");
        executionDateTime = convertIntToDate(jsonMessage.optLong("executionDateTime"));
    }


    private URL safelyMapUrl(String urlString) throws URISyntaxException, MalformedURLException {
        if(!urlString.isEmpty()) {
            return new URI(urlString).toURL();
        }
        return null;
    }

    private Date convertIntToDate(long unixTimestamp) {
        return new Date(unixTimestamp);
    }
}

