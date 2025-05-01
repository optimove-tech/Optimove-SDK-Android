package com.optimove.android;

import androidx.annotation.Nullable;

public class OptimoveCredentialManager {
    private String optimoveCredentials;
    private String optimobileCredentials;
    private String preferenceCenterCredentials;
    private String embeddedMessagingConfig;

    private OptimoveCredentialManager(Builder builder) {
        this.optimobileCredentials = builder.optimobileCredentials;
        this.optimoveCredentials = builder.optimoveCredentials;
        this.preferenceCenterCredentials = builder.preferenceCenterCredentials;
        this.embeddedMessagingConfig = builder.embeddedMessagingConfig;
    }

    public class Builder {
        private String optimoveCredentials;
        private String optimobileCredentials;
        private String preferenceCenterCredentials;
        private String embeddedMessagingConfig;

        public Builder addOptimoveCredentials(String optimoveCredentials) {
            this.optimoveCredentials = optimoveCredentials;
            return this;
        }
        public Builder addOptimobileCredentials(String optimobileCredentials) {
            this.optimobileCredentials = optimobileCredentials;
            return this;
        }

        public Builder addPreferenceCenterCredentials(String preferenceCenterCredentials) {
            this.preferenceCenterCredentials = preferenceCenterCredentials;
            return this;
        }

        public Builder addEmbeddedMessagingConfig(String embeddedMessagingConfig) {
            this.embeddedMessagingConfig = embeddedMessagingConfig;
            return this;
        }

        public OptimoveCredentialManager build() {
            return new OptimoveCredentialManager(this);
        }
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
