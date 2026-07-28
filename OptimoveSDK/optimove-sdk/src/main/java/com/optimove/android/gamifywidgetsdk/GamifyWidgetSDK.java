package com.optimove.android.gamifywidgetsdk;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Entry point for the Gamify Widget SDK.
 *
 * Usage:
 *   GamifyWidgetSDK.initialize("https://your-widget.example.com");
 *   GamifyWidgetSDK.getInstance().open(activity, "u123", "auth-token");
 */
public class GamifyWidgetSDK {

    static final String TAG = "Optimove Gamify";

    private static GamifyWidgetSDK shared;

    private String widgetUrl;
    private WidgetDialog currentDialog;

    public static GamifyWidgetSDK getInstance() {
        if (shared == null) {
            throw new IllegalStateException("GamifyWidgetSDK is not initialized");
        }
        return shared;
    }

    public static void initialize(@NonNull String widgetUrl) {
        shared = new GamifyWidgetSDK();
        shared.widgetUrl = widgetUrl;
    }

    public void open(@NonNull Activity activity) {
        open(activity, null, null);
    }

    public void open(@NonNull Activity activity, @Nullable String userId, @Nullable String token) {
        if (currentDialog != null) {
            return;
        }
        currentDialog = new WidgetDialog(activity, widgetUrl, userId, token, () -> currentDialog = null);
        currentDialog.show();
    }
}
