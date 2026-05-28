package com.optimove.android.gamifywidgetsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.R;

import org.json.JSONException;
import org.json.JSONObject;

class WidgetDialog {

    private final Activity activity;
    private final String widgetUrl;
    @Nullable private final String userId;
    @Nullable private final String token;
    private final Runnable onDismissed;
    private final Dialog dialog;

    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorView;

    @SuppressLint("InflateParams")
    WidgetDialog(@NonNull Activity activity,
                 @NonNull String widgetUrl,
                 @Nullable String userId,
                 @Nullable String token,
                 @NonNull Runnable onDismissed) {
        this.activity = activity;
        this.widgetUrl = widgetUrl;
        this.userId = userId;
        this.token = token;
        this.onDismissed = onDismissed;
        this.dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.gamify_widget_dialog, null);
        dialog.setContentView(root, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        webView = dialog.findViewById(R.id.gamify_widget_webview);
        progressBar = dialog.findViewById(R.id.gamify_widget_progress);
        errorView = dialog.findViewById(R.id.gamify_widget_error);

        configureWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.addJavascriptInterface(
                new AndroidBridge(
                        () -> activity.runOnUiThread(this::dismiss),
                        () -> activity.runOnUiThread(this::sendInit)),
                "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                activity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    activity.runOnUiThread(WidgetDialog.this::showError);
                }
            }
        });
    }

    void show() {
        dialog.setOnDismissListener(d -> {
            destroyWebView();
            onDismissed.run();
        });
        webView.loadUrl(widgetUrl);
        dialog.show();
    }

    private void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void destroyWebView() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    /**
     * Called when the widget fires its READY signal. Posts INIT back via window.postMessage
     * so the widget bridge picks up the userId/token.
     */
    private void sendInit() {
        String initJson = buildInitJson();
        Log.d(GamifyWidgetSDK.TAG, "sending INIT: " + redactedLog(initJson));
        webView.evaluateJavascript("window.postMessage(" + initJson + ", '*');", null);
    }

    private String buildInitJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "INIT");
            if (userId != null) {
                obj.put("userId", userId);
            }
            if (token != null) {
                obj.put("token", token);
            }
            return obj.toString();
        } catch (JSONException e) {
            return "{\"type\":\"INIT\"}";
        }
    }

    private String redactedLog(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("token")) {
                obj.put("token", "[REDACTED]");
            }
            return obj.toString();
        } catch (JSONException e) {
            return "[unparseable]";
        }
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }
}
