package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.TriggeredCampaign;

public class TriggeredNotificationDeliveredEvent extends TriggeredNotificationEvent implements OptimoveCoreEvent {
    public static final String NAME = "triggered_notification_received";


    public TriggeredNotificationDeliveredEvent(TriggeredCampaign triggeredCampaign, long timestamp,
                                               String packageName) {
        super(triggeredCampaign, timestamp, packageName);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
