package com.optimove.android.gamifywidgetsdk

import android.util.Log
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
        Log.d(TAG, "closeWidget called")
        onClose()
    }

    @JavascriptInterface
    fun receiveMessage(json: String) {
        Log.d(TAG, "receiveMessage: $json")
        if (json.contains("\"type\":\"READY\"")) {
            Log.d(TAG, "READY received — sending INIT")
            onReady()
        }
    }

    companion object {
        private const val TAG = "GamifyBridge"
    }
}
