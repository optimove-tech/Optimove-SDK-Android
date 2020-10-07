package com.optimove.sdk.optimove_sdk.main.app_update_listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

/**
 * When the hosting Application was {@code updated}, this broadcast is notified. <br>
 * Then it passes the call to {@link AppUpdateService} who is responsible to initialize the SDK properly, allowing an initial background silent initialization.
 */
public class AppUpdateReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null && action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))
      AppUpdateService.enqueueWork(context, intent);
    else
      OptiLoggerStreamsContainer.error("Suspicious action %s was received by the AppUpdateReceiver", action);
  }
}
