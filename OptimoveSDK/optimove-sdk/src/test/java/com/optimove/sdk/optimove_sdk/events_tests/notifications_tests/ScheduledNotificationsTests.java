package com.optimove.sdk.optimove_sdk.events_tests.notifications_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ScheduledNotificationsTests {

    private long timestamp = 12323;
    private String packageName = "package_name";
    @Test
    public void scheduledNotificationDeliveredShouldContainTheRightName(){
        ScheduledNotificationDeliveredEvent scheduledNotificationDeliveredEvent =
                new ScheduledNotificationDeliveredEvent(Mockito.mock(ScheduledCampaign.class), timestamp, packageName);

        Assert.assertEquals(ScheduledNotificationDeliveredEvent.NAME, scheduledNotificationDeliveredEvent.getName());
    }
    @Test
    public void scheduledNotificationOpenedShouldContainTheRightName(){
        ScheduledNotificationOpenedEvent scheduledNotificationOpenedEvent =
                new ScheduledNotificationOpenedEvent(Mockito.mock(ScheduledCampaign.class), timestamp, packageName);

        Assert.assertEquals(ScheduledNotificationOpenedEvent.NAME, scheduledNotificationOpenedEvent.getName());
    }

}
