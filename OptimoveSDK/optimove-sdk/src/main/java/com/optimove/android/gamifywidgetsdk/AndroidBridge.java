package com.optimove.android.gamifywidgetsdk;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Native bridge exposed to the widget's JavaScript as `window.AndroidBridge`.
 *
 * The widget calls:
 *   window.AndroidBridge.closeWidget()        — to dismiss the bottom sheet
 *   window.AndroidBridge.receiveMessage(json) — generic widget → SDK messages,
 *       including the READY handshake (type = "READY")
 */
class AndroidBridge {

    private static final String TAG = "GamifyWidget";

    private final Runnable onClose;
    private final Runnable onReady;

    AndroidBridge(Runnable onClose, Runnable onReady) {
        this.onClose = onClose;
        this.onReady = onReady;
    }

    @JavascriptInterface
    public void closeWidget() {
        onClose.run();
    }

    @JavascriptInterface
    public void receiveMessage(String json) {
        try {
            if ("READY".equals(new JSONObject(json).getString("type"))) {
                onReady.run();
            }
        } catch (JSONException e) {
            Log.d(TAG, "Incorrect message format: " + json);
        }
    }
}
