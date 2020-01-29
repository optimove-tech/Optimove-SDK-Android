package com.optimove.sdk.optimove_sdk.optipush.events_dispatch_service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.ScheduledCampaign;
import com.optimove.sdk.optimove_sdk.optipush.campaigns.TriggeredCampaign;

import java.util.concurrent.TimeUnit;

public class NotificationOpenedEventDispatchService extends Service {

    @Nullable
    private ScheduledCampaign scheduledCampaign;
    @Nullable
    private TriggeredCampaign triggeredCampaign;

    private static int executionTimeInMilliseconds = (int) TimeUnit.SECONDS.toMillis(5);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(OptipushConstants.Notifications.SCHEDULED_CAMPAIGN_CARD)) {
            scheduledCampaign = intent.getParcelableExtra(OptipushConstants.Notifications.SCHEDULED_CAMPAIGN_CARD);
        } else if (intent.hasExtra(OptipushConstants.Notifications.TRIGGERED_CAMPAIGN_CARD)) {
            triggeredCampaign = intent.getParcelableExtra(OptipushConstants.Notifications.TRIGGERED_CAMPAIGN_CARD);
        }

        Optimove.configureUrgently(this);

        if (triggeredCampaign != null) {
            Optimove.getInstance()
                    .getEventHandlerProvider()
                    .getEventHandler()
                    .reportEvent(new EventContext(new TriggeredNotificationOpenedEvent(triggeredCampaign,
                            System.currentTimeMillis(), ApplicationHelper.getFullPackageName(this)),
                            executionTimeInMilliseconds));
        } else if (scheduledCampaign != null) {
            Optimove.getInstance()
                    .getEventHandlerProvider()
                    .getEventHandler()
                    .reportEvent(new EventContext(new ScheduledNotificationOpenedEvent(scheduledCampaign,
                            System.currentTimeMillis(), ApplicationHelper.getFullPackageName(this)),
                            executionTimeInMilliseconds));
        }
        new Thread(() -> {
            try {
                //wait until the event will be handled
                Thread.sleep(executionTimeInMilliseconds + (int) TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException e) {
                OptiLogger.f149();
            } finally {
                stopSelf();
            }
            stopSelf();
        }).start();

        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}