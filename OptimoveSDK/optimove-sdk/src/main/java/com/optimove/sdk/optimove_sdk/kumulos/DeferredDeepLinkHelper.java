package com.optimove.sdk.optimove_sdk.kumulos;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.textclassifier.TextClassifier;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ClipDescription.CLASSIFICATION_COMPLETE;
import static android.content.Context.CLIPBOARD_SERVICE;

public class DeferredDeepLinkHelper {
    private static AtomicBoolean continuationHandled;
    static AtomicBoolean nonContinuationLinkCheckedForSession = new AtomicBoolean(false);
    @SuppressWarnings("FieldCanBeLocal")
    private DeepLinkFingerprinter fingerprinter;

    /* package */ DeferredDeepLinkHelper() {
        continuationHandled = new AtomicBoolean(false);
    }

    public class DeepLinkContent {
        DeepLinkContent(@Nullable String title, @Nullable String description) {
            this.title = title;
            this.description = description;
        }

        public @Nullable
        String title;
        public @Nullable
        String description;
    }

    public class DeepLink {
        public String url;
        public DeepLinkContent content;
        public JSONObject data;

        DeepLink(URL url, JSONObject obj) throws JSONException {
            this.url = url.toString();
            this.data = obj.getJSONObject("linkData");

            JSONObject content = obj.getJSONObject("content");
            String title = null;
            String description = null;
            if (content.has("title")) {
                title = content.getString("title");
            }
            if (content.has("description")) {
                description = content.getString("description");
            }
            this.content = new DeepLinkContent(title, description);
        }
    }

    public enum DeepLinkResolution {
        LOOKUP_FAILED,
        LINK_NOT_FOUND,
        LINK_EXPIRED,
        LINK_LIMIT_EXCEEDED,
        LINK_MATCHED
    }

    /* package */ void checkForNonContinuationLinkMatch(Context context) {
        if (this.checkForDeferredLinkOnClipboard(context)) {
            return;
        }

        if (continuationHandled.get()) {
            return;
        }

        this.checkForWebToAppBannerTap(context);
    }

    /* package */ boolean maybeProcessUrl(Context context, String urlStr, boolean wasDeferred) {
        URL url = this.getURL(urlStr);
        if (url == null) {
            return false;
        }

        if (!this.urlShouldBeHandled(url)) {
            return false;
        }

        if (!wasDeferred) {
            continuationHandled.set(true);
        }
        else{
            this.clearClipboard(context);
        }

        this.handleDeepLink(context, url, wasDeferred);
        return true;
    }

    private void clearClipboard(Context context){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            clipboard.clearPrimaryClip();
            return;
        }

        ClipData clipData = ClipData.newPlainText("", "");
        clipboard.setPrimaryClip(clipData);
    }

    private boolean checkForDeferredLinkOnClipboard(Context context) {
        boolean handled = false;

        SharedPreferences preferences = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        boolean checked = preferences.getBoolean(SharedPrefs.DEFERRED_LINK_CHECKED_KEY, false);
        if (checked) {
            return handled;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SharedPrefs.DEFERRED_LINK_CHECKED_KEY, true);
        editor.apply();

        String text = this.getClipText(context);
        if (text == null) {
            return handled;
        }

        handled = this.maybeProcessUrl(context, text, true);

        return handled;
    }

    private @Nullable
    String getClipText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            ClipDescription description = clipboard.getPrimaryClipDescription();
            if (description == null){
                return null;
            }

            if (description.getClassificationStatus() != CLASSIFICATION_COMPLETE){
                return null;
            }

            float score = description.getConfidenceScore(TextClassifier.TYPE_URL);
            if (score != 1){
                return null;
            }
        }

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null) {
            return null;
        }

        if (clip.getItemCount() != 1) {
            return null;
        }

        CharSequence text = clip.getItemAt(0).getText();
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        return text.toString();
    }

    private @Nullable
    URL getURL(String text) {
        boolean isUrl = URLUtil.isValidUrl(text);
        if (!isUrl) {
            return null;
        }

        try {
            return new URL(text);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean urlShouldBeHandled(URL url) {
        String host = url.getHost();
        OptimobileConfig config = Kumulos.getConfig();
        URL cname = config.getDeepLinkCname();

        return host.endsWith("lnk.click") || (cname != null && host.equals(cname.getHost()));
    }

    private void handleDeepLink(Context context, URL url, boolean wasDeferred) {
        OkHttpClient httpClient = Kumulos.getHttpClient();

        String slug = Uri.encode(url.getPath().replaceAll("/$|^/", ""));
        String params = "?wasDeferred=" + (wasDeferred ? 1 : 0);
        String query = url.getQuery();
        if (query != null) {
            params = params + "&" + query;
        }

        String requestUrl = Kumulos.urlBuilder.urlForService(UrlBuilder.Service.DDL, "/v1/deeplinks/" + slug + params);

        final Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader(Kumulos.KEY_AUTH_HEADER, Kumulos.authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        this.makeNetworkRequest(context, httpClient, request, url, wasDeferred);
    }

    private void makeNetworkRequest(Context context, OkHttpClient httpClient, Request request, URL url, boolean wasDeferred) {
        Kumulos.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        DeferredDeepLinkHelper.this.handledSuccessResponse(context, url, wasDeferred, response);
                    } else {
                        DeferredDeepLinkHelper.this.handleFailedResponse(context, url, response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    DeferredDeepLinkHelper.this.invokeDeepLinkHandler(context, DeepLinkResolution.LOOKUP_FAILED, url, null);
                }
            }
        });
    }

    private void handledSuccessResponse(Context context, URL url, boolean wasDeferred, Response response) throws IOException {
        if (response.code() != 200) {
            this.invokeDeepLinkHandler(context, DeepLinkResolution.LOOKUP_FAILED, url, null);
            return;
        }

        try {
            JSONObject data = new JSONObject(response.body().string());
            DeepLink deepLink = new DeepLink(url, data);

            this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_MATCHED, url, deepLink);

            this.trackLinkMatched(context, url, wasDeferred);

        } catch (NullPointerException | JSONException e) {
            this.invokeDeepLinkHandler(context, DeepLinkResolution.LOOKUP_FAILED, url, null);
        }
    }

    private void trackLinkMatched(Context context, URL url, boolean wasDeferred) {
        JSONObject params = new JSONObject();
        try {
            params.put("url", url.toString());
            params.put("wasDeferred", wasDeferred);

            Kumulos.trackEvent(context, AnalyticsContract.EVENT_TYPE_DEEP_LINK_MATCHED, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleFailedResponse(Context context, URL url, Response response) {
        switch (response.code()) {
            case 404:
                this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_NOT_FOUND, url, null);
                break;
            case 410:
                this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_EXPIRED, url, null);
                break;
            case 429:
                this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_LIMIT_EXCEEDED, url, null);
                break;
            default:
                this.invokeDeepLinkHandler(context, DeepLinkResolution.LOOKUP_FAILED, url, null);
                break;
        }
    }

    private void invokeDeepLinkHandler(Context context, DeepLinkResolution resolution, URL url, @Nullable DeepLink data) {
        OptimobileConfig config = Kumulos.getConfig();
        DeferredDeepLinkHandlerInterface handler = config.getDeferredDeepLinkHandler();
        if (handler == null) {
            return;
        }

        Kumulos.handler.post(() -> handler.handle(context, resolution, url.toString(), data));
    }

    //********************************* FINGERPRINTING *********************************

    private void checkForWebToAppBannerTap(Context context) {
        fingerprinter = new DeepLinkFingerprinter(context);

        fingerprinter.getFingerprintComponents(new Kumulos.ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject components) {
                DeferredDeepLinkHelper.this.handleFingerprintComponents(context, components);
            }
        });
    }

    private void handleFingerprintComponents(Context context, JSONObject components) {
        String encodedComponents = Base64.encodeToString(components.toString().getBytes(), Base64.NO_WRAP);
        String requestUrl = Kumulos.urlBuilder.urlForService(UrlBuilder.Service.DDL, "/v1/deeplinks/_taps?fingerprint=" + encodedComponents);

        final Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader(Kumulos.KEY_AUTH_HEADER, Kumulos.authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        Kumulos.getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    DeferredDeepLinkHelper.this.handledFingerprintSuccessResponse(context, response);
                } else {
                    DeferredDeepLinkHelper.this.handledFingerprintFailedResponse(context, response);
                }
            }
        });
    }

    private void handledFingerprintSuccessResponse(Context context, Response response) {
        if (response.code() != 200) {
            response.close();
            return;
        }

        try {
            JSONObject data = new JSONObject(response.body().string());
            String linkUrl = data.getString("linkUrl");
            URL url = new URL(linkUrl);

            DeepLink deepLink = new DeepLink(url, data);

            this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_MATCHED, url, deepLink);

            this.trackLinkMatched(context, url, false);
        } catch (NullPointerException | JSONException | IOException e) {
            // Fingerprint matches that fail to parse correctly can't know the URL so
            // don't invoke any error handler.
            e.printStackTrace();
        } finally {
            response.close();
        }

    }

    private void handledFingerprintFailedResponse(Context context, Response response) {
        int statusCode = response.code();
        if (statusCode != 410 && statusCode != 429) {
            response.close();
            return;
        }

        try {
            JSONObject data = new JSONObject(response.body().string());
            String linkUrl = data.getString("linkUrl");
            URL url = new URL(linkUrl);

            switch (statusCode) {
                case 410:
                    this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_EXPIRED, url, null);
                    break;
                case 429:
                    this.invokeDeepLinkHandler(context, DeepLinkResolution.LINK_LIMIT_EXCEEDED, url, null);
                    break;
            }
        } catch (NullPointerException | JSONException | IOException e) {
            // Fingerprint matches that fail to parse correctly can't know the URL so
            // don't invoke any error handler.

            e.printStackTrace();
        } finally {
            response.close();
        }
    }
}