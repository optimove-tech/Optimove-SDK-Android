package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import java.util.Collections;

import static android.content.Context.NOTIFICATION_SERVICE;

public class OptipushMessageCommand {

    private Context context;
    private EventHandler eventHandler;
    private DeviceInfoProvider deviceInfoProvider;
    private NotificationCreator notificationCreator;
    private String fullPackageName;

    public OptipushMessageCommand(Context context, EventHandler eventHandler,
                                  DeviceInfoProvider deviceInfoProvider, NotificationCreator notificationCreator) {
        this.context = context;
        this.eventHandler = eventHandler;
        this.deviceInfoProvider = deviceInfoProvider;
        this.notificationCreator = notificationCreator;
        this.fullPackageName = context.getPackageName();
    }

    public void processRemoteMessage(RemoteMessage remoteMessage,
                                     NotificationData notificationData) {
        if (context.getSystemService(NOTIFICATION_SERVICE) == null) {
            OptiLoggerStreamsContainer.fatal("Failed to process an Optipush push notification because the Device's Notifications Manager is null");
            return;
        }

        if (notificationData.getScheduledCampaign() != null) {
            eventHandler.reportEvent(Collections.singletonList(new ScheduledNotificationDeliveredEvent(OptiUtils.currentTimeSeconds(), fullPackageName,
                    notificationData.getScheduledCampaign())));
        } else if (notificationData.getTriggeredCampaign() != null) {
            eventHandler.reportEvent(Collections.singletonList(new TriggeredNotificationDeliveredEvent(OptiUtils.currentTimeSeconds(), fullPackageName,
                    notificationData.getTriggeredCampaign())));
        }


        loadDynamicLinkAndShowNotification(remoteMessage, notificationData);
    }


    /* ******************************
     * Deep Linking
     * ******************************/

    /**
     * Check if a dynamic link exists in the push data and extract the full URL.
     */
    private void loadDynamicLinkAndShowNotification(RemoteMessage remoteMessage, NotificationData notificationData) {
        String dlString = remoteMessage.getData()
                .get(OptipushConstants.PushSchemaKeys.DEEP_LINK);
        if (dlString != null) {
            notificationData.setDynamicLink(dlString);
        }
        showNotificationIfUserIsOptIn(notificationData);
    }

    private void showNotificationIfUserIsOptIn(NotificationData notificationData) {
        boolean optIn = deviceInfoProvider.notificaionsAreEnabled();
        if (!optIn) {
            OptiLoggerStreamsContainer.warn("Optipush Notification blocked since the user is opt out");
            return;
        }
        notificationCreator.showNotification(notificationData);
    }


}
