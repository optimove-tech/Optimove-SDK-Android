package com.optimove.android.gamifywidgetsdk

import android.webkit.JavascriptInterface

/**
 * Native bridge exposed to the widget's JavaScript as `window.AndroidBridge`.
 *
 * The widget calls:
 *   window.AndroidBridge.closeWidget()        — to dismiss the bottom sheet
 *   window.AndroidBridge.receiveMessage(json) — generic widget → SDK messages,
 *       including the READY handshake (type = "READY")
 */
internal class AndroidBridge(
    private val onClose: () -> Unit,
    private val onReady: () -> Unit
) {

    @JavascriptInterface
    fun closeWidget() {
        onClose()
    }

    @JavascriptInterface
    fun receiveMessage(json: String) {
        if (json.contains("\"type\":\"READY\"")) {
            onReady()
        }
    }
}
