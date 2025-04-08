package com.optimove.android;

import androidx.annotation.Nullable;

public class OptimoveCredentialManager {
    private String optimoveCredentials;
    private String optimobileCredentials;
    private String preferenceCenterCredentials;
    private String embeddedMessagingConfig;

    public OptimoveCredentialManager(
            @Nullable String optimoveCredentials, @Nullable String optimobileCredentials,
            @Nullable String preferenceCenterCredentials, @Nullable String embeddedMessagingConfig) {
        this.optimoveCredentials = optimoveCredentials;
        this.optimobileCredentials = optimobileCredentials;
        this.preferenceCenterCredentials = preferenceCenterCredentials;
        this.embeddedMessagingConfig = embeddedMessagingConfig;
    }

    public String getOptimoveCredentials() {
        return this.optimoveCredentials;
    }

    public String getOptimobileCredentials() {
        return this.optimobileCredentials;
    }

    public String getPreferenceCenterCredentials() {
        return this.preferenceCenterCredentials;
    }

    public String getEmbeddedMessagingConfig() {
        return this.embeddedMessagingConfig;
    }
}
