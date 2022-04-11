package com.optimove.sdk.optimove_sdk.main;

import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.R;
import com.optimove.sdk.optimove_sdk.undecided.DeferredDeepLinkHandlerInterface;
import com.optimove.sdk.optimove_sdk.undecided.KumulosConfig;
import com.optimove.sdk.optimove_sdk.undecided.UrlBuilder;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class OptimobileConfig {

    @DrawableRes
    static final int DEFAULT_NOTIFICATION_ICON_ID = R.drawable.kumulos_ic_stat_notifications;
    static final int DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS = 23;

    private String optimoveToken;
    private String configFile;

    private String apiKey;
    private String secretKey;
    @DrawableRes
    private int notificationSmallIconId;
    private KumulosConfig.InAppConsentStrategy inAppConsentStrategy;
    private int sessionIdleTimeoutSeconds;

    private JSONObject runtimeInfo;
    private JSONObject sdkInfo;
    private Map<UrlBuilder.Service, String> baseUrlMap;

    private URL deepLinkCname;

    public static int getDefaultNotificationIconId() {
        return DEFAULT_NOTIFICATION_ICON_ID;
    }

    public static int getDefaultSessionIdleTimeoutSeconds() {
        return DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS;
    }

    public void setOptimoveToken(String optimoveToken) {
        this.optimoveToken = optimoveToken;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getNotificationSmallIconId() {
        return notificationSmallIconId;
    }

    public void setNotificationSmallIconId(int notificationSmallIconId) {
        this.notificationSmallIconId = notificationSmallIconId;
    }

    public KumulosConfig.InAppConsentStrategy getInAppConsentStrategy() {
        return inAppConsentStrategy;
    }

    public void setInAppConsentStrategy(KumulosConfig.InAppConsentStrategy inAppConsentStrategy) {
        this.inAppConsentStrategy = inAppConsentStrategy;
    }

    public int getSessionIdleTimeoutSeconds() {
        return sessionIdleTimeoutSeconds;
    }

    public void setSessionIdleTimeoutSeconds(int sessionIdleTimeoutSeconds) {
        this.sessionIdleTimeoutSeconds = sessionIdleTimeoutSeconds;
    }

    public JSONObject getRuntimeInfo() {
        return runtimeInfo;
    }

    public void setRuntimeInfo(JSONObject runtimeInfo) {
        this.runtimeInfo = runtimeInfo;
    }

    public JSONObject getSdkInfo() {
        return sdkInfo;
    }

    public void setSdkInfo(JSONObject sdkInfo) {
        this.sdkInfo = sdkInfo;
    }

    public Map<UrlBuilder.Service, String> getBaseUrlMap() {
        return baseUrlMap;
    }

    public void setBaseUrlMap(Map<UrlBuilder.Service, String> baseUrlMap) {
        this.baseUrlMap = baseUrlMap;
    }

    public URL getDeepLinkCname() {
        return deepLinkCname;
    }

    public void setDeepLinkCname(URL deepLinkCname) {
        this.deepLinkCname = deepLinkCname;
    }

    public DeferredDeepLinkHandlerInterface getDeferredDeepLinkHandler() {
        return deferredDeepLinkHandler;
    }

    public void setDeferredDeepLinkHandler(DeferredDeepLinkHandlerInterface deferredDeepLinkHandler) {
        this.deferredDeepLinkHandler = deferredDeepLinkHandler;
    }

    private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;

    private OptimobileConfig(String optimoveToken, String configFile, String apiKey, String secretKey) {
        this.optimoveToken = optimoveToken;
        this.configFile = configFile;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String getOptimoveToken() {
        return optimoveToken;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public static class Builder {
        String optimoveToken;
        String configFile;
        String apiKey;
        String secretKey;

        public Builder(String optimoveToken, String configFile, String apiKey, String secretKey) {
            this.optimoveToken = optimoveToken;
            this.configFile = configFile;
            this.apiKey = apiKey;
            this.secretKey = secretKey;
        }

        @DrawableRes
        private int notificationSmallIconDrawableId = OptimobileConfig.DEFAULT_NOTIFICATION_ICON_ID;
        private KumulosConfig.InAppConsentStrategy consentStrategy = null;
        private int sessionIdleTimeoutSeconds = OptimobileConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS;

        private JSONObject runtimeInfo;
        private JSONObject sdkInfo;
        private Map<UrlBuilder.Service, String> baseUrlMap;

        private @Nullable
        URL deepLinkCname;
        private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;


        public OptimobileConfig.Builder setPushSmallIconId(@DrawableRes int drawableIconId) {
            this.notificationSmallIconDrawableId = drawableIconId;
            return this;
        }

        public OptimobileConfig.Builder enableInAppMessaging(KumulosConfig.InAppConsentStrategy strategy) {
            this.consentStrategy = strategy;
            return this;
        }

        public OptimobileConfig.Builder enableDeepLinking(@NonNull String cname, DeferredDeepLinkHandlerInterface handler) {
            this.deferredDeepLinkHandler = handler;
            try{
                this.deepLinkCname = new URL(cname);
            }
            catch(MalformedURLException e){
                e.printStackTrace();
                this.deepLinkCname = null;
            }

            return this;
        }

        public OptimobileConfig.Builder enableDeepLinking(DeferredDeepLinkHandlerInterface handler) {
            this.deferredDeepLinkHandler = handler;
            this.deepLinkCname = null;
            return this;
        }

        /**
         * The minimum amount of time the user has to have left the app for a session end event to be
         * recorded.
         *
         * The idle period starts when a pause lifecycle event is observed, and is reset when any resume
         * event is seen. If no resume event is observed and the idle period elapses, the app is considered
         * to be in the background and the session ends.
         *
         * This defaults to KumulosConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS if unspecified.
         *
         * @param idleTimeSeconds
         * @return
         */
        public OptimobileConfig.Builder setSessionIdleTimeoutSeconds(int idleTimeSeconds) {
            this.sessionIdleTimeoutSeconds = Math.abs(idleTimeSeconds);
            return this;
        }


        public OptimobileConfig build() {
            if (TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(optimoveToken) || TextUtils.isEmpty(configFile)) {
                throw new IllegalStateException("You need to provide apiKey and secretKey before you can build KumulosConfig.");
            }

            OptimobileConfig newConfig = new OptimobileConfig(optimoveToken, configFile, apiKey, secretKey);

            newConfig.setApiKey(apiKey);
            newConfig.setSecretKey(secretKey);
            newConfig.setNotificationSmallIconId(notificationSmallIconDrawableId);
            newConfig.setSessionIdleTimeoutSeconds(sessionIdleTimeoutSeconds);
            newConfig.setRuntimeInfo(this.runtimeInfo);
            newConfig.setSdkInfo(this.sdkInfo);
            newConfig.setBaseUrlMap(this.baseUrlMap);

            newConfig.setInAppConsentStrategy(consentStrategy);

            newConfig.setDeepLinkCname(this.deepLinkCname);
            newConfig.setDeferredDeepLinkHandler(this.deferredDeepLinkHandler);

            return newConfig;
        }
    }
}
