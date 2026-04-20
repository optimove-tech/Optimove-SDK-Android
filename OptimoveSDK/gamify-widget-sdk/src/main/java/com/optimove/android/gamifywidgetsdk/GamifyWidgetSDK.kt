package com.optimove.android.gamifywidgetsdk

import androidx.fragment.app.FragmentManager

/**
 * Entry point for the Gamify Widget SDK.
 *
 * Usage:
 *   GamifyWidgetSDK.init(widgetUrl = "https://your-widget.vercel.app")
 *   GamifyWidgetSDK.open(supportFragmentManager, userId = "u123")
 */
object GamifyWidgetSDK {

    internal var widgetUrl: String = ""
        private set

    fun init(widgetUrl: String) {
        this.widgetUrl = widgetUrl
    }

    /**
     * Opens the widget in a BottomSheet.
     *
     * @param fragmentManager  Activity's supportFragmentManager
     * @param userId           Optional user ID injected into the widget via INIT
     * @param token            Optional auth token injected into the widget via INIT
     */
    fun open(
        fragmentManager: FragmentManager,
        userId: String? = null,
        token: String? = null
    ) {
        val sheet = WidgetBottomSheet.newInstance(
            widgetUrl = widgetUrl,
            userId = userId,
            token = token
        )
        sheet.show(fragmentManager, WidgetBottomSheet.FRAGMENT_TAG)
    }
}
