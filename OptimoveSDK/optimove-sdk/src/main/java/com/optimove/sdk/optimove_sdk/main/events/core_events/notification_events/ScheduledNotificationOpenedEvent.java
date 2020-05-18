package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;

public final class ScheduledNotificationOpenedEvent extends NotificationEvent implements OptimoveCoreEvent {

  public static final String NAME = "notification_opened";


  public ScheduledNotificationOpenedEvent(long timestamp,String packageName, String identityToken)  {
    super(timestamp, packageName, identityToken);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
