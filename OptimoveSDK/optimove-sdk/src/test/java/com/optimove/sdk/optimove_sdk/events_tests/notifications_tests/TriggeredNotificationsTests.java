package com.optimove.sdk.optimove_sdk.events_tests.notifications_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;

import org.junit.Assert;
import org.junit.Test;

public class TriggeredNotificationsTests {

    private long timestamp = 12323;
    private String packageName = "package_name";
    private String identityToken = "identity_token";
    @Test
    public void triggeredNotificationDeliveredShouldContainTheRightName(){
        TriggeredNotificationDeliveredEvent triggeredNotificationDeliveredEvent =
                new TriggeredNotificationDeliveredEvent(timestamp, packageName, identityToken);

        Assert.assertEquals(TriggeredNotificationDeliveredEvent.NAME, triggeredNotificationDeliveredEvent.getName());
    }
    @Test
    public void triggeredNotificationOpenedShouldContainTheRightName(){
        TriggeredNotificationOpenedEvent triggeredNotificationOpenedEvent =
                new TriggeredNotificationOpenedEvent(timestamp, packageName, identityToken);

        Assert.assertEquals(TriggeredNotificationOpenedEvent.NAME, triggeredNotificationOpenedEvent.getName());
    }

}
