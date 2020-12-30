package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class NotificationEvent extends OptimoveEvent {

    static final String TIMESTAMP_KEY = "timestamp";
    static final String APP_NS_KEY = "app_ns";
    static final String IDENTITY_TOKEN = "identity_token";

    protected long timestamp;
    protected String packageName;
    protected String identityToken;


    protected NotificationEvent(long timestamp, String packageName, String identityToken,
                                @Nullable String requestId) {
        super(requestId);
        this.timestamp = timestamp;
        this.packageName = packageName;
        this.identityToken = identityToken;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(TIMESTAMP_KEY, timestamp);
        params.put(APP_NS_KEY, packageName);
        params.put(IDENTITY_TOKEN, identityToken);
        return params;
    }


}
