package com.optimove.sdk.optimove_sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.common.EventHandlerFactory;
import com.optimove.sdk.optimove_sdk.main.common.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.common.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.common.UserInfo;
import com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys;
import com.optimove.sdk.optimove_sdk.main.event_generators.EventGenerator;
import com.optimove.sdk.optimove_sdk.main.event_generators.OptimoveLifecycleEventGenerator;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetPageVisitEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.RemoteLogsServiceOutputStream;
import com.optimove.sdk.optimove_sdk.optistream.OptistreamDbHelper;
import com.optimove.sdk.optimove_sdk.optimobile.Optimobile;
import com.optimove.sdk.optimove_sdk.optimobile.OptimobileConfig;
import com.optimove.sdk.optimove_sdk.optimobile.PushActionHandlerInterface;
import com.optimove.sdk.optimove_sdk.optimobile.PushTokenType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.CONFIG_NAME;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TOKEN;
import static com.optimove.sdk.optimove_sdk.optistream.OptitrackConstants.OPTITRACK_BUFFER_SIZE;
import static com.optimove.sdk.optimove_sdk.optistream.OptitrackConstants.OPTITRACK_SP_NAME;
import static com.optimove.sdk.optimove_sdk.optistream.OptitrackConstants.USER_ID_MAX_LENGTH;

/**
 * The main access point for the {@code Optimove SDK}.
 */
final public class Optimove {

    private static final Object LOCK = new Object();
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

    private Optimove(@NonNull Context context) {
        this.context = context;
        this.coreSharedPreferences = context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE,
                Context.MODE_PRIVATE);
        this.deviceInfoProvider = new DeviceInfoProvider(context);
        this.tenantInfo = null;
        this.userInfo = UserInfo.newInstance(context);
        this.localConfigKeysPreferences =
                context.getSharedPreferences(TenantConfigsKeys.LOCAL_INIT_SP_FILE, Context.MODE_PRIVATE);
        this.lifecycleObserver = new LifecycleObserver();
        this.eventHandlerProvider = new EventHandlerProvider(EventHandlerFactory.builder()
                .userInfo(userInfo)
                .httpClient(HttpClient.getInstance(context))
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
     * Gets the {@link Optimove} {@code singleton}. {@link Optimove#initialize(Application, OptimobileConfig)} must be called
     * before trying to access {@code Optimove}.
     *
     * @return the {@code Optimove singleton}
     */
    public static Optimove getInstance() {
        if (shared == null) {
            throw new IllegalStateException("Optimove.configure() must be called");
        }
        return shared;
    }

    /**
     * Initializes the {@code Optimove SDK}. <b>Must</b> be called from the <b>Main</b> thread.<br>
     * Must be called as soon as possible ({@link Application#onCreate()} is the ideal place), and before any call to {@link Optimove#getInstance()}.
     *
     * @param application    The instance of the current {@code Application} object.
     * @param config The {@link OptimobileConfig} as provided by <i>Optimove</i>
     */
    public static void initialize(@NonNull Application application, @NonNull OptimobileConfig config) {
        if (config.isOptimobileConfigured()){
            Optimobile.initialize(application, config);
        }

        if (config.isOptimoveConfigured()){
            OptiLoggerStreamsContainer.initializeLogger(application.getApplicationContext());

            Runnable initCommand = () -> {
                boolean initializedSuccessfully = performSingletonInitialization(application.getApplicationContext(),
                        new TenantInfo(config.getOptimoveToken(), config.getConfigFileName()));
                if (initializedSuccessfully) {
                    OptiLoggerStreamsContainer.debug("Optimove.configure() is starting");
                    shared.lifecycleObserver.addActivityStoppedListener(shared.optimoveLifecycleEventGenerator);
                    shared.lifecycleObserver.addActivityStartedListener(shared.optimoveLifecycleEventGenerator);
                    application.registerActivityLifecycleCallbacks(shared.lifecycleObserver);
                    shared.fetchConfigs();
                }
            };
            if (!OptiUtils.isRunningOnMainThread()) {
                OptiLoggerStreamsContainer.debug("Optimove.initialize() was called from a worker thread, moving call to main thread");
                OptiUtils.runOnMainThread(initCommand);
            } else {
                initCommand.run();
            }
        }
    }

    /**
     * Initializes the {@code Optimove SDK}. <b>Must</b> be called from the <b>Main</b> thread.<br>
     * Must be called as soon as possible ({@link Application#onCreate()} is the ideal place), and before any call to {@link Optimove#getInstance()}.
     *
     * @param application           The instance of the current {@code Application} object.
     * @param optimobileConfig        The {@link OptimobileConfig} as provided by <i>Optimove</i>.
     * @param logcatMinLogLevel Logcat minimum log level to show.
     */
    public static void initialize(Application application, OptimobileConfig optimobileConfig, LogLevel logcatMinLogLevel) {
        OptiLoggerStreamsContainer.setMinLogLevelToShow(logcatMinLogLevel);
        initialize(application, optimobileConfig);
    }

    /**
     * Enables remote logs for investigations. Don't call it unless we explicitly asked you to.
     */
    public static void enableStagingRemoteLogs() {
        OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.DEBUG);
    }

    public EventHandlerProvider getEventHandlerProvider() {
        return eventHandlerProvider;
    }

    private void fetchConfigs() {
        ConfigsFetcher configsFetcher = ConfigsFetcher.builder()
                .httpClient(HttpClient.getInstance(context))
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

    private void sendInitialEvents(){
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
     * <b>Note</b>: To initialize properly, the Optimove instance MUST have a {@link TenantInfo} property, either remote or local.
     *
     * @param context The Context.
     * @param newTenantInfo      The {@code TenantInfo} that was sent by the client.
     * @throws IllegalArgumentException if <b>both</b> the new and the local {@code TenantInfo}s passed are null.
     */
    private static boolean performSingletonInitialization(Context context, TenantInfo newTenantInfo) {
        if (shared != null) {
            boolean tenantInfoExists = (shared.retrieveLocalTenantInfo() != null || newTenantInfo != null);
            if (!tenantInfoExists) {
                OptiLoggerStreamsContainer.error("Optimove initialization failed due to corrupted tenant info");
            }
            return tenantInfoExists;
        }

        synchronized (LOCK) {
            shared = new Optimove(context);

            TenantInfo localTenantInfo = shared.retrieveLocalTenantInfo();
            if (newTenantInfo == null && localTenantInfo == null) {
                OptiLoggerStreamsContainer.error("Optimove initialization failed due to corrupted tenant info");
                return false;
            }
            // Merge the local and the new TenantInfo objects
            if (localTenantInfo != null && newTenantInfo == null) {
                shared.tenantInfo =
                        localTenantInfo; // No point in storing the tenant info as it was already fetched from local storage
            } else if (localTenantInfo != null) {
                // Now merge the local with the new. NOTE: the new does not contain a tenant ID while the local must contain tenant ID otherwise it will be null
                localTenantInfo.setTenantToken(newTenantInfo.getTenantToken());
                localTenantInfo.setConfigName(newTenantInfo.getConfigName());
                shared.setAndStoreTenantInfo(localTenantInfo);
            } else {
                shared.tenantInfo =
                        newTenantInfo; // No point in storing the tenant info as it is not yet valid (tenantId == -1). It will be stored once the configurations are fetched
            }
        }

        return true;
    }

    //==============================================================================================
    //-- Analytics APIs

    /**
     * Method that performs both the {@code setUserId} and the {@code setUserEmail} flows from a single call.
     *
     * @param userId The new userId
     * @param email the <i>email address</i> to attach
     * @see Optimove#setUserId(String)
     * @see Optimove#setUserEmail(String)
     */
    public void registerUser(String userId, String email) {
        Optimobile.associateUserWithInstall(context, userId);
        SetUserIdEvent setUserIdEvent = processUserId(userId);
        SetEmailEvent setEmailEvent = processUserEmail(email);
        if (setUserIdEvent != null && setEmailEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Arrays.asList(setUserIdEvent, setEmailEvent));
        } else if (setUserIdEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(setUserIdEvent));
        } else if (setEmailEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(setEmailEvent));
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
        Optimobile.associateUserWithInstall(context, userId);
        SetUserIdEvent setUserIdEvent = processUserId(userId);
        if (setUserIdEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(setUserIdEvent));
        }
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
        if (OptiUtils.isNullNoneOrUndefined(userId)) {
            return new SetUserIdEvent(this.userInfo.getInitialVisitorId(), null, this.userInfo.getVisitorId());
        }

        if (userId.length() > USER_ID_MAX_LENGTH) {
            return new SetUserIdEvent(this.userInfo.getInitialVisitorId(), userId, this.userInfo.getVisitorId());
        }

        String newUserId = userId.trim(); // Safe to trim now as it could never be null

        if (this.userInfo.getUserId() != null && this.userInfo.getUserId()
                .equals(newUserId)) {
            OptiLoggerStreamsContainer.warn("The provided user ID %s, was already set", userId);
            return null;
        }

        String originalVisitorId = this.userInfo.getInitialVisitorId();
        String updatedVisitorId = OptiUtils.SHA1(newUserId)
                .substring(0, 16);

        this.userInfo.setUserId(newUserId);
        this.userInfo.setVisitorId(updatedVisitorId);

        return new SetUserIdEvent(originalVisitorId, newUserId, updatedVisitorId);
    }
    /** get visitor id of Optimove SDK  */
    public String getVisitorId(){
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
     * Clears any existing association between this install record and a user identifier
     * @see Optimobile#associateUserWithInstall(Context, String)
     * @see Optimobile#getCurrentUserIdentifier(Context)
     * @param context
     */
    public static void clearUserAssociation(@NonNull Context context) {
        Optimobile.clearUserAssociation(context);
    }

    /**
     * Returns the identifier for the user currently associated with the Optimobile installation record
     *
     * @see Optimobile#associateUserWithInstall(Context, String)
     * @see com.optimove.sdk.optimove_sdk.optimobile.Installation#id(Context)
     *
     * @param context
     * @return The current user identifier (if available), otherwise the Optimobile installation ID
     */
    public static String getCurrentUserIdentifier(@NonNull Context context) {
        return Optimobile.getCurrentUserIdentifier(context);
    }

    //==============================================================================================
    //-- Push APIs

    /**
     * Used to register the device installation with FCM to receive push notifications
     *
     * @param context
     */
    public static void pushRegister(Context context) {
        Optimobile.pushRegister(context);
    }

    /**
     * Used to unregister the current installation from receiving push notifications
     *
     * @param context
     */
    public static void pushUnregister(Context context) {
        Optimobile.pushUnregister(context);
    }

    /**
     * Used to track a conversion from a push notification
     *
     * @param context
     * @param id
     */
    public static void pushTrackOpen(Context context, final int id) throws Optimobile.UninitializedException {
        Optimobile.pushTrackOpen(context, id);
    }

    /**
     * Used to track a dismissal of a push notification
     *
     * @param context
     * @param id
     */
    public static void pushTrackDismissed(Context context, final int id) throws Optimobile.UninitializedException {
        Optimobile.pushTrackDismissed(context, id);
    }

    /**
     * Registers the push token with Optimobile to allow sending push notifications to this install
     * @param context
     * @param token
     */
    public static void pushTokenStore(@NonNull Context context, @NonNull final PushTokenType type,
                                      @NonNull final String token) {
       Optimobile.pushTokenStore(context, type, token);
    }

    /**
     * Allows setting the handler you want to use for push action buttons
     * @param handler
     */
    public static void setPushActionHandler(PushActionHandlerInterface handler) {
        Optimobile.setPushActionHandler(handler);
    }

    //==============================================================================================
    //-- DEFERRED DEEP LINKING

    public static void seeIntent(Context context, Intent intent, @Nullable Bundle savedInstanceState) {
        Optimobile.seeIntent(context, intent, savedInstanceState);
    }

    public static void seeIntent(Context context, Intent intent) {
        Optimobile.seeIntent(context, intent);
    }

    public static void seeInputFocus(Context context, boolean hasFocus) {
        Optimobile.seeInputFocus(context, hasFocus);
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
