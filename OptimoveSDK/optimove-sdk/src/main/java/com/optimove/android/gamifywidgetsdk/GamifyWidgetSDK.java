package com.optimove.android.gamifywidgetsdk;

import android.app.Activity;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Entry point for the Gamify Widget SDK (loyalty + Adact).
 *
 * <p>Usage (loyalty):
 * <pre>
 *   GamifyWidgetSDK.initialize("https://your-widget.example.com");
 *   GamifyWidgetSDK.getInstance().open(activity, "u123", "auth-token");
 * </pre>
 *
 * <p>Usage (Adact — {@code adactUrl} from onboarding / retrieval service):
 * <pre>
 *   GamifyWidgetSDK.initialize(widgetUrl, "https://adact-campaign.example.com/");
 *   GamifyWidgetSDK.getInstance().openAdactCampaign(activity,
 *       new OpenAdactParams(179, "cid", "token"));
 * </pre>
 */
public class GamifyWidgetSDK {

    static final String TAG = "Optimove Gamify";

    private static GamifyWidgetSDK shared;

    @Nullable private String widgetUrl;
    @Nullable private String adactUrl;
    @Nullable private WidgetDialog loyaltyDialog;
    @Nullable private WidgetDialog adactDialog;

    public static GamifyWidgetSDK getInstance() {
        if (shared == null) {
            throw new IllegalStateException("GamifyWidgetSDK is not initialized");
        }
        return shared;
    }

    public static void initialize(@NonNull String widgetUrl) {
        initialize(widgetUrl, null);
    }

    /**
     * @param widgetUrl loyalty widget base URL (may be null/blank if only Adact is used)
     * @param adactUrl  Adact campaign host from config (trailing slash optional);
     *                  region-correct URL is supplied by onboarding / retrieval service
     */
    public static void initialize(@Nullable String widgetUrl, @Nullable String adactUrl) {
        shared = new GamifyWidgetSDK();
        shared.widgetUrl = widgetUrl;
        shared.adactUrl = normalizeBaseUrl(adactUrl);
    }

    @NonNull
    public String getAdactUrl() {
        return adactUrl != null ? adactUrl + "/" : "";
    }

    public void open(@NonNull Activity activity) {
        open(activity, null, null);
    }

    public void open(@NonNull Activity activity, @Nullable String userId, @Nullable String token) {
        if (widgetUrl == null || widgetUrl.isEmpty()) {
            return;
        }
        if (loyaltyDialog != null) {
            return;
        }
        // Both surfaces are fullscreen overlays on Android — only one at a time.
        closeAdactCampaign();
        loyaltyDialog = new WidgetDialog(activity, widgetUrl, userId, token, () -> loyaltyDialog = null);
        loyaltyDialog.show();
    }

    /**
     * Opens an Adact embedded campaign overlay.
     * Does not perform the loyalty READY→INIT handshake; identity is passed via URL query params.
     */
    public void openAdactCampaign(@NonNull Activity activity, @NonNull OpenAdactParams params) {
        String campaignUrl = buildAdactCampaignUrl(params);
        if (campaignUrl.isEmpty()) {
            return;
        }
        if (adactDialog != null) {
            return;
        }
        closeWidget();
        adactDialog = new WidgetDialog(
                activity,
                campaignUrl,
                null,
                null,
                false,
                () -> adactDialog = null);
        adactDialog.show();
    }

    /**
     * Builds {@code {adactUrl}/embedded/{campaignId}?cid=&customerIdToken=}.
     * Returns empty string when {@code adactUrl} or {@code campaignId} is missing, or URL is not HTTPS.
     */
    @NonNull
    public String buildAdactCampaignUrl(@NonNull OpenAdactParams params) {
        if (adactUrl == null || adactUrl.isEmpty() || params.campaignId == null) {
            return "";
        }

        try {
            Uri uri = Uri.parse(adactUrl + "/embedded/" + params.campaignId);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return "";
            }

            Uri.Builder builder = uri.buildUpon();
            if (params.cid != null && !params.cid.isEmpty()) {
                builder.appendQueryParameter("cid", params.cid);
            }
            if (params.token != null && !params.token.isEmpty()) {
                builder.appendQueryParameter("customerIdToken", params.token);
            }
            return builder.build().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void closeWidget() {
        if (loyaltyDialog != null) {
            loyaltyDialog.dismiss();
        }
    }

    public void closeAdactCampaign() {
        if (adactDialog != null) {
            adactDialog.dismiss();
        }
    }

    @Nullable
    static String normalizeBaseUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
