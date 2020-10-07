package com.optimove.sdk.optimove_sdk.optipush_tests.messaging_tests;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotificationManager;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class NotificationCreatorTests {

    //@Mock
    private Context context;
    @Mock
    private NotificationManager notificationManager;
    @Mock
    private ApplicationInfo applicationInfo;

    @Mock
    private NotificationData notificationData;

    private String packageName = "some_package_name";
    private String applicationName = "some_app_name";

    private NotificationCreator notificationCreator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.context = ApplicationProvider.getApplicationContext();
        this.notificationCreator = new NotificationCreator(context);

    }

    @Test
    public void notificationChannelShouldBeCreated() {
        notificationCreator.showNotification(notificationData);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        ShadowNotificationManager shadowNotificationManager = shadowOf(notificationManager);
        Notification notification = shadowNotificationManager.getAllNotifications().get(0);
        Assert.assertEquals(notification.getChannelId(),OptipushConstants.Notifications.SDK_NOTIFICATION_CHANNEL_ID);
    }

    @Test
    public void notificationShouldContainTheRightSmallIcon() {
        notificationCreator.showNotification(notificationData);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        ShadowNotificationManager shadowNotificationManager = shadowOf(notificationManager);
        Notification notification = shadowNotificationManager.getAllNotifications().get(0);
        Assert.assertEquals(notification.icon, context.getApplicationInfo().icon);
    }

}
