package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.optimove.sdk.optimove_sdk.OptipushNotificationOpenActivity;
import com.optimove.sdk.optimove_sdk.main.tools.UiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import java.util.concurrent.Executors;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationCreator {

    private final Context context;
    private final NotificationManager notificationManager;
    private final String fullPackageName;

    public NotificationCreator(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.fullPackageName = context.getPackageName();
    }

    public void showNotification(NotificationData notificationData) {
        handleNotificationChannel(notificationData);

        final NotificationCompat.Builder basicNotificationBuilder =
                createBasicNotificationBuilder(notificationData);
        if (notificationData.getNotificationMedia() != null && notificationData.getNotificationMedia().mediaType.equals(OptipushConstants.PushSchemaKeys.MEDIA_TYPE_IMAGE)) {
            Executors.newSingleThreadExecutor()
                    .execute(() -> {
                        Bitmap notificationDecodedBitmap =
                                UiUtils.getBitmapFromURL(notificationData.getNotificationMedia().url);
                        if (notificationDecodedBitmap == null) {
                            OptiLoggerStreamsContainer.error("Failed to get bitmap from url - %s", notificationData.getNotificationMedia().url);
                            presentNotification(applyBigTextStyle(basicNotificationBuilder, notificationData.getBody()).build(), notificationData);
                        } else {
                            presentNotification(applyBigImageStyle(basicNotificationBuilder, notificationDecodedBitmap).build(), notificationData);
                        }
                    });
            return;
        }
        if (notificationData.getNotificationMedia() != null) {
            OptiLoggerStreamsContainer.debug("Notification payload contains media that is not image, image type is: %s",
                    notificationData.getNotificationMedia().mediaType);
        }
        presentNotification(applyBigTextStyle(basicNotificationBuilder, notificationData.getBody()).build(), notificationData);
    }

    private void handleNotificationChannel(NotificationData notificationData) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (notificationData.getChannelInfo() == null) {
            createSdkDefaultNotificationChannel();
            return;
        }

        notificationManager.deleteNotificationChannel(OptipushConstants.Notifications.SDK_NOTIFICATION_CHANNEL_ID);
        if (notificationData.getChannelInfo().channelId != null) {
            createCustomNotificationChannel(notificationData.getChannelInfo().channelId,
                    notificationData.getChannelInfo().channelName);
        }
    }

    private void presentNotification(Notification notification, NotificationData notificationData) {
        if (notificationData.getCollapseKey() == null) {
            notificationManager.notify(OptipushConstants.Notifications.NOTIFICATION_ID, notification);
        } else {
            notificationManager.notify(notificationData.getCollapseKey(), OptipushConstants.Notifications.NOTIFICATION_ID,
                    notification);
        }
    }

    private NotificationCompat.Builder createBasicNotificationBuilder(NotificationData notificationData) {
        NotificationCompat.Builder builder;

        builder = new NotificationCompat.Builder(context,
                notificationData.getChannelInfo() == null ? OptipushConstants.Notifications.SDK_NOTIFICATION_CHANNEL_ID
                        : notificationData.getChannelInfo().channelId);

        builder.setContentTitle(notificationData.getTitle())
                .setContentText(notificationData.getBody())
                .setContentIntent(createClickPendingIntent(notificationData))
                .setSmallIcon(getNotificationIcon())
                .setAutoCancel(true);

        int notificationColor = getNotificationColor();
        if (notificationColor != OptipushConstants.Notifications.INVALID_CUSTOM_COLOR_VALUE) {
            builder.setColor(ContextCompat.getColor(context, notificationColor));
        }

        return builder;
    }

    private NotificationCompat.Builder applyBigTextStyle(NotificationCompat.Builder builder, String text) {
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        return builder;
    }

    private NotificationCompat.Builder applyBigImageStyle(NotificationCompat.Builder builder, Bitmap bitmap) {
        builder.setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        .bigLargeIcon(null));
        return builder;
    }

    private PendingIntent createClickPendingIntent(NotificationData notificationData) {
        Intent intent = new Intent(OptipushConstants.Actions.ACTION_NOTIFICATION_CLICKED).setClass(context,
                OptipushNotificationOpenActivity.class);
        if (notificationData.getScheduledCampaign() != null) {
            intent.putExtra(OptipushConstants.Notifications.SCHEDULED_IDENTITY_TOKEN, notificationData.getScheduledCampaign());
        } else if (notificationData.getTriggeredCampaign() != null) {
            intent.putExtra(OptipushConstants.Notifications.TRIGGERED_IDENTITY_TOKEN, notificationData.getTriggeredCampaign());
        }
        if (notificationData.getDynamicLink() != null) {
            intent.putExtra(OptipushConstants.Notifications.DYNAMIC_LINK, notificationData.getDynamicLink());
        }
        if (notificationData.getRequestId() != null) {
            intent.putExtra(OptipushConstants.Notifications.REQUEST_ID, notificationData.getRequestId());
        }

        int rc;

        if (notificationData.getCollapseKey() != null) {
            rc = (notificationData.getCollapseKey() + "_open").hashCode();
        } else {
            rc = OptipushConstants.Notifications.PENDING_INTENT_OPEN_RC;
        }

        int flags = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ?
                PendingIntent.FLAG_ONE_SHOT :
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;

        return PendingIntent.getActivity(context, rc, intent, flags);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createSdkDefaultNotificationChannel() {
        NotificationChannel channel =
                new NotificationChannel(OptipushConstants.Notifications.SDK_NOTIFICATION_CHANNEL_ID,
                        getApplicationName(), NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createCustomNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel =
                new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }

    private String getApplicationName() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    @DrawableRes
    private int getNotificationIcon() {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(fullPackageName, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle.containsKey(OptipushConstants.Notifications.CUSTOM_ICON_META_DATA_KEY)) {
                return bundle.getInt(OptipushConstants.Notifications.CUSTOM_ICON_META_DATA_KEY);
            }
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            OptiLoggerStreamsContainer.debug("No custom notification icon was found, using default");
        }
        return context.getApplicationInfo().icon;
    }

    @ColorRes
    private int getNotificationColor() {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(fullPackageName, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle.containsKey(OptipushConstants.Notifications.CUSTOM_COLOR_META_DATA_KEY)) {
                return bundle.getInt(OptipushConstants.Notifications.CUSTOM_COLOR_META_DATA_KEY);
            }
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            OptiLoggerStreamsContainer.debug("No custom notification color was found, using default");
        }
        return OptipushConstants.Notifications.INVALID_CUSTOM_COLOR_VALUE;
    }
}
