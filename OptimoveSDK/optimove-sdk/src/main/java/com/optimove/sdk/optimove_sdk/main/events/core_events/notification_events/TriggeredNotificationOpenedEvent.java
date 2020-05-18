package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;

public class TriggeredNotificationOpenedEvent extends NotificationEvent implements OptimoveCoreEvent {

    public static final String NAME = "triggered_notification_opened";

    public TriggeredNotificationOpenedEvent(long timestamp,String packageName, String identityToken) {
        super(timestamp, packageName, identityToken);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
