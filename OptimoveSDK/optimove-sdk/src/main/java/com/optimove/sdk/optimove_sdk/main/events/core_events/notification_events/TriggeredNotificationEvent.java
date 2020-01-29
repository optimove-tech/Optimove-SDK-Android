package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.optipush.campaigns.TriggeredCampaign;

import java.util.HashMap;
import java.util.Map;

public abstract class TriggeredNotificationEvent extends NotificationEvent {

    static final String ACTION_ID_KEY = "action_id";
    TriggeredCampaign triggeredCampaign;

    public TriggeredNotificationEvent(TriggeredCampaign triggeredCampaign, long timestamp, String packageName) {
        super(timestamp, packageName);
        this.triggeredCampaign = triggeredCampaign;
    }


    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>(super.getParameters());
        params.put(TEMPLATE_ID_KEY, triggeredCampaign.getTemplateId());
        params.put(ACTION_SERIAL_KEY, triggeredCampaign.getActionSerial());
        params.put(ACTION_ID_KEY, triggeredCampaign.getActionId());
        return params;
    }
    @Override
    @NonNull
    public String toString() {
        return  "{"+ triggeredCampaign + " , " +
                "timestamp=" + timestamp +
                '}';

    }


}
