package com.optimove.sdk.optimovemobilesdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.optimove.sdk.optimove_sdk.Optimove;
import com.optimove.sdk.optimove_sdk.optimobile.OptimobileConfig;

public class MyApplication extends Application {

  public static void askForOverlayPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
      return;
    Context context = Optimove.getInstance().getApplicationContext();
    Intent intent = new Intent();
    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.setData(Uri.parse("package:" + context.getPackageName()));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    context.startActivity(intent);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Optimove.initialize(this, new OptimobileConfig.Builder(
            null,
            "creds")
            .enableInAppMessaging(OptimobileConfig.InAppConsentStrategy.AUTO_ENROLL)
            .build());
    // Shouldn't be called unless explicitly told to
    Optimove.enableStagingRemoteLogs();

    Optimove.pushRegister(this);
  }
}
