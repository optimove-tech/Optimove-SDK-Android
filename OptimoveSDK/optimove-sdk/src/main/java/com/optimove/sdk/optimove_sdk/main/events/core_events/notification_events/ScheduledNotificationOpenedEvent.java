package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;

public final class ScheduledNotificationOpenedEvent extends ScheduledNotificationEvent implements OptimoveCoreEvent {

  public static final String NAME = "notification_opened";


  public ScheduledNotificationOpenedEvent(ScheduledCampaign scheduledCampaign, long timestamp,
                                          String packageName) {
    super(scheduledCampaign, timestamp, packageName);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
