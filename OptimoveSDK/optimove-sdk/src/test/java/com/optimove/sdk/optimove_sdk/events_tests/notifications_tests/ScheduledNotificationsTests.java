package com.optimove.sdk.optimove_sdk.events_tests.notifications_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;

import org.junit.Assert;
import org.junit.Test;

public class ScheduledNotificationsTests {

    private long timestamp = 12323;
    private String packageName = "package_name";
    private String identityToken = "identity_token";
    @Test
    public void scheduledNotificationDeliveredShouldContainTheRightName(){
        ScheduledNotificationDeliveredEvent scheduledNotificationDeliveredEvent =
                new ScheduledNotificationDeliveredEvent(timestamp, packageName, identityToken);

        Assert.assertEquals(ScheduledNotificationDeliveredEvent.NAME, scheduledNotificationDeliveredEvent.getName());
    }
    @Test
    public void scheduledNotificationOpenedShouldContainTheRightName(){
        ScheduledNotificationOpenedEvent scheduledNotificationOpenedEvent =
                new ScheduledNotificationOpenedEvent(timestamp, packageName, identityToken);

        Assert.assertEquals(ScheduledNotificationOpenedEvent.NAME, scheduledNotificationOpenedEvent.getName());
    }

}
