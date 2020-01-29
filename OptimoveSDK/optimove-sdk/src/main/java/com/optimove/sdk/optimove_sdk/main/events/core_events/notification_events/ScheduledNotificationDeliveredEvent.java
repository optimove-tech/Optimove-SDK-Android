package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;

public final class ScheduledNotificationDeliveredEvent extends ScheduledNotificationEvent implements OptimoveCoreEvent {

  public static final String NAME = "notification_delivered";

  public ScheduledNotificationDeliveredEvent(ScheduledCampaign scheduledCampaign, long timestamp,
                                             String packageName) {
    super(scheduledCampaign, timestamp, packageName);
  }

  @Override
  public String getName() {
    return NAME;
  }

}
