package com.optimove.sdk.optimove_sdk.events_tests.notifications_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.TriggeredCampaign;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TriggeredNotificationsTests {

    private long timestamp = 12323;
    private String packageName = "package_name";
    @Test
    public void triggeredNotificationDeliveredShouldContainTheRightName(){
        TriggeredNotificationDeliveredEvent triggeredNotificationDeliveredEvent =
                new TriggeredNotificationDeliveredEvent(Mockito.mock(TriggeredCampaign.class), timestamp, packageName);

        Assert.assertEquals(TriggeredNotificationDeliveredEvent.NAME, triggeredNotificationDeliveredEvent.getName());
    }
    @Test
    public void triggeredNotificationOpenedShouldContainTheRightName(){
        TriggeredNotificationOpenedEvent triggeredNotificationOpenedEvent =
                new TriggeredNotificationOpenedEvent(Mockito.mock(TriggeredCampaign.class), timestamp, packageName);

        Assert.assertEquals(TriggeredNotificationOpenedEvent.NAME, triggeredNotificationOpenedEvent.getName());
    }

}
