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

        OptimoveLiveUpdates liveUpdates;
        try {
            liveUpdates = OptimoveLiveUpdates.getInstance();
        } catch (IllegalStateException e) {
            Optimobile.log(TAG, "Live Updates: notification for " + name + " dismissed before Optimove was initialized");
            forwardOriginalDeleteIntent(intent, name);
            return;
        }

        PendingResult result = goAsync();
        Optimobile.executorService.submit(() -> {
            try {
                liveUpdates.endFromNotificationDismissal(name);
            } finally {
                forwardOriginalDeleteIntent(intent, name);
                result.finish();
            }
        });
    }

    private static void forwardOriginalDeleteIntent(Intent intent, String name) {
        @SuppressWarnings("deprecation")
        PendingIntent original = intent.getParcelableExtra(EXTRA_ORIGINAL_DELETE_INTENT);
        if (original == null) {
            return;
        }

        try {
            original.send();
        } catch (PendingIntent.CanceledException e) {
            Optimobile.log(TAG, "Live Updates: original delete intent for " + name + " was cancelled");
        }
    }
}
