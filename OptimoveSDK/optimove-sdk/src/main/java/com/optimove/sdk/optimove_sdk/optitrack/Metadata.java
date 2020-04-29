package com.optimove.sdk.optimove_sdk.optitrack;

import com.google.gson.annotations.SerializedName;

public class Metadata {
    @SerializedName("sdk_version")
    private String sdkVersion;
    @SerializedName("sdk_platform")
    private String sdkPlatform;

    public Metadata(String sdkVersion, String sdkPlatform) {
        this.sdkVersion = sdkVersion;
        this.sdkPlatform = sdkPlatform;
    }
}