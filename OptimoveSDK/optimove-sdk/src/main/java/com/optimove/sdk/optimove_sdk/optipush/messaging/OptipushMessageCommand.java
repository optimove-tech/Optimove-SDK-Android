package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import com.google.firebase.FirebaseApp;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.TriggeredNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import static android.content.Context.NOTIFICATION_SERVICE;

public class OptipushMessageCommand {

    private Context context;
    private EventHandler eventHandler;
    private RequirementProvider requirementProvider;
    private NotificationCreator notificationCreator;
    private String fullPackageName;

    public OptipushMessageCommand(Context context, EventHandler eventHandler,
                                  RequirementProvider requirementProvider, NotificationCreator notificationCreator) {
        this.context = context;
        this.eventHandler = eventHandler;
        this.requirementProvider = requirementProvider;
        this.notificationCreator = notificationCreator;
        this.fullPackageName = context.getPackageName();
    }

    public void processRemoteMessage(int executionTimeInMilliseconds, RemoteMessage remoteMessage,
                        NotificationData notificationData) {
        if (context.getSystemService(NOTIFICATION_SERVICE) == null) {
            OptiLogger.optipushFailedToProcessNotification_WhenNotificationManagerIsNull();
            return;
        }

        if (notificationData.getScheduledCampaign() != null) {
            eventHandler.reportEvent(new EventContext(new ScheduledNotificationDeliveredEvent(notificationData.getScheduledCampaign(),
                            System.currentTimeMillis(), fullPackageName),
                            executionTimeInMilliseconds));
        } else if (notificationData.getTriggeredCampaign() != null) {
            eventHandler.reportEvent(new EventContext(new TriggeredNotificationDeliveredEvent(notificationData.getTriggeredCampaign(),
                            System.currentTimeMillis(), fullPackageName), executionTimeInMilliseconds
                    ));
        }


        loadDynamicLinkAndShowNotification(remoteMessage, notificationData);
    }


    /* ******************************
     * Deep Linking
     * ******************************/
    /**
     * Check if a dynamic link exists in the push data and extract the full URL.
     *
     */
    public void loadDynamicLinkAndShowNotification(RemoteMessage remoteMessage, NotificationData notificationData) {
        String dlString = remoteMessage.getData()
                .get(OptipushConstants.PushSchemaKeys.DYNAMIC_LINKS);
        if (dlString == null) {
            showNotificationIfUserIsOptIn(notificationData);
            return;
        }
        if (FirebaseApp.getApps(context)
                .isEmpty()) {
            OptiLogger.optipushFailedToGetDeepLinkFromDynamicLink(dlString, "No FirebaseApps are available");
            showNotificationIfUserIsOptIn(notificationData);
            return;
        }
        try {
            JSONObject dlJson = new JSONObject(dlString);
            String shortDynamicLink = dlJson.getJSONObject(OptipushConstants.PushSchemaKeys.ANDROID_DYNAMIC_LINKS)
                    .getString(fullPackageName);
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(Uri.parse(shortDynamicLink))
                    .addOnSuccessListener(dynamicLink -> {
                        if (dynamicLink != null && dynamicLink.getLink() != null) {
                            String deepLink = dynamicLink.getLink()
                                    .toString();
                            String deepLinkPersonalizationValues = remoteMessage.getData()
                                    .get(OptipushConstants.PushSchemaKeys.DEEP_LINK_PERSONALIZATION_VALUES);
                            if (deepLinkPersonalizationValues != null) {
                                deepLink = getPersonalizedDeepLink(deepLink, deepLinkPersonalizationValues);
                            }
                            notificationData.setDynamicLink(deepLink);
                        }
                        showNotificationIfUserIsOptIn(notificationData);
                    })
                    .addOnFailureListener(e -> {
                        OptiLogger.optipushFailedToGetDeepLinkFromDynamicLink(remoteMessage.getData()
                                .get(OptipushConstants.PushSchemaKeys.DYNAMIC_LINKS), e.getMessage());
                        showNotificationIfUserIsOptIn(notificationData);
                    });
        } catch (JSONException e) {
            OptiLogger.optipushFailedToGetDeepLinkFromDynamicLink(dlString, "No valid Dynamic Link was found");
            showNotificationIfUserIsOptIn(notificationData);
        }
    }

    @VisibleForTesting
    private String getPersonalizedDeepLink(String deepLink, String deepLinkPersonalizationValues) {
        try {
            JSONObject json = new JSONObject(deepLinkPersonalizationValues);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!json.isNull(key)) {
                    deepLink =
                            deepLink.replace(URLEncoder.encode(key, "utf8"), URLEncoder.encode(json.getString(key), "utf8"));
                }
            }
        } catch (JSONException e) {
            OptiLogger.optipushDeepLinkPersonalizationValuesBadJson();
        } catch (UnsupportedEncodingException e) {
            OptiLogger.optipushDeepLinkFailedToDecodeLinkParam();
        }
        return deepLink;
    }


    /* ******************************
     * Notification Builder
     * ******************************/

    private void showNotificationIfUserIsOptIn(NotificationData notificationData) {
        boolean optIn = requirementProvider.notificaionsAreEnabled();
        if (!optIn) {
            OptiLogger.optipushNotificationNotPresented_WhenUserIdOptOut();
            return;
        }
        notificationCreator.showNotification(notificationData);
    }



}
