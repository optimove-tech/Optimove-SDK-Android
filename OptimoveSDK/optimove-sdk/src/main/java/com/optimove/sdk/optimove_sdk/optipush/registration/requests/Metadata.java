package com.optimove.sdk.optimove_sdk.optipush.registration.requests;

import com.google.gson.annotations.SerializedName;

public class Metadata {
    @SerializedName("sdk_version")
    private String sdkVersion;
    @SerializedName("app_version")
    private String appVersion;
    @SerializedName("os_version")
    private String osVersion;
    @SerializedName("device_model")
    private String deviceModel;

    public Metadata(String sdkVersion, String appVersion, String osVersion, String deviceModel) {
        this.sdkVersion = sdkVersion;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
        this.deviceModel = deviceModel;
    }
}