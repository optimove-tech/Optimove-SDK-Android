package com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events;

import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;

public class TriggeredNotificationDeliveredEvent extends NotificationEvent implements OptimoveCoreEvent {
    public static final String NAME = "triggered_notification_received";


    public TriggeredNotificationDeliveredEvent(long timestamp,String packageName, String identityToken,
                                               @Nullable String requestId) {
        super(timestamp, packageName, identityToken, requestId);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
