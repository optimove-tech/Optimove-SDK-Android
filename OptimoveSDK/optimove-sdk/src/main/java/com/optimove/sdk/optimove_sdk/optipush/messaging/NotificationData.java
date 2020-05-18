package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.support.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public class NotificationData {

    @SerializedName("title")
    private String title;
    @SerializedName("content")
    private String body;
    @Nullable
    private String dynamicLink;
    @SerializedName("triggered_campaign")
    @JsonAdapter(IdentityTokenDeserializer.class)
    @Nullable
    private String triggeredCampaign;
    @SerializedName("scheduled_campaign")
    @JsonAdapter(IdentityTokenDeserializer.class)
    @Nullable
    private String scheduledCampaign;
    @SerializedName("collapse_Key")
    @Nullable
    private String collapseKey;

    @SerializedName("media")
    @Nullable
    private NotificationMedia notificationMedia;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Nullable
    public String getDynamicLink() {
        return dynamicLink;
    }

    public void setDynamicLink(@Nullable String dynamicLink) {
        this.dynamicLink = dynamicLink;
    }

    @Nullable
    public String getTriggeredCampaign() {
        return triggeredCampaign;
    }

    public void setTriggeredCampaign(@Nullable String triggeredCampaign) {
        this.triggeredCampaign = triggeredCampaign;
    }

    @Nullable
    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(@Nullable String collapseKey) {
        this.collapseKey = collapseKey;
    }

    @Nullable
    public NotificationMedia getNotificationMedia() {
        return notificationMedia;
    }

    public void setNotificationMedia(@Nullable NotificationMedia notificationMedia) {
        this.notificationMedia = notificationMedia;
    }

    @Nullable
    public String getScheduledCampaign() {
        return scheduledCampaign;
    }

    public void setScheduledCampaign(@Nullable String scheduledCampaign) {
        this.scheduledCampaign = scheduledCampaign;
    }

    public static class IdentityTokenDeserializer implements JsonDeserializer<String> {

        @Override
        public String deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException {
            return json.toString();
        }
    }
}