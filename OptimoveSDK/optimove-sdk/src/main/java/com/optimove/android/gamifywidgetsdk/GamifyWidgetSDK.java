package com.optimove.android.gamifywidgetsdk;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

/**
 * Entry point for the Gamify Widget SDK.
 *
 * Usage:
 *   GamifyWidgetSDK.init("https://your-widget.example.com");
 *   GamifyWidgetSDK.open(supportFragmentManager, "u123", null);
 */
public final class GamifyWidgetSDK {

    private static volatile String widgetUrl = "";

    private GamifyWidgetSDK() {}

    public static void init(String widgetUrl) {
        GamifyWidgetSDK.widgetUrl = widgetUrl;
    }

    static String getWidgetUrl() {
        return widgetUrl;
    }

    public static void open(FragmentManager fragmentManager) {
        open(fragmentManager, null, null);
    }

    /**
     * Opens the widget in a BottomSheet.
     *
     * @param fragmentManager  Activity's supportFragmentManager
     * @param userId           Optional user ID injected into the widget via INIT
     * @param token            Optional auth token injected into the widget via INIT
     */
    public static void open(FragmentManager fragmentManager,
                            @Nullable String userId,
                            @Nullable String token) {
        WidgetBottomSheet sheet = WidgetBottomSheet.newInstance(widgetUrl, userId, token);
        sheet.show(fragmentManager, WidgetBottomSheet.FRAGMENT_TAG);
    }
}
