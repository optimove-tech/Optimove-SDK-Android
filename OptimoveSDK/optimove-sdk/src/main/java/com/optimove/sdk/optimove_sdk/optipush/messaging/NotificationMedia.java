package com.optimove.sdk.optimove_sdk.optipush.messaging;


import com.google.gson.annotations.SerializedName;

public class NotificationMedia {
    @SerializedName("url")
    public String url;
    @SerializedName("media_type")
    public String mediaType;

    public String getMediaType() {
        return mediaType;
    }
}
