package com.optimove.android.optimobile;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SessionHelper implements AppStateWatcher.AppStateChangedListener {

    private static final String EVENT_TYPE_FOREGROUND = "k.fg";

    private final WeakReference<Context> mContextRef;
    static AtomicBoolean startNewSession;

    SessionHelper(Context context) {
        startNewSession = new AtomicBoolean(true);
        this.mContextRef = new WeakReference<>(context.getApplicationContext());

        OptimobileInitProvider.getAppStateWatcher().registerListener(this);
    }

    @Override
    public void appEnteredForeground() {
        checkNotificationEnablementForStatusChange();
    }

    @Override
    public void activityAvailable(@NonNull Activity activity) {
        //Not in appEnteredForeground because need activity.
        final Context context = mContextRef.get();
        if (null == context) {
            return;
        }

        if (startNewSession.getAndSet(false)) {
            if (this.isLaunchActivity(context, activity)) {
                DeferredDeepLinkHelper.nonContinuationLinkCheckedForSession.set(false);
            }

            Optimobile.trackEvent(context, SessionHelper.EVENT_TYPE_FOREGROUND, null);

            Optimobile.executorService.submit(() -> {
                WorkManager.getInstance(context).cancelUniqueWork(AnalyticsBackgroundEventWorker.TAG);
            });
        }
    }

    private void checkNotificationEnablementForStatusChange(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        final Context context = mContextRef.get();
        if (null == context) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);

        boolean persistedNotificationsEnabled = prefs.getBoolean(SharedPrefs.KEY_NOTIFICATIONS_ENABLEMENT_STATUS,
                false);
        boolean currentNotificationsEnabled =
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();

        if (persistedNotificationsEnabled == currentNotificationsEnabled) {
            return;
        }

        Optimobile.notificationEnablementStatusChanged(context, currentNotificationsEnabled);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefs.KEY_NOTIFICATIONS_ENABLEMENT_STATUS, currentNotificationsEnabled);
        editor.apply();
    }

    private boolean isLaunchActivity(Context context, Activity activity) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return false;
        }
        ComponentName component = launchIntent.getComponent();
        if (component == null) {
            return false;
        }

        return component.getClassName().equals(activity.getComponentName().getClassName());
    }


    @Override
    public void activityUnavailable(@NonNull Activity activity) {
        //noop
    }

    @Override
    public void appEnteredBackground() {
        final Context context = mContextRef.get();
        if (null == context) {
            return;
        }

        final Data input = new Data.Builder()
                .putLong(AnalyticsBackgroundEventWorker.EXTRAS_KEY_TIMESTAMP, System.currentTimeMillis())
                .build();

        Optimobile.executorService.submit(() -> {
            OptimoveConfig config = Optimove.getConfig();

            OneTimeWorkRequest.Builder taskBuilder = new OneTimeWorkRequest.Builder(AnalyticsBackgroundEventWorker.class)
                    .setInitialDelay(config.getSessionIdleTimeoutSeconds(), TimeUnit.SECONDS)
                    .setInputData(input);

            WorkManager.getInstance(context).enqueueUniqueWork(AnalyticsBackgroundEventWorker.TAG,
                    ExistingWorkPolicy.REPLACE, taskBuilder.build());
        });
    }
}
