package com.optimove.android.gamifywidgetsdk;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.optimove.android.AuthManager;
import com.optimove.android.Optimove;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Gamify Widget SDK (loyalty + Adact).
 *
 * <p>Both {@code loyaltyWidgetUrl} and {@code adactUrl} come from the tenant configuration; there is nothing to
 * initialize.
 *
 * <p>Usage (loyalty — opens {@code {loyaltyWidgetUrl}/{tenantId}/{userId}} for the identified SDK user; when auth
 * is enabled via {@code OptimoveConfig.Builder#enableAuth}, a JWT is minted for that user and passed in INIT):
 * <pre>
 *   GamifyWidgetSDK.getInstance().openWidget(activity);
 *   GamifyWidgetSDK.getInstance().openWidget(activity, new OpenLoyaltyWidgetParams("u123", "brand"));
 * </pre>
 *
 * <p>Usage (Adact):
 * <pre>
 *   GamifyWidgetSDK.getInstance().openAdactCampaign(activity,
 *       new OpenAdactParams(179, "cid", "token"));
 * </pre>
 */
public class GamifyWidgetSDK {

    static final String TAG = "Optimove Gamify";

    interface Environment {
        @Nullable
        String currentUserId();

        @Nullable
        AuthManager authManager();
    }

    private static final Environment DEFAULT_ENVIRONMENT = new Environment() {
        @Nullable
        @Override
        public String currentUserId() {
            try {
                return Optimove.getInstance().getUserInfo().getUserId();
            } catch (IllegalStateException e) {
                return null; // Optimove not initialized
            }
        }

        @Nullable
        @Override
        public AuthManager authManager() {
            return Optimove.getAuthManager();
        }
    };

    private static GamifyWidgetSDK shared;

    @NonNull private static volatile TenantOptions tenantOptions = TenantOptions.EMPTY;

    @NonNull private static Environment environment = DEFAULT_ENVIRONMENT;

    // Set only through the deprecated initialize(...) overloads.
    @Nullable private static volatile String legacyWidgetUrl;
    @Nullable private static volatile String legacyAdactUrl;

    @Nullable private WidgetDialog loyaltyDialog;
    @Nullable private WidgetDialog adactDialog;

    @NonNull
    public static synchronized GamifyWidgetSDK getInstance() {
        if (shared == null) {
            shared = new GamifyWidgetSDK();
        }
        return shared;
    }

    /**
     * Applies the {@code gamify} block of the tenant configuration. Called by the SDK when the configuration is
     * fetched; not intended for host apps.
     */
    public static void applyTenantConfiguration(@Nullable String adactUrl,
                                                @Nullable String loyaltyWidgetUrl,
                                                int tenantId) {
        tenantOptions = new TenantOptions(normalizeBaseUrl(adactUrl), normalizeBaseUrl(loyaltyWidgetUrl), tenantId);
        Log.d(TAG, "Applied tenant configuration: adactUrl=" + adactUrl + ", loyaltyWidgetUrl=" + loyaltyWidgetUrl
                + ", tenantId=" + tenantId);
    }

    /**
     * @deprecated Both URLs now come from the tenant configuration, so there is nothing to initialize. Use
     * {@link #openWidget(Activity, OpenLoyaltyWidgetParams)} for the loyalty widget; {@link #openAdactCampaign}
     * needs no setup.
     */
    @Deprecated
    public static void initialize(@NonNull String widgetUrl) {
        initialize(widgetUrl, null);
    }

    /**
     * @deprecated Both URLs now come from the tenant configuration, so there is nothing to initialize. Use
     * {@link #openWidget(Activity, OpenLoyaltyWidgetParams)} for the loyalty widget; {@link #openAdactCampaign}
     * needs no setup. Until removed, an {@code adactUrl} passed here takes precedence over the tenant
     * configuration's.
     */
    @Deprecated
    public static void initialize(@Nullable String widgetUrl, @Nullable String adactUrl) {
        Log.e(TAG, "GamifyWidgetSDK.initialize(...) is deprecated and will be removed in a future version: the "
                + "widget URLs now come from the tenant configuration.");
        legacyWidgetUrl = widgetUrl;
        legacyAdactUrl = normalizeBaseUrl(adactUrl);
    }

    @NonNull
    public String getAdactUrl() {
        String adactUrl = adactBaseUrl();
        return adactUrl != null ? adactUrl + "/" : "";
    }

    @Nullable
    private static String adactBaseUrl() {
        String legacy = legacyAdactUrl;
        return legacy != null ? legacy : tenantOptions.adactUrl;
    }

    /**
     * @deprecated Use {@link #openWidget(Activity)}.
     */
    @Deprecated
    public void open(@NonNull Activity activity) {
        open(activity, null, null);
    }

    /**
     * Opens the URL passed to {@link #initialize(String, String)} as given. A {@code token} is sent in INIT and
     * on every refresh; without one, a token comes from the {@code AuthTokenProvider} when auth is enabled.
     *
     * @deprecated Use {@link #openWidget(Activity, OpenLoyaltyWidgetParams)}, which builds the URL from the tenant
     * configuration and gets the token from the {@code AuthTokenProvider}.
     */
    @Deprecated
    public void open(@NonNull Activity activity, @Nullable String userId, @Nullable String token) {
        Log.e(TAG, "GamifyWidgetSDK.open(...) is deprecated and will be removed in a future version: use "
                + "openWidget(activity, params) instead.");
        String widgetUrl = legacyWidgetUrl;
        if (widgetUrl == null || widgetUrl.isEmpty()) {
            Log.e(TAG, "Cannot open the loyalty widget: open(...) needs a widget URL from initialize(...)");
            return;
        }
        if (loyaltyDialog != null) {
            return;
        }
        closeAdactCampaign();
        WidgetDialog.TokenSource tokenSource = token != null
                ? callback -> callback.onComplete(token, null)
                : tokenSourceFor(userId);
        loyaltyDialog = new WidgetDialog(activity, widgetUrl, userId, tokenSource, () -> loyaltyDialog = null);
        loyaltyDialog.show();
    }

    public void openWidget(@NonNull Activity activity) {
        openWidget(activity, new OpenLoyaltyWidgetParams());
    }

    public void openWidget(@NonNull Activity activity, @NonNull OpenLoyaltyWidgetParams params) {
        String userId = params.userId != null ? params.userId : environment.currentUserId();
        userId = userId != null ? userId.trim() : null;
        String widgetSrc = buildLoyaltyWidgetUrl(userId, params.brand);
        if (widgetSrc.isEmpty()) {
            Log.e(TAG, "Cannot open the loyalty widget: " + describeMissingLoyaltyInputs(userId));
            return;
        }

        if (loyaltyDialog != null) {
            return;
        }
        // Both surfaces are fullscreen overlays on Android — only one at a time.
        closeAdactCampaign();
        loyaltyDialog = new WidgetDialog(activity, widgetSrc, userId,
                tokenSourceFor(userId), () -> loyaltyDialog = null);
        loyaltyDialog.show();
    }

    @NonNull
    private WidgetDialog.TokenSource tokenSourceFor(@Nullable String userId) {
        AuthManager authManager = environment.authManager();
        if (authManager == null || userId == null || userId.isEmpty()) {
            return callback -> callback.onComplete(null, null);
        }
        return callback -> authManager.getToken(userId, callback);
    }

    @NonNull
    public String buildLoyaltyWidgetUrl(@Nullable String userId, @Nullable String brand) {
        TenantOptions options = tenantOptions;
        if (options.loyaltyWidgetUrl == null || options.tenantId <= 0 || userId == null || userId.isEmpty()) {
            return "";
        }

        String brandSegment = brand != null && !brand.isEmpty() ? "/brand/" + Uri.encode(brand) : "";
        String url = options.loyaltyWidgetUrl + "/" + options.tenantId + brandSegment + "/" + Uri.encode(userId);
        return "https".equalsIgnoreCase(Uri.parse(url).getScheme()) ? url : "";
    }

    @NonNull
    private static String describeMissingLoyaltyInputs(@Nullable String userId) {
        TenantOptions options = tenantOptions;
        List<String> missing = new ArrayList<>();
        if (options.loyaltyWidgetUrl == null) {
            missing.add("loyaltyWidgetUrl (gamify.loyaltyWidgetUrl in the tenant configuration)");
        }
        if (options.tenantId <= 0) {
            missing.add("tenantId (tenant configuration not loaded yet?)");
        }
        if (userId == null || userId.isEmpty()) {
            missing.add("userId (pass one in OpenLoyaltyWidgetParams or call Optimove.setUserId)");
        }
        if (missing.isEmpty()) {
            return "loyaltyWidgetUrl is not HTTPS: " + options.loyaltyWidgetUrl;
        }
        return "missing " + TextUtils.join(", ", missing);
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
        adactDialog = new WidgetDialog(activity, campaignUrl, () -> adactDialog = null);
        adactDialog.show();
    }

    /**
     * Builds {@code {adactUrl}/embedded/{campaignId}?cid=&customerIdToken=}.
     * Returns empty string when {@code adactUrl} or {@code campaignId} is missing, or URL is not HTTPS.
     */
    @NonNull
    public String buildAdactCampaignUrl(@NonNull OpenAdactParams params) {
        String adactUrl = adactBaseUrl();
        if (adactUrl == null || params.campaignId == null) {
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

    @VisibleForTesting
    static synchronized void resetForTesting(@Nullable Environment testEnvironment) {
        shared = null;
        tenantOptions = TenantOptions.EMPTY;
        legacyWidgetUrl = null;
        legacyAdactUrl = null;
        environment = testEnvironment != null ? testEnvironment : DEFAULT_ENVIRONMENT;
    }

    @Nullable
    static String normalizeBaseUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static final class TenantOptions {

        static final TenantOptions EMPTY = new TenantOptions(null, null, -1);

        @Nullable final String adactUrl;
        @Nullable final String loyaltyWidgetUrl;
        final int tenantId;

        TenantOptions(@Nullable String adactUrl, @Nullable String loyaltyWidgetUrl, int tenantId) {
            this.adactUrl = adactUrl;
            this.loyaltyWidgetUrl = loyaltyWidgetUrl;
            this.tenantId = tenantId;
        }
    }
}
