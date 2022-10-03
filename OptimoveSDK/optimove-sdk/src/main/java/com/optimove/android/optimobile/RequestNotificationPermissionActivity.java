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

import java.util.Locale;

public class RequestNotificationPermissionActivity extends Activity {

    private static final int REQ_CODE = 1;
    private static final String TAG = RequestNotificationPermissionActivity.class.getName();

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT < 33) {
            finish();
            return;
        }

        boolean shouldShow = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
        Optimobile.log(TAG, "Should show perms req rationale? " + (shouldShow ? "YES" : "NO"));

        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean shouldShow = false;
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            shouldShow = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
        }

        Optimobile.log(TAG, String.format(Locale.getDefault(), "%d %s %d %b", requestCode, permissions[0],
                grantResults[0],
                shouldShow));
        finish();
    }
}
