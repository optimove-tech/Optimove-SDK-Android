package com.optimove.android.gamifywidgetsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

class WidgetBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "GamifyWidget";
    static final String FRAGMENT_TAG = "WidgetBottomSheet";
    private static final String ARG_WIDGET_URL = "widget_url";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_TOKEN = "token";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorView;

    static WidgetBottomSheet newInstance(String widgetUrl,
                                         @Nullable String userId,
                                         @Nullable String token) {
        WidgetBottomSheet sheet = new WidgetBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_WIDGET_URL, widgetUrl);
        if (userId != null) {
            args.putString(ARG_USER_ID, userId);
        }
        if (token != null) {
            args.putString(ARG_TOKEN, token);
        }
        sheet.setArguments(args);
        return sheet;
    }

    private String getWidgetUrl() {
        Bundle args = getArguments();
        return args != null ? args.getString(ARG_WIDGET_URL, "") : "";
    }

    @Nullable
    private String getUserId() {
        Bundle args = getArguments();
        return args != null ? args.getString(ARG_USER_ID) : null;
    }

    @Nullable
    private String getToken() {
        Bundle args = getArguments();
        return args != null ? args.getString(ARG_TOKEN) : null;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout root = new FrameLayout(requireContext());

        webView = new WebView(requireContext());
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.addJavascriptInterface(
                new AndroidBridge(
                        new Runnable() {
                            @Override
                            public void run() {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dismiss();
                                        }
                                    });
                                }
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendInit();
                                        }
                                    });
                                }
                            }
                        }),
                "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showError();
                            }
                        });
                    }
                }
            }
        });

        progressBar = new ProgressBar(requireContext());
        progressBar.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        errorView = new TextView(requireContext());
        errorView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        errorView.setText("Unable to load widget.\nCheck your connection and try again.");
        errorView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorView.setVisibility(View.GONE);

        root.addView(webView);
        root.addView(progressBar);
        root.addView(errorView);

        webView.loadUrl(getWidgetUrl());
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) {
            return;
        }
        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) {
            return;
        }
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    /**
     * Called by AndroidBridge.onReady() when the widget fires its READY signal.
     * Posts the INIT payload back via window.postMessage so the widget bridge picks it up.
     */
    private void sendInit() {
        String initJson = buildInitJson();
        Log.d(TAG, "sending INIT: " + redactedLog(initJson));
        webView.evaluateJavascript("window.postMessage(" + initJson + ", '*');", null);
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

    private String buildInitJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "INIT");
            String userId = getUserId();
            if (userId != null) {
                obj.put("userId", userId);
            }
            String token = getToken();
            if (token != null) {
                obj.put("token", token);
            }
            return obj.toString();
        } catch (JSONException e) {
            return "{\"type\":\"INIT\"}";
        }
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }
}
