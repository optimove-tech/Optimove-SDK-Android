package com.optimove.sdk.optimove_sdk.optipush.events_dispatch_service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationOpenedEvent;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NotificationOpenedEventDispatchService extends Service {

    @Nullable
    private String triggeredIdentityToken;
    @Nullable
    private String scheduledIdentityToken;

    private static int executionTimeInMilliseconds = (int) TimeUnit.SECONDS.toMillis(5);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(OptipushConstants.Notifications.SCHEDULED_IDENTITY_TOKEN)) {
            scheduledIdentityToken = intent.getStringExtra(OptipushConstants.Notifications.SCHEDULED_IDENTITY_TOKEN);
        } else if (intent.hasExtra(OptipushConstants.Notifications.TRIGGERED_IDENTITY_TOKEN)) {
            triggeredIdentityToken = intent.getParcelableExtra(OptipushConstants.Notifications.TRIGGERED_IDENTITY_TOKEN);
        }

        Optimove.configureUrgently(this);

        if (triggeredIdentityToken != null) {
            Optimove.getInstance()
                    .getEventHandlerProvider()
                    .getEventHandler()
                    .reportEvent(Collections.singletonList(new TriggeredNotificationOpenedEvent(OptiUtils.currentTimeSeconds(),
                            ApplicationHelper.getFullPackageName(this), triggeredIdentityToken)));
        } else if (scheduledIdentityToken != null) {
            Optimove.getInstance()
                    .getEventHandlerProvider()
                    .getEventHandler()
                    .reportEvent(Collections.singletonList(new ScheduledNotificationOpenedEvent(OptiUtils.currentTimeSeconds(),
                            ApplicationHelper.getFullPackageName(this), scheduledIdentityToken)));
        }
        new Thread(() -> {
            try {
                //wait until the event will be handled
                Thread.sleep(executionTimeInMilliseconds + (int) TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException e) {
                OptiLoggerStreamsContainer.warn("Thread.sleep after dispatching event was interrupted");
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