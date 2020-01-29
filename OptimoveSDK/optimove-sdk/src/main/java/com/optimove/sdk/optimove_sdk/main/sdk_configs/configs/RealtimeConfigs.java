package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

import android.support.annotation.NonNull;

public class RealtimeConfigs {

    @NonNull
    private String realtimeToken;
    @NonNull
    private String realtimeGateway;

    public RealtimeConfigs() {
        realtimeToken = null;
        realtimeGateway = null;
    }

    @NonNull
    public String getRealtimeToken() {
        return realtimeToken;
    }

    public void setRealtimeToken(@NonNull String realtimeToken) {
        this.realtimeToken = realtimeToken;
    }

    @NonNull
    public String getRealtimeGateway() {
        return realtimeGateway;
    }

    public void setRealtimeGateway(@NonNull String realtimeGateway) {
        this.realtimeGateway = realtimeGateway;
    }

}
