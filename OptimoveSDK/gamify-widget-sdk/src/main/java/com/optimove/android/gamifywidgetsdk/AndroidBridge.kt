package com.optimove.android.gamifywidgetsdk

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONException
import org.json.JSONObject

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
        try {
            if (JSONObject(json).getString("type") == "READY") {
                onReady()
            }
        } catch (e: JSONException) {
            Log.d(TAG, "Incorrect message format: $json")
        }
    }

    companion object {
        private const val TAG = "GamifyWidget"
    }
}
