package com.optimove.android.gamifywidgetsdk

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GamifyWidgetSDKTest {

    @Before
    fun setUp() {
        // Reset state between tests via init
        GamifyWidgetSDK.init(widgetUrl = "")
    }

    @Test
    fun `init stores widgetUrl correctly`() {
        val url = "https://gamify-widget.vercel.app"
        GamifyWidgetSDK.init(widgetUrl = url)
        assertEquals(url, GamifyWidgetSDK.widgetUrl)
    }

    @Test
    fun `init overwrites previous url`() {
        GamifyWidgetSDK.init(widgetUrl = "https://first.example.com")
        GamifyWidgetSDK.init(widgetUrl = "https://second.example.com")
        assertEquals("https://second.example.com", GamifyWidgetSDK.widgetUrl)
    }
}
