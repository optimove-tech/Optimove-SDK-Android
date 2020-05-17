package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;

import java.util.HashMap;
import java.util.Map;

public abstract class ScheduledNotificationEvent extends NotificationEvent {

    ScheduledCampaign scheduledCampaign;

    protected ScheduledNotificationEvent(ScheduledCampaign scheduledCampaign, long timestamp, String packageName) {
        super(timestamp, packageName, new Gson().toJson(scheduledCampaign));
        this.scheduledCampaign = scheduledCampaign;
    }

    @Override
    @NonNull
    public String toString() {
        return  "{"+ scheduledCampaign + " , " +
                "timestamp=" + timestamp +
                '}';

    }
}
