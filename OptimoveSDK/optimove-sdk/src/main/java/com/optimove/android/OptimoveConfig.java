package com.optimove.android;

import android.util.Base64;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.LogLevel;
import com.optimove.android.optimobile.DeferredDeepLinkHandlerInterface;
import com.optimove.android.optimobile.InternalSdkEmbeddingApi;
import com.optimove.android.optimobile.UrlBuilder;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Represents the configuration for the Optimove client
 */
public final class OptimoveConfig {

    @DrawableRes
    public static final int DEFAULT_NOTIFICATION_ICON_ID = R.drawable.optimobile_ic_stat_notifications;
    static final int DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS = 23;

    private @Nullable
    String region;
    private @Nullable
    String apiKey;
    private @Nullable
    String secretKey;

    private @Nullable
    String optimoveToken;
    private @Nullable
    String configFileName;

    @DrawableRes
    private int notificationSmallIconId;
    private int sessionIdleTimeoutSeconds;

    private InAppConsentStrategy inAppConsentStrategy;
    private InAppDisplayMode inAppDisplayMode;

    private JSONObject runtimeInfo;
    private JSONObject sdkInfo;
    private @Nullable Map<UrlBuilder.Service, String> baseUrlMap;

    private URL deepLinkCname;
    private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;

    private @Nullable LogLevel minLogLevel;

    private @NonNull FeatureSet featureSet;

    private boolean delayedInitialisation;


    public enum InAppConsentStrategy {
        AUTO_ENROLL,
        EXPLICIT_BY_USER
    }

    public enum InAppDisplayMode {
        AUTOMATIC,
        PAUSED
    }

    public enum Region {
        EU("eu-central-2"),
        US("us-east-1"),
        DEV("uk-1");
        private final String region;

        Region(String region) {
            this.region = region;
        }

        @NonNull
        public String toString() {
            return region;
        }
    }

    public static class FeatureSet {
        private enum Feature {
            OPTIMOVE,
            OPTIMOBILE
        }
        List<Feature> features = new ArrayList<>();
        public FeatureSet withOptimove(){
            features.add(Feature.OPTIMOVE);

            return this;
        }

        public FeatureSet withOptimobile(){
            features.add(Feature.OPTIMOBILE);

            return this;
        }

        boolean has(Feature feature){
            return features.contains(feature);
        }

        boolean isEmpty(){
            return features.isEmpty();
        }
    }



    // Private constructor to discourage not using the Builder.
    private OptimoveConfig() {
    }

    private void setRegion(@Nullable String region) {
        this.region = region;
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

    private void setInAppDisplayMode(@NonNull InAppDisplayMode mode) {
        this.inAppDisplayMode = mode;
    }

    private void setCname(@Nullable URL deepLinkCname) {
        this.deepLinkCname = deepLinkCname;
    }

    private void setDeferredDeepLinkHandler(DeferredDeepLinkHandlerInterface deferredHandler) {
        this.deferredDeepLinkHandler = deferredHandler;
    }

    private void setMinLogLevel(@Nullable LogLevel minLogLevel){
        this.minLogLevel = minLogLevel;
    }

    void setCredentials(@Nullable String optimoveCredentials, @Nullable String optimobileCredentials){
        if (optimoveCredentials == null && optimobileCredentials == null) {
            throw new IllegalArgumentException("Should provide at least optimove or optimobile credentials");
        }

        if (optimoveCredentials != null && !this.featureSet.has(FeatureSet.Feature.OPTIMOVE)){
            throw new IllegalArgumentException("Cannot set credentials for optimove as it is not in the desired feature set");
        }

        if (optimobileCredentials != null && !this.featureSet.has(FeatureSet.Feature.OPTIMOBILE)){
            throw new IllegalArgumentException("Cannot set credentials for optimobile as it is not in the desired feature set");
        }

        if (this.hasFinishedInitialisation()){
            throw new IllegalStateException("OptimoveConfig: credentials are already set");
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
            this.configFileName = result.getString(2);
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
            this.region = region;
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

    private void setFeatureSet(@NonNull FeatureSet featureSet){ this.featureSet = featureSet; }

    private void setDelayedInitialisation(boolean delayedInitialisation){ this.delayedInitialisation = delayedInitialisation; }

    @Nullable
    public String getRegion() {
        return region;
    }

    @Nullable
    public String getApiKey() {
        return apiKey;
    }

    @Nullable
    public String getSecretKey() {
        return secretKey;
    }

    public @Nullable String getOptimoveToken() {
        return optimoveToken;
    }

    public @Nullable String getConfigFileName() {
        return configFileName;
    }

    public @DrawableRes
    int getNotificationSmallIconId() {
        return notificationSmallIconId;
    }

    public int getSessionIdleTimeoutSeconds() {
        return sessionIdleTimeoutSeconds;
    }

    public JSONObject getRuntimeInfo() {
        return this.runtimeInfo;
    }

    public JSONObject getSdkInfo() {
        return this.sdkInfo;
    }

    public @Nullable Map<UrlBuilder.Service, String> getBaseUrlMap() {
        return baseUrlMap;
    }

    public InAppConsentStrategy getInAppConsentStrategy() {
        return inAppConsentStrategy;
    }

    public @NonNull
    InAppDisplayMode getInAppDisplayMode() { return inAppDisplayMode; }

    public @Nullable
    URL getDeepLinkCname() {
        return this.deepLinkCname;
    }

    public DeferredDeepLinkHandlerInterface getDeferredDeepLinkHandler() {
        return this.deferredDeepLinkHandler;
    }

    public boolean isOptimoveConfigured(){
        // TODO: optimove part is not ready for partial init yet
        // this.featureSet.has(FeatureSet.Feature.OPTIMOVE);
        return this.optimoveToken != null && this.configFileName != null;
    }

    public boolean isOptimobileConfigured(){
        return this.featureSet.has(FeatureSet.Feature.OPTIMOBILE);
    }

    public @Nullable LogLevel getCustomMinLogLevel(){
        return this.minLogLevel;
    }

    public boolean usesDelayedOptimobileConfiguration(){
        if (!this.delayedInitialisation){
            return false;
        }
        return this.featureSet.has(FeatureSet.Feature.OPTIMOBILE);
    }

    public boolean usesDelayedOptimoveConfiguration(){
        if (!this.delayedInitialisation){
            return false;
        }
        return this.featureSet.has(FeatureSet.Feature.OPTIMOVE);
    }

    public boolean usesDelayedConfiguration(){
        return this.delayedInitialisation;
    }

    private boolean hasFinishedInitialisation(){
        boolean hasOptimoveCreds = optimoveToken != null && configFileName != null;
        boolean hasOptimobileCreds = apiKey != null && secretKey != null;

        if (!hasOptimoveCreds && featureSet.has(FeatureSet.Feature.OPTIMOVE)){
            return false;
        }

        if (!hasOptimobileCreds && featureSet.has(FeatureSet.Feature.OPTIMOBILE)){
            return false;
        }

        return  true;
    }

    /**
     * Config builder for the Optimobile client
     */
    public static class Builder {
        private @Nullable
        String region;
        private @Nullable String optimoveCredentials;
        private @Nullable String optimobileCredentials;
        private final @NonNull FeatureSet featureSet;
        private final boolean delayedInitialisation;

        @DrawableRes
        private int notificationSmallIconDrawableId = OptimoveConfig.DEFAULT_NOTIFICATION_ICON_ID;
        private int sessionIdleTimeoutSeconds = OptimoveConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS;

        private InAppConsentStrategy consentStrategy = null;
        private InAppDisplayMode inAppDisplayMode = null;

        private JSONObject runtimeInfo;
        private JSONObject sdkInfo;
        private @Nullable Map<UrlBuilder.Service, String> baseUrlMap;
        private @Nullable Map<UrlBuilder.Service, String> overridingBaseUrlMap;

        private @Nullable
        URL deepLinkCname;
        private DeferredDeepLinkHandlerInterface deferredDeepLinkHandler;

        private @Nullable LogLevel minLogLevel;

        public Builder(@NonNull Region region, @NonNull FeatureSet featureSet) {
            if (featureSet.isEmpty()){
                throw new IllegalArgumentException("Feature set cannot be empty");
            }
            this.region = region.toString();
            this.baseUrlMap = UrlBuilder.defaultMapping(this.region);
            this.featureSet = featureSet;
            this.delayedInitialisation = true;
        }

        public Builder(@Nullable String optimoveCredentials, @Nullable String optimobileCredentials) {
            this.optimoveCredentials = optimoveCredentials;
            this.optimobileCredentials = optimobileCredentials;
            this.delayedInitialisation = false;

            if (optimoveCredentials == null && optimobileCredentials == null){
                throw new IllegalArgumentException("Should provide at least optimove or optimobile credentials");
            }

            this.featureSet = new FeatureSet();
            if (optimoveCredentials != null){
                this.featureSet.withOptimove();
            }

            if (optimobileCredentials != null){
                this.featureSet.withOptimobile();
            }
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

        public Builder enableInAppMessaging(@NonNull InAppConsentStrategy strategy, @NonNull InAppDisplayMode defaultDisplayMode) {
            this.consentStrategy = strategy;
            this.inAppDisplayMode = defaultDisplayMode;
            return this;
        }

        public Builder enableInAppMessaging(InAppConsentStrategy strategy) {
            return enableInAppMessaging(strategy, InAppDisplayMode.AUTOMATIC);
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
         * This defaults to OptimoveConfig.DEFAULT_SESSION_IDLE_TIMEOUT_SECONDS if unspecified.
         *
         * @param idleTimeSeconds
         * @return
         */
        public Builder setSessionIdleTimeoutSeconds(int idleTimeSeconds) {
            this.sessionIdleTimeoutSeconds = Math.abs(idleTimeSeconds);
            return this;
        }

        /**
         * @param minLogLevel Logcat minimum log level to show.
         */
        public Builder setMinLogLevel(LogLevel minLogLevel){
            this.minLogLevel = minLogLevel;
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
            this.overridingBaseUrlMap = baseUrlMap;
            return this;
        }

        public OptimoveConfig build() {
            OptimoveConfig newConfig = new OptimoveConfig();
            newConfig.setFeatureSet(this.featureSet);
            newConfig.setDelayedInitialisation(delayedInitialisation);
            if (!delayedInitialisation){
                newConfig.setCredentials(this.optimoveCredentials, this.optimobileCredentials);
            }
            else{
                newConfig.setRegion(this.region);
                newConfig.setBaseUrlMap(this.baseUrlMap);
            }

            if (this.overridingBaseUrlMap != null){
                newConfig.setBaseUrlMap(this.overridingBaseUrlMap);
            }

            newConfig.setNotificationSmallIconId(notificationSmallIconDrawableId);
            newConfig.setSessionIdleTimeoutSeconds(sessionIdleTimeoutSeconds);
            newConfig.setRuntimeInfo(this.runtimeInfo);
            newConfig.setSdkInfo(this.sdkInfo);

            newConfig.setInAppConsentStrategy(consentStrategy);
            newConfig.setInAppDisplayMode(inAppDisplayMode);

            newConfig.setCname(this.deepLinkCname);
            newConfig.setDeferredDeepLinkHandler(this.deferredDeepLinkHandler);

            newConfig.setMinLogLevel(this.minLogLevel);

            return newConfig;
        }
    }
}