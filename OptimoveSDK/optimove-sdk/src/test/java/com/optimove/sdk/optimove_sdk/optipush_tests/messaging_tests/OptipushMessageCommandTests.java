package com.optimove.sdk.optimove_sdk.optipush_tests.messaging_tests;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.TriggeredCampaign;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
//@Config(sdk = Build.VERSION_CODES.P)
public class OptipushMessageCommandTests {

    @Mock
    private RemoteMessage remoteMessage;
    @Mock
    private Map<String,String> remoteMessageData;
    @Mock
    private NotificationData notificationData;
    @Mock
    private EventHandler eventHandler;
    @Mock
    private DeviceInfoProvider deviceInfoProvider;
    @Mock
    private NotificationCreator notificationCreator;

    @Mock
    private ScheduledCampaign scheduledCampaign;
    @Mock
    private TriggeredCampaign triggeredCampaign;


    private int executionTimeInMilliseconds = 54665;

    private OptipushMessageCommand optipushMessageCommand;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        optipushMessageCommand = new OptipushMessageCommand(ApplicationProvider.getApplicationContext(),eventHandler, deviceInfoProvider,
                notificationCreator);
        when(remoteMessage.getData()).thenReturn(remoteMessageData);
        when(remoteMessageData.get(OptipushConstants.PushSchemaKeys.DYNAMIC_LINKS)).thenReturn("some_dl_string");
    }

    @Test
    public void eventContextShouldContainScheduledCampaignWhenNotificationDataContainsIt() {
        when(notificationData.getScheduledCampaign()).thenReturn(scheduledCampaign);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verify(eventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent().getName(),
                ScheduledNotificationDeliveredEvent.NAME)));
    }
    @Test
    public void eventContextShouldContainTriggeredCampaignWhenNotificationDataContainsIt() {
        when(notificationData.getTriggeredCampaign()).thenReturn(triggeredCampaign);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verify(eventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent().getName(),
                TriggeredNotificationDeliveredEvent.NAME)));
    }
    @Test
    public void shouldCallShowNotificationIfNotificationsAreEnabled() {
        when(notificationData.getScheduledCampaign()).thenReturn(scheduledCampaign);
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verify(notificationCreator).showNotification(notificationData);
    }
    @Test
    public void shouldCallShowNotificationIfNotificationsAreEnabledAndDlStringIsNull() {
        when(notificationData.getScheduledCampaign()).thenReturn(scheduledCampaign);
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        when(remoteMessageData.get(OptipushConstants.PushSchemaKeys.DYNAMIC_LINKS)).thenReturn(null);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verify(notificationCreator).showNotification(notificationData);
    }
    @Test
    public void shouldntCallShowNotificationIfNotificationsAreNotEnabled() {
        when(notificationData.getScheduledCampaign()).thenReturn(scheduledCampaign);
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(false);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verifyZeroInteractions(notificationCreator);
    }
    @Test
    public void shouldDoNothingIfContextDoesntContainSystemService() {
        Context context = mock(Context.class);
        OptipushMessageCommand optipushMessageCommand = new OptipushMessageCommand(context,eventHandler,
                deviceInfoProvider,
                notificationCreator);
        when(notificationData.getScheduledCampaign()).thenReturn(scheduledCampaign);
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);

        optipushMessageCommand.processRemoteMessage(executionTimeInMilliseconds,remoteMessage,notificationData);
        verifyZeroInteractions(notificationCreator);
        verifyZeroInteractions(eventHandler);

    }
//    @Test
//    public void dfgdg() {
//        optipushMessageCommand.getPersonalizedDeepLink()
//
//    }

}
