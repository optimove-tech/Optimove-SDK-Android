package com.optimove.android.optimobile;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class LiveUpdateDismissReceiver extends BroadcastReceiver {

    private static final String TAG = LiveUpdateDismissReceiver.class.getName();

    static final String EXTRA_NAME = "com.optimove.android.optimobile.LIVE_UPDATE_NAME";
    static final String EXTRA_ORIGINAL_DELETE_INTENT = "com.optimove.android.optimobile.LIVE_UPDATE_ORIGINAL_DELETE_INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String name = intent.getStringExtra(EXTRA_NAME);
        if (name == null) {
            return;
        }

        try {
            OptimoveLiveUpdates.getInstance().endFromNotificationDismissal(name);
        } catch (IllegalStateException e) {
            Optimobile.log(TAG, "Live Updates: notification for " + name + " dismissed before Optimove was initialized");
        }

        @SuppressWarnings("deprecation")
        PendingIntent original = intent.getParcelableExtra(EXTRA_ORIGINAL_DELETE_INTENT);
        if (original != null) {
            try {
                original.send();
            } catch (PendingIntent.CanceledException e) {
                Optimobile.log(TAG, "Live Updates: original delete intent for " + name + " was cancelled");
            }
        }
    }
}
