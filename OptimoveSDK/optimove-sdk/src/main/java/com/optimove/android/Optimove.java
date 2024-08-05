package com.optimove.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.main.common.EventHandlerFactory;
import com.optimove.android.main.common.EventHandlerProvider;
import com.optimove.android.main.common.LifecycleObserver;
import com.optimove.android.main.common.TenantInfo;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.constants.TenantConfigsKeys;
import com.optimove.android.main.event_generators.EventGenerator;
import com.optimove.android.main.event_generators.OptimoveLifecycleEventGenerator;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.main.events.SimpleCustomEvent;
import com.optimove.android.main.events.core_events.SetEmailEvent;
import com.optimove.android.main.events.core_events.SetPageVisitEvent;
import com.optimove.android.main.events.core_events.SetUserIdEvent;
import com.optimove.android.main.sdk_configs.ConfigsFetcher;
import com.optimove.android.main.sdk_configs.configs.Configs;
import com.optimove.android.main.tools.DeviceInfoProvider;
import com.optimove.android.main.tools.FileUtils;
import com.optimove.android.main.tools.OptiUtils;
import com.optimove.android.main.tools.networking.HttpClient;
import com.optimove.android.main.tools.opti_logger.LogLevel;
import com.optimove.android.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.android.main.tools.opti_logger.RemoteLogsServiceOutputStream;
import com.optimove.android.optistream.OptistreamDbHelper;
import com.optimove.android.optimobile.Optimobile;
import com.optimove.android.optimobile.PushActionHandlerInterface;
import com.optimove.android.optimobile.PushTokenType;
import com.optimove.android.preferencecenter.OptimovePreferenceCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.optimove.android.main.constants.TenantConfigsKeys.TenantInfoKeys.CONFIG_NAME;
import static com.optimove.android.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;
import static com.optimove.android.main.constants.TenantConfigsKeys.TenantInfoKeys.TOKEN;
import static com.optimove.android.optistream.OptitrackConstants.OPTITRACK_BUFFER_SIZE;
import static com.optimove.android.optistream.OptitrackConstants.USER_ID_MAX_LENGTH;

/**
 * The main access point for the {@code Optimove SDK}.
 */
final public class Optimove {

    private static Optimove shared;
    @NonNull
    private final Context context;
    private final SharedPreferences coreSharedPreferences;
    private TenantInfo tenantInfo;
    private final UserInfo userInfo;
    private final SharedPreferences localConfigKeysPreferences;
    private final EventHandlerProvider eventHandlerProvider;
    private final OptimoveLifecycleEventGenerator optimoveLifecycleEventGenerator;
    private final DeviceInfoProvider deviceInfoProvider;
    private final AtomicBoolean configSet;
    private final LifecycleObserver lifecycleObserver;

    private static OptimoveConfig currentConfig;

    public enum IBeaconProximity {
        UNKNOWN,
        IMMEDIATE,
        NEAR,
        FAR
    }

    private Optimove(@NonNull Context context, OptimoveConfig config) {
        this.context = context;
        this.userInfo = UserInfo.newInstance(context);

        if (!config.isOptimoveConfigured()) {
            coreSharedPreferences = null;
            localConfigKeysPreferences = null;
            eventHandlerProvider = null;
            optimoveLifecycleEventGenerator = null;
            deviceInfoProvider = null;
            configSet = null;
            lifecycleObserver = null;

            return;
        }

        this.coreSharedPreferences = context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE,
                Context.MODE_PRIVATE);
        this.deviceInfoProvider = new DeviceInfoProvider(context);
        this.tenantInfo = null;
        this.localConfigKeysPreferences =
                context.getSharedPreferences(TenantConfigsKeys.LOCAL_INIT_SP_FILE, Context.MODE_PRIVATE);
        this.lifecycleObserver = new LifecycleObserver();
        this.eventHandlerProvider = new EventHandlerProvider(EventHandlerFactory.builder()
                .userInfo(userInfo)
                .httpClient(HttpClient.getInstance())
                .maximumBufferSize(OPTITRACK_BUFFER_SIZE)
                .optistreamDbHelper(new OptistreamDbHelper(context))
                .lifecycleObserver(lifecycleObserver)
                .context(context)
                .build());

        this.optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                context.getPackageName());

        this.configSet = new AtomicBoolean(false);
    }

    /**
     * Gets the {@link Optimove} {@code singleton}. {@link Optimove#initialize(Application, OptimoveConfig)} must be called
     * before trying to access {@code Optimove}.
     *
     * @return the {@code Optimove singleton}
     */
    public static Optimove getInstance() {
        if (shared == null) {
            throw new IllegalStateException("Optimove.initialize() must be called");
        }
        return shared;
    }

    /**
     * Initializes the {@code Optimove SDK}. <b>Must</b> be called from the <b>Main</b> thread.<br>
     * Must be called as soon as possible ({@link Application#onCreate()} is the ideal place), and before any call to {@link Optimove#getInstance()}.
     *
     * @param application The instance of the current {@code Application} object.
     * @param config      The {@link OptimoveConfig} as provided by <i>Optimove</i>
     */
    public static void initialize(@NonNull Application application, @NonNull OptimoveConfig config) {
        currentConfig = config;

        performSingletonInitialization(application.getApplicationContext(), config);

        if (config.isOptimobileConfigured()) {
            Optimobile.initialize(application, config, shared.userInfo.getInitialVisitorId(), shared.userInfo.getUserId());
        }

        if (config.isOptimoveConfigured()) {
            if (config.getCustomMinLogLevel() != null) {
                OptiLoggerStreamsContainer.setMinLogLevelToShow(config.getCustomMinLogLevel());
            }

            OptiLoggerStreamsContainer.initializeLogger(application);

            runOnMainThread(() -> {
                OptiLoggerStreamsContainer.debug("Optimove.initialize() is starting");
                shared.lifecycleObserver.addActivityStoppedListener(shared.optimoveLifecycleEventGenerator);
                shared.lifecycleObserver.addActivityStartedListener(shared.optimoveLifecycleEventGenerator);
                application.registerActivityLifecycleCallbacks(shared.lifecycleObserver);
            });

            if (!config.usesDelayedConfiguration()) {
                Optimove.fetchConfigsAndFinishOptimoveInit(application, config);
            }
        }

        if (config.isOptimoveConfigured() && config.isPreferenceCenterConfigured()) {
            OptimovePreferenceCenter.initialize(config);
        }
    }

    private static void fetchConfigsAndFinishOptimoveInit(@NonNull Application application, @NonNull OptimoveConfig config) {
        TenantInfo newTenantInfo = new TenantInfo(config.getOptimoveToken(), config.getConfigFileName());
        TenantInfo localTenantInfo = shared.retrieveLocalTenantInfo();
        if (localTenantInfo != null) {
            // Now merge the local with the new. NOTE: the new does not contain a tenant ID while the local must contain tenant ID otherwise it will be null
            newTenantInfo.setTenantId(localTenantInfo.getTenantId());
            shared.setAndStoreTenantInfo(newTenantInfo);
        } else {
            // No point in storing the tenant info as it is not yet valid (tenantId == -1). It will be stored once the configurations are fetched
            shared.tenantInfo = newTenantInfo;
        }

        runOnMainThread(() -> {
            shared.fetchConfigs();
        });
    }

    private static void runOnMainThread(Runnable command) {
        if (!OptiUtils.isRunningOnMainThread()) {
            OptiLoggerStreamsContainer.debug("Optimove.initialize() was called from a worker thread, moving call to main thread");
            OptiUtils.runOnMainThread(command);
        } else {
            command.run();
        }
    }

    /**
     * Late setting of credentials. Must be called only if partial initialisation constructor was used and only once.
     *
     * @param optimoveCredentials   credentials for track and trigger
     * @param optimobileCredentials credentials for other mobile features (push, in-app, deep links etc)
     */
    public static void setCredentials(@Nullable String optimoveCredentials, @Nullable String optimobileCredentials) {
        if (!currentConfig.usesDelayedConfiguration()) {
            throw new IllegalStateException("Cannot set credentials as delayed configuration is not enabled");
        }

        currentConfig.setCredentials(optimoveCredentials, optimobileCredentials);
        if (optimobileCredentials != null && currentConfig.usesDelayedOptimobileConfiguration()) {
            Optimobile.completeDelayedConfiguration(shared.getApplicationContext(), currentConfig);
        }

        if (optimoveCredentials != null && currentConfig.usesDelayedOptimoveConfiguration()) {
            Optimove.fetchConfigsAndFinishOptimoveInit((Application) shared.getApplicationContext(), currentConfig);
        }
    }

    /**
     * Gets the current config
     *
     * @return
     */
    public static OptimoveConfig getConfig() {
        return currentConfig;
    }

    /**
     * Enables remote logs for investigations. Don't call it unless we explicitly asked you to.
     */
    public static void enableStagingRemoteLogs() {
        OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.DEBUG);
    }

    private void fetchConfigs() {
        ConfigsFetcher configsFetcher = ConfigsFetcher.builder()
                .httpClient(HttpClient.getInstance())
                .tenantToken(tenantInfo.getTenantToken())
                .configName(tenantInfo.getConfigName())
                .sharedPrefs(localConfigKeysPreferences)
                .fileProvider(new FileUtils())
                .context(context)
                .build();
        configsFetcher.fetchConfigs(this::setConfigurationsIfNotSet, error -> {
            OptiLoggerStreamsContainer.fatal("Failed to get configuration file due to - %s", error);
        });
    }

    private void setConfigurationsIfNotSet(@NonNull Configs configs) {
        if (configSet.compareAndSet(false, true)) {
            updateConfigurations(configs);
        } else {
            OptiLoggerStreamsContainer.debug("Configuration file was already set, no need to set again");
        }
    }

    private void updateConfigurations(Configs configs) {
        loadTenantId(configs);
        if (configs.getLogsConfigs()
                .isProdLogsEnabled() && OptiLoggerStreamsContainer.getMinLogLevelRemote().getRawLevel() > LogLevel.ERROR.getRawLevel()) {
            OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.ERROR);
        }
        OptiLoggerStreamsContainer.debug("Updating the configurations for tenant ID %d", tenantInfo.getTenantId());

        eventHandlerProvider.processConfigs(configs);
        sendInitialEvents();
    }

    private void sendInitialEvents() {
        EventGenerator eventGenerator =
                EventGenerator.builder()
                        .withPackageName(context.getPackageName())
                        .withRequirementProvider(deviceInfoProvider)
                        .withTenantInfo(tenantInfo)
                        .withEventHandlerProvider(eventHandlerProvider)
                        .withContext(context)
                        .build();

        eventGenerator.generateStartEvents();
    }

    private void loadTenantId(Configs configs) {
        // If this is the first time the tenantId was set, we need to update the Service Logger (if exists)
        for (OptiLoggerOutputStream stream : OptiLoggerStreamsContainer.getLoggerOutputStreams()) {
            if (stream instanceof RemoteLogsServiceOutputStream) {
                RemoteLogsServiceOutputStream logsServiceOutputStream = (RemoteLogsServiceOutputStream) stream;
                logsServiceOutputStream.setTenantId(configs.getLogsConfigs()
                        .getTenantId());
            }
        }

        int tenantId = configs.getTenantId();
        tenantInfo.setTenantId(tenantId);
        setAndStoreTenantInfo(tenantInfo);
    }

    /**
     * Runs the singleton's initialization that's composed of a <b>thread safe</b> init of:
     * <ul>
     * <li>The singleton's instance {@link Optimove}</li>
     * <li>The singleton's logger {@link OptiLoggerStreamsContainer}</li>
     * </ul>
     */
    private static synchronized void performSingletonInitialization(Context context, OptimoveConfig config) {
        if (shared != null) {
            return;
        }
        shared = new Optimove(context, config);
    }

    //==============================================================================================
    //-- Analytics APIs

    /**
     * Method that performs both the {@code setUserId} and the {@code setUserEmail} flows from a single call.
     *
     * @param userId The new userId
     * @param email  the <i>email address</i> to attach
     * @see Optimove#setUserId(String)
     * @see Optimove#setUserEmail(String)
     */
    public void registerUser(String userId, String email) {
        if (currentConfig.isOptimobileConfigured()) {
            Optimobile.associateUserWithInstall(context, userId);
        }

        if (currentConfig.isOptimoveConfigured()) {
            SetUserIdEvent setUserIdEvent = processUserId(userId);
            SetEmailEvent setEmailEvent = processUserEmail(email);
            List<OptimoveEvent> list = new ArrayList<>();
            if (setUserIdEvent != null) {
                list.add(setUserIdEvent);
            }

            if (setEmailEvent != null) {
                list.add(setEmailEvent);
            }

            if (!list.isEmpty()) {
                eventHandlerProvider.getEventHandler().reportEvent(list);
            }
        }
    }

    /**
     * Attaches an <i>email address</i> to the current user.
     * If you report both the user ID and the email, use {@link Optimove#registerUser(String, String)}
     *
     * @param email the <i>email address</i> to attach
     */
    public void setUserEmail(String email) {
        SetEmailEvent setEmailEvent = processUserEmail(email);
        if (setEmailEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(setEmailEvent));
        }
    }

    /**
     * Sets the User ID of the current user and starts the {@code Visitor} to {@code Customer} conversion flow.<br>
     * <b>Note</b>: The user ID must be the same user ID that is passed to Optimove at the daily ETL
     * If you report both the user ID and the email, use {@link Optimove#registerUser(String, String)}
     *
     * @param userId The new userId to set
     */
    public void setUserId(String userId) {
        if (currentConfig.isOptimobileConfigured()) {
            Optimobile.associateUserWithInstall(context, userId);
        }

        if (currentConfig.isOptimoveConfigured()) {
            SetUserIdEvent setUserIdEvent = processUserId(userId);
            if (setUserIdEvent != null) {
                eventHandlerProvider.getEventHandler()
                        .reportEvent(Collections.singletonList(setUserIdEvent));
            }
        }
    }

    /**
     * Clears the user id, undoing the last setUserId call
     */
    public void signOutUser() {
        if (currentConfig.isOptimobileConfigured()) {
            Optimobile.clearUserAssociation(context);
        }

        if (currentConfig.isOptimoveConfigured()) {
            this.userInfo.setUserId(null);
            this.userInfo.setVisitorId(this.userInfo.getInitialVisitorId());
        }
    }

    /**
     * Used to unregister the current installation from receiving push notifications
     */
    public void pushUnregister() {
        Optimobile.pushUnregister(context);
    }

    private @Nullable
    SetEmailEvent processUserEmail(String email) {
        if (OptiUtils.isNullNoneOrUndefined(email)) {
            return new SetEmailEvent(email);
        }
        String trimmedEmail = email.trim();
        if (this.userInfo.getEmail() != null && this.userInfo.getEmail()
                .equals(trimmedEmail)) {
            OptiLoggerStreamsContainer.warn("The provided email %s, was already set", email);
            return null;
        }

        if (OptiUtils.isValidEmailAddress(trimmedEmail)) {
            this.userInfo.setEmail(trimmedEmail);
        }

        return new SetEmailEvent(trimmedEmail);
    }

    private @Nullable
    SetUserIdEvent processUserId(String userId) {
        String originalVisitorId = this.userInfo.getInitialVisitorId();

        if (OptiUtils.isNullNoneOrUndefined(userId)) {
            return new SetUserIdEvent(originalVisitorId, null, this.userInfo.getVisitorId());
        }

        if (userId.length() > USER_ID_MAX_LENGTH) {
            return new SetUserIdEvent(originalVisitorId, userId, this.userInfo.getVisitorId());
        }

        String newUserId = userId.trim();

        if (this.userInfo.getUserId() != null && this.userInfo.getUserId()
                .equals(newUserId)) {
            OptiLoggerStreamsContainer.warn("The provided user ID %s, was already set", userId);
            return null;
        }

        String updatedVisitorId = OptiUtils.SHA1(newUserId)
                .substring(0, 16);

        this.userInfo.setUserId(newUserId);
        this.userInfo.setVisitorId(updatedVisitorId);

        return new SetUserIdEvent(originalVisitorId, newUserId, updatedVisitorId);
    }

    /**
     * get visitor id of Optimove SDK
     */
    public String getVisitorId() {
        return this.userInfo.getVisitorId();
    }

    /**
     * Convenience method for reporting a <b>custom</b> {@link OptimoveEvent} without parameters
     *
     * @param name The name of the event, as declared in the <i>Optimove SDK Configurations</i>
     * @see Optimove#reportEvent(OptimoveEvent)
     */
    public void reportEvent(String name) {
        reportEvent(new SimpleCustomEvent(name, null));
    }

    /**
     * Convenience method for reporting a <b>custom</b> {@link OptimoveEvent}
     *
     * @param name       The name of the event, as declared in the <i>Optimove SDK Configurations</i>
     * @param parameters The event's parameters
     * @see Optimove#reportEvent(OptimoveEvent)
     */
    public void reportEvent(String name, Map<String, Object> parameters) {
        reportEvent(new SimpleCustomEvent(name, parameters));
    }

    /**
     * Report a <i><b>Custom Event</b></i>.
     * <p>
     * <b>Discussion</b>:<br>
     * Custom Events are defined through the {@code Optimove Site} and are passed to the {@code SDK} during initialization.<br>
     * The {@code SDK} validates that the event submitted is in compliance to the structure defined at the {@code site}.
     * Failure to comply results in the SDK rejecting the Event.<br>
     * </p>
     *
     * @param optimoveEvent The <i><b>Custom Event</b></i> to report
     * @see OptimoveEvent
     */
    public void reportEvent(OptimoveEvent optimoveEvent) {
        eventHandlerProvider.getEventHandler()
                .reportEvent(Collections.singletonList(optimoveEvent));
    }

    public void reportScreenVisit(@NonNull String screenName) {
        this.reportScreenVisit(screenName, null);
    }

    @SuppressWarnings("ConstantConditions")
    public void reportScreenVisit(@NonNull String screenName, @Nullable String screenCategory) {
        eventHandlerProvider.getEventHandler()
                .reportEvent(Collections.singletonList(new SetPageVisitEvent(screenName, screenCategory)));
    }

    /**
     * Returns the identifier for the user currently associated with the Optimobile installation record
     *
     * @return The current user identifier (if available), otherwise the Optimobile installation ID
     * @see Optimobile#associateUserWithInstall(Context, String)
     */
    public String getCurrentUserIdentifier() {
        return Optimobile.getCurrentUserIdentifier(context);
    }

    //==============================================================================================
    //-- Push APIs

    /**
     * Used to register the device installation to receive push notifications. Prompts a notification permission request
     */
    public void pushRequestDeviceToken() {
        Optimobile.pushRequestDeviceToken(context);
    }

    /**
     * Used to track a conversion from a push notification
     *
     * @param id
     */
    public void pushTrackOpen(final int id) throws Optimobile.UninitializedException {
        Optimobile.pushTrackOpen(context, id);
    }

    /**
     * Used to track a dismissal of a push notification
     *
     * @param id
     */
    public void pushTrackDismissed(final int id) throws Optimobile.UninitializedException {
        Optimobile.pushTrackDismissed(context, id);
    }

    /**
     * Registers the push token with Optimobile to allow sending push notifications to this install
     *
     * @param token
     */
    public void pushTokenStore(@NonNull final PushTokenType type, @NonNull final String token) {
        Optimobile.pushTokenStore(context, type, token);
    }

    /**
     * Allows setting the handler you want to use for push action buttons
     *
     * @param handler
     */
    public void setPushActionHandler(PushActionHandlerInterface handler) {
        Optimobile.setPushActionHandler(handler);
    }

    //==============================================================================================
    //-- DEFERRED DEEP LINKING

    public void seeIntent(Intent intent, @Nullable Bundle savedInstanceState) {
        Optimobile.seeIntent(context, intent, savedInstanceState);
    }

    public void seeIntent(Intent intent) {
        Optimobile.seeIntent(context, intent);
    }

    public void seeInputFocus(boolean hasFocus) {
        Optimobile.seeInputFocus(context, hasFocus);
    }

    //==============================================================================================
    //-- Location APIs

    /**
     * Updates the location of the current installation in Optimove
     * Accurate locaiton information is used for geofencing
     *
     * @param location
     */
    public void sendLocationUpdate(@Nullable Location location) {
        Optimobile.sendLocationUpdate(context, location);
    }

    /**
     * Records a proximity event for an Eddystone beacon.
     *
     * @param hexNamespace
     * @param hexInstance
     * @param distanceMetres - Optional distance to beacon in metres. If null, will not be recorded
     */
    public void trackEddystoneBeaconProximity(@NonNull String hexNamespace, @NonNull String hexInstance, @Nullable Double distanceMetres) {
        Optimobile.trackEddystoneBeaconProximity(context, hexNamespace, hexInstance, distanceMetres);
    }

    /**
     * Records a proximity event for an iBeacon
     *
     * @param uuid
     * @param majorId
     * @param minorId
     * @param proximity - Constant that reflects the relative distance to a beacon.
     */
    public void trackIBeaconProximity(@NonNull String uuid, int majorId, int minorId, @Nullable IBeaconProximity proximity) {
        Optimobile.trackIBeaconProximity(context, uuid, majorId, minorId, proximity);
    }

    /* *******************
     * Public Only to SDK Getters
     ******************* */

    public Context getApplicationContext() {
        return context;
    }

    public TenantInfo getTenantInfo() {
        return tenantInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    //==============================================================================================
    //-- Private Instance Methods

    private void setAndStoreTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
        this.coreSharedPreferences.edit()
                .putInt(TENANT_ID, tenantInfo.getTenantId())
                .putString(TOKEN, tenantInfo.getTenantToken())
                .putString(CONFIG_NAME, tenantInfo.getConfigName())
                .apply();
    }

    @Nullable
    private TenantInfo retrieveLocalTenantInfo() {
        int tenantId = this.coreSharedPreferences.getInt(TENANT_ID, -1);
        String token = this.coreSharedPreferences.getString(TOKEN, null);
        String configName = this.coreSharedPreferences.getString(CONFIG_NAME, null);
        if (tenantId == -1 || token == null || configName == null) {
            return null;
        }
        return new TenantInfo(tenantId, token, configName);
    }
}
