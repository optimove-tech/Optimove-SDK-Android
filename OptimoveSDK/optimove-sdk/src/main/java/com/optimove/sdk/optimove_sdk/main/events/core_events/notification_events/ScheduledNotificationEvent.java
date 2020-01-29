package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;

import java.util.HashMap;
import java.util.Map;

public abstract class ScheduledNotificationEvent extends NotificationEvent {

    static final String CAMPAIGN_ID_KEY = "campaign_id";
    static final String ENGAGEMENT_ID_KEY = "engagement_id";
    static final String CAMPAIGN_TYPE_KEY = "campaign_type";

    ScheduledCampaign scheduledCampaign;

    protected ScheduledNotificationEvent(ScheduledCampaign scheduledCampaign, long timestamp, String packageName) {
        super(timestamp, packageName);
        this.scheduledCampaign = scheduledCampaign;
    }


    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>(super.getParameters());
        params.put(CAMPAIGN_ID_KEY, scheduledCampaign.getCampaignId());
        params.put(TEMPLATE_ID_KEY, scheduledCampaign.getTemplateId());
        params.put(ACTION_SERIAL_KEY, scheduledCampaign.getActionSerial());
        params.put(ENGAGEMENT_ID_KEY, scheduledCampaign.getEngagementId());
        params.put(CAMPAIGN_TYPE_KEY, scheduledCampaign.getCampaignType());
        return params;
    }

    @Override
    @NonNull
    public String toString() {
        return  "{"+ scheduledCampaign + " , " +
                "timestamp=" + timestamp +
                '}';

    }
}
