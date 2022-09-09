package com.optimove.android.optimobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class RequestNotificationPermissionActivity extends Activity {

    private static final int REQ_CODE = 1;
    private static final String TAG = RequestNotificationPermissionActivity.class.getName();

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= 33) {
            boolean shouldShow = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);

            Log.d(TAG, "Should show perms req rationale? " + (shouldShow ? "YES" : "NO"));

            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_CODE);
        } else {
            finish();
        }
    }

    static void request(Context context) {
        // TODO: handle multiple requests / app lifecycle changes
        int granted = PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 33) {
            granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (granted == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Optimobile.handler.post(() -> {
            OptimobileInitProvider.getAppStateWatcher().registerListener(new AppStateWatcher.AppStateChangedListener() {
                @Override
                public void appEnteredForeground() {

                }

                @Override
                public void activityAvailable(@NonNull Activity activity) {
                    Intent intent = new Intent(context, RequestNotificationPermissionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    OptimobileInitProvider.getAppStateWatcher().unregisterListener(this);
                }

                @Override
                public void activityUnavailable(@NonNull Activity activity) {

                }

                @Override
                public void appEnteredBackground() {

                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean shouldShow = false;
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            shouldShow = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
        }

        Log.d(TAG, String.format("%d %s %d %b", requestCode, permissions[0], grantResults[0], shouldShow));

        finish();
    }
}
