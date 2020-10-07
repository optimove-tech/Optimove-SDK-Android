package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.events_dispatch_service.NotificationOpenedEventDispatchService;

public class NotificationInteractionReceiver extends BroadcastReceiver {

  static final String ACTION_DEEPLINK = "com.optimove.sdk.optimove_sdk.DEEPLINK";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null)
      return;

    // Guard against unexpected intents
    if (!intent.hasExtra(OptipushConstants.Notifications.IS_DELETE_KEY)) {
      OptiLoggerStreamsContainer.error("Illegal intent was passed to the %s - no '%s' extra was found!",
          NotificationInteractionReceiver.class.getSimpleName(), OptipushConstants.Notifications.IS_DELETE_KEY);
      return;
    }

    boolean isDelete = intent.getBooleanExtra(OptipushConstants.Notifications.IS_DELETE_KEY, false);
    OptiLoggerStreamsContainer.info("User has responded to Optipush Notification with %s", isDelete ? "dismiss" : "open");
    if (!isDelete) {
      dispatchNotificationOpenedEventToService(context, intent);
      openApplication(context, intent);
    }
  }

  private void dispatchNotificationOpenedEventToService(Context context, Intent intent){
    Intent serviceIntent = new Intent(context, NotificationOpenedEventDispatchService.class);
    serviceIntent.putExtras(intent);
    //start service is allowed here even in Android O, the app is receiving a broadcast and therefore, it is
    // temporarily placed in a whitelist
    try {
      context.startService(serviceIntent);
    } catch (IllegalStateException e) {
      OptiLoggerStreamsContainer.error("Couldn't start notification opened service due to %s", e.getMessage());

    }
  }
  private void openApplication(Context context, Intent intent) {
    if (intent.hasExtra(OptipushConstants.Notifications.DYNAMIC_LINK))
      openActivityWithDynamicLink(context, intent.getStringExtra(OptipushConstants.Notifications.DYNAMIC_LINK));
    else
      openMainActivity(context);
  }

  private void openActivityWithDynamicLink(Context context, String dynamicLink) {
    Intent linkIntent = new Intent(ACTION_DEEPLINK, Uri.parse(dynamicLink));

    if (linkIntent.resolveActivity(context.getPackageManager()) == null) {
      linkIntent.setAction(Intent.ACTION_VIEW);
    }
    linkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    linkIntent.setPackage(context.getPackageName());
    try {
      context.startActivity(linkIntent);
    } catch (Exception e) {
      OptiLoggerStreamsContainer.error("Failed to redirect user to deep link %s due to: %s", dynamicLink, e.getMessage());
      openMainActivity(context);
    }
  }

  private void openMainActivity(Context context) {
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    if (launchIntent == null)
      return;
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(launchIntent);
  }
}
