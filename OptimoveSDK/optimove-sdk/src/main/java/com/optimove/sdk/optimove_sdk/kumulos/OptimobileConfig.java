package com.optimove.sdk.optimove_sdk.kumulos;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.R;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Represents the configuration for the Kumulos client
 */
public final class OptimobileConfig {

    @DrawableRes
    static final int DEFAULT_NOTIFICATION_ICON_ID = R.drawable.kumulos_ic_stat_notifications;
    static final int DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS = 23;

    private @Nullable
    String apiKey;
    private @Nullable
    String secretKey;

    private @Nullable
    String optimoveToken;
    private @Nullable
    String optimoveConfigFile;

    @DrawableRes
    private int notificationSmallIconId;
    private InAppConsentStrategy inAppConsentStrategy;
    private int sessionIdleTimeoutSeconds;

    private JSONObject runtimeInfo;
    private JSONObject sdkInfo;
    private @Nullable Map<UrlBuilder.Service, String> baseUrlMap;

    private URL deepLinkCname;
    private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;

    public enum InAppConsentStrategy {
        AUTO_ENROLL,
        EXPLICIT_BY_USER
    }

    // Private constructor to discourage not using the Builder.
    private OptimobileConfig() {
    }

    private void setApiKey(@Nullable String apiKey) {
        this.apiKey = apiKey;
    }

    private void setSecretKey(@Nullable String secretKey) {
        this.secretKey = secretKey;
    }

    private void setOptimoveToken(@Nullable String optimoveToken) {
        this.optimoveToken = optimoveToken;
    }

    private void setOptimoveConfigFile(@Nullable String optimoveConfigFile) {
        this.optimoveConfigFile = optimoveConfigFile;
    }

    private void setNotificationSmallIconId(@DrawableRes int notificationSmallIconId) {
        this.notificationSmallIconId = notificationSmallIconId;
    }

    private void setSessionIdleTimeoutSeconds(int timeoutSeconds) {
        this.sessionIdleTimeoutSeconds = timeoutSeconds;
    }

    private void setRuntimeInfo(JSONObject info) {
        this.runtimeInfo = info;
    }

    private void setSdkInfo(JSONObject info) {
        this.sdkInfo = info;
    }

    private void setBaseUrlMap(@Nullable Map<UrlBuilder.Service, String> baseUrlMap) {
        this.baseUrlMap = baseUrlMap;
    }

    private void setInAppConsentStrategy(InAppConsentStrategy strategy) {
        this.inAppConsentStrategy = strategy;
    }

    private void setCname(@Nullable URL deepLinkCname) {
        this.deepLinkCname = deepLinkCname;
    }

    private void setDeferredDeepLinkHandler(DeferredDeepLinkHandlerInterface deferredHandler) {
        this.deferredDeepLinkHandler = deferredHandler;
    }

    @Nullable
    String getApiKey() {
        return apiKey;
    }

    @Nullable
    String getSecretKey() {
        return secretKey;
    }

    public String getOptimoveToken() {
        return optimoveToken;
    }

    public String getOptimoveConfigFile() {
        return optimoveConfigFile;
    }

    public @DrawableRes
    int getNotificationSmallIconId() {
        return notificationSmallIconId;
    }

    public int getSessionIdleTimeoutSeconds() {
        return sessionIdleTimeoutSeconds;
    }

    JSONObject getRuntimeInfo() {
        return this.runtimeInfo;
    }

    JSONObject getSdkInfo() {
        return this.sdkInfo;
    }

    @Nullable Map<UrlBuilder.Service, String> getBaseUrlMap() {
        return baseUrlMap;
    }

    InAppConsentStrategy getInAppConsentStrategy() {
        return inAppConsentStrategy;
    }

    public @Nullable
    URL getDeepLinkCname() {
        return this.deepLinkCname;
    }

    public DeferredDeepLinkHandlerInterface getDeferredDeepLinkHandler() {
        return this.deferredDeepLinkHandler;
    }

    public boolean isOptimoveConfigured(){
        return this.optimoveToken != null && this.optimoveConfigFile != null ;
    }

    public boolean isOptimobileConfigured(){
        return this.apiKey != null && this.secretKey != null;
    }

    /**
     * Config builder for the Kumulos client
     */
    public static class Builder {
        private @Nullable
        String apiKey;
        private @Nullable
        String secretKey;
        private @Nullable
        String optimoveToken;
        private @Nullable
        String optimoveConfigFile;

        @DrawableRes
        private int notificationSmallIconDrawableId = OptimobileConfig.DEFAULT_NOTIFICATION_ICON_ID;
        private InAppConsentStrategy consentStrategy = null;
        private int sessionIdleTimeoutSeconds = OptimobileConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS;

        private JSONObject runtimeInfo;
        private JSONObject sdkInfo;
        private @Nullable Map<UrlBuilder.Service, String> baseUrlMap;

        private @Nullable
        URL deepLinkCname;
        private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;

        public Builder(@Nullable String optimoveCredentials, @Nullable String optimobileCredentials) {
            if (optimoveCredentials == null && optimobileCredentials == null) {
                throw new IllegalArgumentException("Should provide at least optimove or optimobile credentials");
            }

            this.setOptimoveCredentials(optimoveCredentials);
            this.setOptimobileCredentials(optimobileCredentials);
        }

        private void setOptimoveCredentials(@Nullable String optimoveCredentials) {
            if (optimoveCredentials == null) {
                return;
            }

            try {
                JSONArray result = this.parseCredentials(optimoveCredentials);

                this.optimoveToken = result.getString(1);
                this.optimoveConfigFile = result.getString(2);
            } catch (NullPointerException | JSONException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Optimove credentials are not correct");
            }
        }

        private void setOptimobileCredentials(@Nullable String optimobileCredentials) {
            if (optimobileCredentials == null) {
                return;
            }

            try {
                JSONArray result = this.parseCredentials(optimobileCredentials);

                String region = result.getString(1);
                this.apiKey = result.getString(2);
                this.secretKey = result.getString(3);

                this.baseUrlMap = UrlBuilder.defaultMapping(region);

            } catch (NullPointerException | JSONException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Optimobile credentials are not correct");
            }
        }

        private JSONArray parseCredentials(@NonNull String credentials) throws JSONException {
            byte[] data = Base64.decode(credentials, Base64.DEFAULT);

            return new JSONArray(new String(data, StandardCharsets.UTF_8));
        }

        /**
         * Set up the drawable to use for the small push notification icon
         *
         * @param drawableIconId
         * @return
         */
        public Builder setPushSmallIconId(@DrawableRes int drawableIconId) {
            this.notificationSmallIconDrawableId = drawableIconId;
            return this;
        }

        public Builder enableInAppMessaging(InAppConsentStrategy strategy) {
            this.consentStrategy = strategy;
            return this;
        }

        public Builder enableDeepLinking(@NonNull String cname, DeferredDeepLinkHandlerInterface handler) {
            this.deferredDeepLinkHandler = handler;
            try {
                this.deepLinkCname = new URL(cname);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                this.deepLinkCname = null;
            }

            return this;
        }

        public Builder enableDeepLinking(DeferredDeepLinkHandlerInterface handler) {
            this.deferredDeepLinkHandler = handler;
            this.deepLinkCname = null;
            return this;
        }

        /**
         * The minimum amount of time the user has to have left the app for a session end event to be
         * recorded.
         * <p>
         * The idle period starts when a pause lifecycle event is observed, and is reset when any resume
         * event is seen. If no resume event is observed and the idle period elapses, the app is considered
         * to be in the background and the session ends.
         * <p>
         * This defaults to KumulosConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS if unspecified.
         *
         * @param idleTimeSeconds
         * @return
         */
        public Builder setSessionIdleTimeoutSeconds(int idleTimeSeconds) {
            this.sessionIdleTimeoutSeconds = Math.abs(idleTimeSeconds);
            return this;
        }

        /**
         * Private API
         */
        @InternalSdkEmbeddingApi(purpose = "Allow override of stats data in x-plat SDKs")
        public Builder setRuntimeInfo(JSONObject info) {
            this.runtimeInfo = info;
            return this;
        }

        /**
         * Private API
         */
        @InternalSdkEmbeddingApi(purpose = "Allow override of stats data in x-plat SDKs")
        public Builder setSdkInfo(JSONObject info) {
            this.sdkInfo = info;
            return this;
        }

        /**
         * Private API
         */
        @InternalSdkEmbeddingApi(purpose = "Allow sending traffic to different domains")
        public Builder setBaseUrlMapping(Map<UrlBuilder.Service, String> baseUrlMap) {
            this.baseUrlMap = baseUrlMap;
            return this;
        }

        public OptimobileConfig build() {
            OptimobileConfig newConfig = new OptimobileConfig();
            newConfig.setApiKey(apiKey);
            newConfig.setSecretKey(secretKey);
            newConfig.setOptimoveToken(optimoveToken);
            newConfig.setOptimoveConfigFile(optimoveConfigFile);
            newConfig.setNotificationSmallIconId(notificationSmallIconDrawableId);
            newConfig.setSessionIdleTimeoutSeconds(sessionIdleTimeoutSeconds);
            newConfig.setRuntimeInfo(this.runtimeInfo);
            newConfig.setSdkInfo(this.sdkInfo);
            newConfig.setBaseUrlMap(this.baseUrlMap);

            newConfig.setInAppConsentStrategy(consentStrategy);

            newConfig.setCname(this.deepLinkCname);
            newConfig.setDeferredDeepLinkHandler(this.deferredDeepLinkHandler);

            return newConfig;
        }
    }
}
