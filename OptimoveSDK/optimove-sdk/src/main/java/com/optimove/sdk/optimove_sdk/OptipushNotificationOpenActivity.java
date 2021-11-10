package com.optimove.sdk.optimove_sdk;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.events_dispatch_service.NotificationOpenedEventDispatchService;

import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Actions.ACTION_DEEPLINK;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Actions.ACTION_NOTIFICATION_CLICKED;

public class OptipushNotificationOpenActivity extends AppCompatActivity {
    
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        if (intent == null || intent.getAction() == null || !intent.getAction().equals(ACTION_NOTIFICATION_CLICKED)) {
            OptiLoggerStreamsContainer.error("The received intent is either null or has an illegal action");
            finish();
            return;
        }

        OptiLoggerStreamsContainer.info("User has responded to Optipush Notification with open");
        dispatchNotificationOpenedEventIfNeeded(intent);
        launchNextActivity(intent);
        finish();
    }


    private void launchNextActivity(Intent intent) {
        if (intent.hasExtra(OptipushConstants.Notifications.DYNAMIC_LINK)) {
            openActivityWithDynamicLink(this, intent.getStringExtra(OptipushConstants.Notifications.DYNAMIC_LINK));
            return;
        }
        openMainActivity(this);
    }

    private void openActivityWithDynamicLink(Context context, String dynamicLink) {
        Intent linkIntent = new Intent(ACTION_DEEPLINK, Uri.parse(dynamicLink));

        linkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        linkIntent.setPackage(context.getPackageName());
        try {
            context.startActivity(linkIntent);
        } catch (ActivityNotFoundException e) {
            OptiLoggerStreamsContainer.warn("Was not able to open an activity using the com.optimove.sdk" +
                    ".optimove_sdk.DEEPLINK action, about to try the legacy action");
            startActivityWithLegacyAction(context, linkIntent);
        }
    }

    //backward compatibility for intent filters prior to ACTION_DEEPLINK
    private void startActivityWithLegacyAction(Context context, Intent intent) {
        intent.setAction(Intent.ACTION_VIEW);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            OptiLoggerStreamsContainer.error("Was not able to open the deep link due to - %s, opening the main " +
                    "activity", e.getMessage());
            openMainActivity(context);
        }
    }

    private void openMainActivity(Context context) {
        Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent == null) {
            return;
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(launchIntent);
    }

    private void dispatchNotificationOpenedEventIfNeeded(Intent intent){
        if (! intent.hasExtra(OptipushConstants.Notifications.SCHEDULED_IDENTITY_TOKEN)
                && !intent.hasExtra(OptipushConstants.Notifications.TRIGGERED_IDENTITY_TOKEN)){
            OptiLoggerStreamsContainer.info("No identity token found, not going to send the open event");
            return;
        }
        Context context = getApplicationContext();
        Intent serviceIntent = new Intent(context, NotificationOpenedEventDispatchService.class);
        serviceIntent.putExtras(getIntent());
        try {
            context.startService(serviceIntent);
        } catch (IllegalStateException e) {
            OptiLoggerStreamsContainer.error("Couldn't start notification opened service due to %s", e.getMessage());
        }
    }

}