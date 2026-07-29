package com.optimove.android.gamifywidgetsdk;

import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Native bridge exposed to the widget's JavaScript as {@code window.AndroidBridge}.
 *
 * <p>The widget calls:
 * <ul>
 *   <li>{@code window.AndroidBridge.closeWidget()} — dismiss the dialog</li>
 *   <li>{@code window.AndroidBridge.receiveMessage(json)} — widget → SDK messages,
 *       including READY (loyalty handshake) and CLOSE</li>
 * </ul>
 */
class AndroidBridge {

    private final Runnable onClose;
    @Nullable private final Runnable onReady;

    AndroidBridge(Runnable onClose, @Nullable Runnable onReady) {
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
            String type = new JSONObject(json).getString("type");
            if ("READY".equals(type)) {
                if (onReady != null) {
                    onReady.run();
                }
            } else if ("CLOSE".equals(type)) {
                onClose.run();
            }
        } catch (JSONException e) {
            Log.d(GamifyWidgetSDK.TAG, "Incorrect message format: " + json);
        }
    }
}
