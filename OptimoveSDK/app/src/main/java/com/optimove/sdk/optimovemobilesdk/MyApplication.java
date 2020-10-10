package com.optimove.sdk.optimovemobilesdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

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
    if (!BuildConfig.DEBUG) {
      OptiLoggerStreamsContainer.addOutputStream(FileOptiLoggerOutputStream.getInstance(this, new FileUtils()));
    }
    registerActivityLifecycleCallbacks(new MyActivitiesListener());
    TenantInfo tenantInfo = new TenantInfo("internal-token", "dev");
    Optimove.enableStagingRemoteLogs();
    Optimove.configure(this, tenantInfo, LogLevel.DEBUG);
  }

  /**
   * Temp flags for testing/dev/automation build mode. should be a gradle script
   */

  private class MyActivitiesListener implements ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
      for (OptiLoggerOutputStream outputStream : OptiLoggerStreamsContainer.getLoggerOutputStreams()) {
        if (outputStream instanceof FileOptiLoggerOutputStream) {
          FileOptiLoggerOutputStream.getInstance(MyApplication.this, new FileUtils()).save();
        }
      }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
  }
}
