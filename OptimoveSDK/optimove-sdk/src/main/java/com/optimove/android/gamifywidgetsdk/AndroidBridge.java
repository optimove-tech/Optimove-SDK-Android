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
 *       including READY (loyalty handshake), REFRESH_TOKEN (loyalty token refresh) and CLOSE</li>
 * </ul>
 */
class AndroidBridge {

    private final Runnable onClose;
    @Nullable private final Runnable onReady;
    @Nullable private final Runnable onRefreshToken;

    AndroidBridge(Runnable onClose, @Nullable Runnable onReady, @Nullable Runnable onRefreshToken) {
        this.onClose = onClose;
        this.onReady = onReady;
        this.onRefreshToken = onRefreshToken;
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
            } else if ("REFRESH_TOKEN".equals(type)) {
                if (onRefreshToken != null) {
                    onRefreshToken.run();
                }
            } else if ("CLOSE".equals(type)) {
                onClose.run();
            }
        } catch (JSONException e) {
            Log.d(GamifyWidgetSDK.TAG, "Incorrect message format: " + json);
        }
    }
}
