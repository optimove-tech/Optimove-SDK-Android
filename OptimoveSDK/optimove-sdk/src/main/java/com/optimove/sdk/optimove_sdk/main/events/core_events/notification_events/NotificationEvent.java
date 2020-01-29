package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class NotificationEvent implements OptimoveEvent {

    static final String TIMESTAMP_KEY = "timestamp";
    static final String APP_NS_KEY = "app_ns";
    static final String ACTION_SERIAL_KEY = "action_serial";
    static final String TEMPLATE_ID_KEY = "template_id";

    protected long timestamp;
    protected String packageName;


    protected NotificationEvent(long timestamp, String packageName) {
        this.timestamp = timestamp;
        this.packageName = packageName;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(TIMESTAMP_KEY, timestamp);
        params.put(APP_NS_KEY, packageName);
        return params;
    }


}
