
package com.optimove.android.optimobile;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import com.optimove.android.BuildConfig;
import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;

/**
 * The Optimobile class is the main public API for calling Optimobile RPC methods and handling push registration
 */
public final class Optimobile {

    private static final String TAG = Optimobile.class.getName();

    /** package */ static final String KEY_AUTH_HEADER = "Authorization";
    private static boolean initialized;

    private static String installId;

    static UrlBuilder urlBuilder;

    private static OkHttpClient httpClient;
    /** package */ static String authHeader;
    /** package */ static ExecutorService executorService;
    /** package */ static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Object userIdLocker = new Object();

    static PushActionHandlerInterface pushActionHandler = null;

    private static DeferredDeepLinkHelper deepLinkHelper;

    static SessionHelper sessionHelper;

    /** package */ static class BaseCallback {
        public void onFailure(Exception e) {
            e.printStackTrace();
        }
    }

    static abstract class ResultCallback<S> extends BaseCallback {
        public abstract void onSuccess(S result);
    }

    public static class UninitializedException extends RuntimeException {
        UninitializedException() {
            super("The Optimobile has not been correctly initialized. Please ensure you have followed the integration guide before invoking SDK methods");
        }
    }

    /**
     * Used to configure the Optimobile class. Only needs to be called once per process
     * @param application
     * @param config
     */
    public static synchronized void initialize(final Application application, OptimoveConfig config, String installationId) {
        if (!config.isOptimobileConfigured()){
            throw new UninitializedException();
        }

        if (initialized) {
            log("Optimobile is already initialized, aborting...");
            return;
        }

        installId = installationId;

        authHeader = buildBasicAuthHeader(config.getApiKey(), config.getSecretKey());

        urlBuilder  = new UrlBuilder(config.getBaseUrlMap());
        httpClient = buildOkHttpClient();

        executorService = Executors.newSingleThreadExecutor();

        initialized = true;

        OptimoveInApp.initialize(application, config);

        if (config.getDeferredDeepLinkHandler() != null){
            deepLinkHelper = new DeferredDeepLinkHelper();
        }

        sessionHelper = new SessionHelper(application);

        // Stats ping
        AnalyticsContract.StatsCallHomeRunnable statsTask = new AnalyticsContract.StatsCallHomeRunnable(application);
        executorService.submit(statsTask);
    }

    private static OkHttpClient buildOkHttpClient(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return new OkHttpClient();
        }

        //ciphers available on Android 4.4 have intersections with the approved ones in MODERN_TLS, but the intersections are on bad cipher list, so,
        //perhaps not supported by CloudFlare. On older devices allow all ciphers
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .allEnabledCipherSuites()
            .build();

        return new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(spec))
            .build();
    }

    //==============================================================================================
    //-- Getters/setters

    /**
     * Associates a user identifier with the current Optimobile installation record.
     * @param context
     * @param userIdentifier
     */
    public static void associateUserWithInstall(Context context, @NonNull final String userIdentifier) {
        associateUserWithInstallImpl(context, userIdentifier, null);
    }

    /**
     * Associates a user identifier with the current Optimobile installation record, additionally setting the attributes for the user.
     * @param context
     * @param userIdentifier
     * @param attributes
     */
    public static void associateUserWithInstall(Context context, @NonNull final String userIdentifier, @NonNull final JSONObject attributes) {
        associateUserWithInstallImpl(context, userIdentifier, attributes);
    }

    /**
     * Clears any existing association between this install record and a user identifier
     * @see Optimobile#associateUserWithInstall(Context, String)
     * @see Optimobile#getCurrentUserIdentifier(Context)
     * @param context
     */
    public static void clearUserAssociation(@NonNull Context context) {
        String currentUserId = null;
        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);

        synchronized (userIdLocker) {
            currentUserId = prefs.getString(SharedPrefs.KEY_USER_IDENTIFIER, null);
        }

        JSONObject props = new JSONObject();
        try {
            props.put("oldUserIdentifier", currentUserId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        trackEvent(context, AnalyticsContract.EVENT_TYPE_CLEAR_USER_ASSOCIATION, props);

        synchronized (userIdLocker) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(SharedPrefs.KEY_USER_IDENTIFIER);
            editor.apply();
        }

        OptimoveInApp.getInstance().handleInAppUserChange(context, Optimove.getConfig());
    }

    /**
     * Returns the identifier for the user currently associated with the Optimobile installation record
     *
     * @see Optimobile#associateUserWithInstall(Context, String)
     *
     * @param context
     * @return The current user identifier (if available), otherwise the Optimobile installation ID
     */
    public static String getCurrentUserIdentifier(@NonNull Context context) {
        synchronized (userIdLocker) {
            SharedPreferences preferences = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
            return preferences.getString(SharedPrefs.KEY_USER_IDENTIFIER, getInstallId());
        }
    }

    private static void associateUserWithInstallImpl(Context context, @NonNull final String userIdentifier, @Nullable final JSONObject attributes) {
        if (TextUtils.isEmpty(userIdentifier)) {
            throw new IllegalArgumentException("Optimobile.associateUserWithInstall requires a non-empty user identifier");
        }

        boolean isNewUserIdentifier = !userIdentifier.equals(getCurrentUserIdentifier(context));

        JSONObject props = new JSONObject();
        try {
            props.put("id", userIdentifier);
            if (null != attributes) {
                props.put("attributes", attributes);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (isNewUserIdentifier){
            SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);

            synchronized (userIdLocker) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedPrefs.KEY_USER_IDENTIFIER, userIdentifier);
                editor.apply();
            }
        }

        trackEventImmediately(context, AnalyticsContract.EVENT_TYPE_ASSOCIATE_USER, props);

        if (isNewUserIdentifier){
            OptimoveInApp.getInstance().handleInAppUserChange(context, Optimove.getConfig());
        }
    }

    //==============================================================================================
    //-- Push APIs

    /**
     * Used to register the device installation with FCM to receive push notifications
     *
     * @param context
     */
    public static void pushRegister(Context context) {
        PushRegistration.RegisterTask task = new PushRegistration.RegisterTask(context);
        executorService.submit(task);
    }

    /**
     * Used to unregister the current installation from receiving push notifications
     *
     * @param context
     */
    public static void pushUnregister(Context context) {
        PushRegistration.UnregisterTask task = new PushRegistration.UnregisterTask(context);
        executorService.submit(task);
    }

    /**
     * Used to track a conversion from a push notification
     *
     * @param context
     * @param id
     */
    public static void pushTrackOpen(Context context, final int id) throws UninitializedException {
        log("PUSH: Tracking open for " + id);

        JSONObject props = new JSONObject();
        try {
            props.put("type", AnalyticsContract.MESSAGE_TYPE_PUSH);
            props.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_OPENED, props);
    }

    /**
     * Used to track a dismissal of a push notification
     *
     * @param context
     * @param id
     */
    public static void pushTrackDismissed(Context context, final int id) throws UninitializedException {
        log("PUSH: Tracking dismissal for " + id);

        JSONObject props = new JSONObject();
        try {
            props.put("type", AnalyticsContract.MESSAGE_TYPE_PUSH);
            props.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_DISMISSED, props);
    }

    /**
     * Registers the push token with Optimobile to allow sending push notifications to this install
     * @param context
     * @param token
     */
    public static void pushTokenStore(@NonNull Context context, @NonNull final PushTokenType type, @NonNull final String token) {

        JSONObject props = new JSONObject();

        try {
            props.put("token", token);
            props.put("type", type.getValue());
            props.put("package", context.getPackageName());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        trackEvent(context, AnalyticsContract.EVENT_TYPE_PUSH_DEVICE_REGISTERED, props, System.currentTimeMillis(), true);
    }

    /**
     * Allows setting the handler you want to use for push action buttons
     * @param handler
     */
    public static void setPushActionHandler(PushActionHandlerInterface handler) {
        pushActionHandler = handler;
    }

    //==============================================================================================
    //-- DEFERRED DEEP LINKING

    public static void seeIntent(Context context, Intent intent, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null){
            return;
        }

        Optimobile.seeIntent(context, intent);
    }

    public static void seeIntent(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_VIEW.equals(action)){
            return;
        }

        Uri uri = intent.getData();
        if (uri == null){
            return;
        }

        if (Optimove.getConfig().getDeferredDeepLinkHandler() == null){
            return;
        }

        deepLinkHelper.maybeProcessUrl(context, uri.toString(), false);
    }

    public static void seeInputFocus(Context context, boolean hasFocus) {
        if (!hasFocus){
            return;
        }

        if (Optimove.getConfig().getDeferredDeepLinkHandler() == null){
            return;
        }

        if (DeferredDeepLinkHelper.nonContinuationLinkCheckedForSession.getAndSet(true)) {
            return;
        }

        deepLinkHelper.checkForNonContinuationLinkMatch(context);
    }

    //==============================================================================================
    //-- OTHER

    /**
     * Generates the correct Authorization header value for HTTP Basic auth with the API key & secret
     * @return Authorization header value
     */
    private static String buildBasicAuthHeader(String apiKey, String secretKey) {
        return "Basic "
                + Base64.encodeToString((apiKey + ":" + secretKey).getBytes(), Base64.NO_WRAP);
    }

    /**
     * Logging
     *
     * @param message
     */
    protected static void log(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    protected static void log(String message) {
        log(TAG, message);
    }

    /** package */ static OkHttpClient getHttpClient() throws UninitializedException {
        if (!initialized) {
            throw new UninitializedException();
        }

        return httpClient;
    }

    /** package */ static String getInstallId() throws UninitializedException {
        if (!initialized) {
            throw new UninitializedException();
        }
        return installId;
    }

    /** package */ static boolean isInitialized() {
        return initialized;
    }

    static void trackEvent(@NonNull final Context context, @NonNull final String eventType, @Nullable final JSONObject properties, final long timestamp, boolean immediateFlush) {
        if (TextUtils.isEmpty(eventType)) {
            throw new IllegalArgumentException("Optimobile.trackEvent expects a non-empty event type");
        }

        Runnable trackingTask = new AnalyticsContract.TrackEventRunnable(context, eventType, timestamp, properties, immediateFlush);
        executorService.submit(trackingTask);
    }

    static void trackEvent(@NonNull final Context context, @NonNull final String eventType, @Nullable final JSONObject properties) {
        trackEvent(context, eventType, properties, System.currentTimeMillis(), false);
    }

    static void trackEventImmediately(@NonNull final Context context, @NonNull final String eventType, @Nullable final JSONObject properties) {
        trackEvent(context, eventType, properties, System.currentTimeMillis(), true);
    }
}