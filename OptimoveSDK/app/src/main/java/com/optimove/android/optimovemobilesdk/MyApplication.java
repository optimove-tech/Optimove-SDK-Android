package com.optimove.android.optimovemobilesdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;

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

    Optimove.initialize(this, new OptimoveConfig.Builder(
            "WyIxIiwgIjgwYTRhMjI0ZGRkMTRhNDQ4MTNlYzIwZmNkMjAxNjE2IiwgIm1vYmlsZS1jb25maWd1cmF0aW9uLjEuMC4wIl0=",
            "WzEsInVrLTEiLCI2YjE5OThhYS1lZmM1LTRjODUtYjg4ZC1mMjQzMTE4ODA1NTAiLCJKcTMxVEJ6dmxmVTQxb2xzMXltQVZTSVdjNXlnY3VmbHpjbysiXQ==")
            .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL)
            .enableEmbeddedMessaging("WyJkZXYiLCAiMzAxMyIsICI5YWJiOGQ2ZC02MmVkLTQyZDEtOTdkMS1jODJkMTVmOWMxZmMiXQ==")
            .build());
    // Shouldn't be called unless explicitly told to
    Optimove.enableStagingRemoteLogs();

    Optimove.getInstance().pushRequestDeviceToken();
  }
}
