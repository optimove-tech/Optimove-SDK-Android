package com.optimove.sdk.optimove_sdk.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;

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
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.SdkLogsServiceOutputStream;
import com.optimove.sdk.optimove_sdk.optipush.OptipushManager;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamDbHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.CONFIG_NAME;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TOKEN;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_BUFFER_SIZE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_SP_NAME;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.USER_ID_MAX_LENGTH;

/**
 * The main access point for the {@code Optimove SDK}.
 */
final public class Optimove {


    /* *******************
     * Singleton
     ******************* */
    private static final Object LOCK = new Object();
    private static Optimove shared;

    @NonNull
    private Context context;
    private SharedPreferences coreSharedPreferences;
    private TenantInfo tenantInfo;
    private UserInfo userInfo;
    /* *******************
     * Object Definition
     ******************* */


    private SharedPreferences localConfigKeysPreferences;

    private EventHandlerProvider eventHandlerProvider;
    private OptipushManager optipushManager;
    private OptimoveLifecycleEventGenerator optimoveLifecycleEventGenerator;
    private DeviceInfoProvider deviceInfoProvider;
    private AtomicBoolean configSet;
    private LifecycleObserver lifecycleObserver;

    private Optimove(Context context) {
        this.context = context;
        this.coreSharedPreferences = context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE, Context.MODE_PRIVATE);
        this.deviceInfoProvider = new DeviceInfoProvider(context);
        this.tenantInfo = null;
        this.userInfo = UserInfo.newInstance(context);

        this.localConfigKeysPreferences =
                context.getSharedPreferences(TenantConfigsKeys.LOCAL_INIT_SP_FILE, Context.MODE_PRIVATE);
        this.lifecycleObserver = new LifecycleObserver();
        EventHandlerFactory eventHandlerFactory = EventHandlerFactory.builder()
                .userInfo(userInfo)
                .httpClient(HttpClient.getInstance(context))
                .maximumBufferSize(OPTITRACK_BUFFER_SIZE)
                .optistreamDbHelper(new OptistreamDbHelper(context))
                .lifecycleObserver(lifecycleObserver)
                .context(context)
                .build();
        this.eventHandlerProvider = new EventHandlerProvider(eventHandlerFactory);

        this.optipushManager = new OptipushManager(new RegistrationDao(context),
                deviceInfoProvider, HttpClient.getInstance(context), lifecycleObserver, context);
        this.optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                ApplicationHelper.getFullPackageName(context),
                context.getSharedPreferences(OPTITRACK_SP_NAME, Context.MODE_PRIVATE), deviceInfoProvider);
        this.configSet = new AtomicBoolean(false);
    }

    /**
     * Gets the {@link Optimove} {@code singleton}. {@link Optimove#configure(Context, TenantInfo)} must be called before trying to access {@code Optimove}.
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
     * @param context    The instance of the current {@code Context} object.
     * @param tenantInfo The {@link TenantInfo} as provided by <i>Optimove</i>
     */
    public static void configure(Context context, TenantInfo tenantInfo) {
        Context applicationContext = context.getApplicationContext();
        if (!(applicationContext instanceof Application)) {
            OptiLoggerStreamsContainer.fatal("Optimove#configure", "Can't initialize Optimove SDK since the ApplicationContext isn't an instance of Application class but of %s",
                    applicationContext.getClass()
                            .getCanonicalName());
            return;
        }

        Runnable initCommand = () -> {
            boolean initializedSuccessfully = performSingletonInitialization(context, tenantInfo);
            if (initializedSuccessfully) {
                OptiLoggerStreamsContainer.debug("Optimove.configure() is starting");
                shared.lifecycleObserver.addActivityStoppedListener(shared.optimoveLifecycleEventGenerator);
                shared.lifecycleObserver.addActivityStartedListener(shared.optimoveLifecycleEventGenerator);
                ((Application) applicationContext).registerActivityLifecycleCallbacks(shared.lifecycleObserver);
                shared.fetchConfigs(false);
            }
        };
        if (!OptiUtils.isRunningOnMainThread()) {
            OptiLoggerStreamsContainer.debug("Optimove.configure() was called from a worker thread, moving call to main thread");
            OptiUtils.runOnMainThread(initCommand);
        } else {
            initCommand.run();
        }
    }

    /**
     * Initializes the {@code Optimove SDK}. <b>Must</b> be called from the <b>Main</b> thread.<br>
     * Must be called as soon as possible ({@link Application#onCreate()} is the ideal place), and before any call to {@link Optimove#getInstance()}.
     *
     * @param context     The instance of the current {@code Context} object.
     * @param tenantInfo  The {@link TenantInfo} as provided by <i>Optimove</i>.
     * @param isStgEnv    An indication whether this is a staging environment.
     * @param minLogLevel Logcat minimum log level to show.
     */
    public static void configure(Context context, TenantInfo tenantInfo, Boolean isStgEnv, LogLevel minLogLevel) {
        OptiLoggerStreamsContainer.setMinLogLevelToShow(minLogLevel);

        if (isStgEnv) {
            OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.DEBUG);
        }
        configure(context, tenantInfo);
    }

    /**
     * THIS IS AN <b>INTERNAL</b> FUNCTION, <b>NOT</b> TO BE CALLED BY THE CLIENT.
     * <p>
     * Initializes the {@code Optimove SDK} from local the configuration file.<br>
     * <b>Discussion</b>: Background components need lean initialization that supports flows where the Application's {@code onCreate} callback wasn't called yet (e.g. Content providers
     * and some observed crashes on Services in Android 8.0). This flow requires only {@code Context} and is faster.
     */
    public static void configureUrgently(Context context) {
        OptiLoggerStreamsContainer.debug("Optimove.configureUrgently() is starting");
        boolean initializedSuccessfully = performSingletonInitialization(context, null);
        if (initializedSuccessfully) {
            shared.executeUrgentInit();
        }
    }

    public EventHandlerProvider getEventHandlerProvider() {
        return eventHandlerProvider;
    }

    private void fetchConfigs(boolean isUrgent) {
        ConfigsFetcher configsFetcher = ConfigsFetcher.builder()
                .httpClient(HttpClient.getInstance(context))
                .tenantToken(tenantInfo.getTenantToken())
                .configName(tenantInfo.getConfigName())
                .urgent(isUrgent)
                .sharedPrefs(localConfigKeysPreferences)
                .fileProvider(new FileUtils())
                .context(context)
                .build();
        configsFetcher.fetchConfigs(this::setConfigurationsIfNotSet,
                OptiLogger::failedToGetConfigurationFile);
    }

    private void executeUrgentInit() {
        if (!configSet.get()) {
            fetchConfigs(true);
        } else {
            OptiLoggerStreamsContainer.debug("Configuration file was already loaded, no need to load again");
        }
    }

    private void setConfigurationsIfNotSet(@NonNull Configs configs) {

        if (configSet.compareAndSet(false, true)) {
            updateConfigurations(configs);
        } else {
            OptiLogger.configurationsAreAlreadySet();
        }
    }

    private void updateConfigurations(Configs configs) {
        loadTenantId(configs);
        if (configs.getLogsConfigs().isProdLogsEnabled()) {
            OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.ERROR);
        }
        OptiLoggerStreamsContainer.debug("Updating the configurations for tenant ID %d", tenantInfo.getTenantId());

        optipushManager.processConfigs(configs.getOptipushConfigs(), tenantInfo.getTenantId(), userInfo);
        eventHandlerProvider.processConfigs(configs);
        //sends initial events
        EventGenerator eventGenerator =
                EventGenerator.builder()
                        .withUserInfo(userInfo)
                        .withPackageName(ApplicationHelper.getFullPackageName(context))
                        .withDeviceId(userInfo.getInstallationId())
                        .withRequirementProvider(deviceInfoProvider)
                        .withTenantInfo(tenantInfo)
                        .withEventHandlerProvider(eventHandlerProvider)
                        .withContext(context)
                        .build();

        eventGenerator.generateStartEvents(configs.getOptitrackConfigs()
                .isEnableAdvertisingIdReport());
    }

    private void loadTenantId(Configs configs) {
        // If this is the first time the tenantId was set, we need to update the Service Logger (if exists)
        for (OptiLoggerOutputStream stream : OptiLoggerStreamsContainer.getLoggerOutputStreams()) {
            if (stream instanceof SdkLogsServiceOutputStream) {
                SdkLogsServiceOutputStream logsServiceOutputStream = (SdkLogsServiceOutputStream) stream;
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
     * @param applicationContext The Application's Context.
     * @param newTenantInfo      The {@code TenantInfo} that was sent by the client.
     * @throws IllegalArgumentException if <b>both</b> the new and the local {@code TenantInfo}s passed are null.
     */
    private static boolean performSingletonInitialization(Context applicationContext, TenantInfo newTenantInfo) {
        if (shared != null) {
            boolean tenantInfoExists = (shared.retrieveLocalTenantInfo() != null || newTenantInfo != null);
            if (!tenantInfoExists) {
                OptiLogger.optimoveInitializationFailedDueToCorruptedTenantInfo();
            }
            return tenantInfoExists;
        } else {
            synchronized (LOCK) {
                shared = new Optimove(applicationContext);

                TenantInfo localTenantInfo = shared.retrieveLocalTenantInfo();
                if (newTenantInfo == null && localTenantInfo == null) {
                    OptiLogger.optimoveInitializationFailedDueToCorruptedTenantInfo();
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
    }

    /* *******************
     * Public API
     ******************* */

    /**
     * Method that performs both the {@code setUserId} and the {@code setUserEmail} flows from a single call.
     *
     * @param sdkId The new User's SDK ID to set
     * @param email the <i>email address</i> to attach
     * @see Optimove#setUserId(String)
     * @see Optimove#setUserEmail(String)
     */
    public void registerUser(String sdkId, String email) {
        SetUserIdEvent setUserIdEvent = processUserId(sdkId);
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
     * @param sdkId The new User' SDK ID to set
     */
    public void setUserId(String sdkId) {
        SetUserIdEvent setUserIdEvent = processUserId(sdkId);
        if (setUserIdEvent != null) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(setUserIdEvent));
        }
    }

    private @Nullable SetEmailEvent processUserEmail(String email){
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

    private @Nullable SetUserIdEvent processUserId(String userId) {
        if (OptiUtils.isNullNoneOrUndefined(userId)) {
            return new SetUserIdEvent(this.userInfo.getInitialVisitorId(), null, this.userInfo.getVisitorId());
        } else if (userId.length() > USER_ID_MAX_LENGTH) {
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

        optipushManager.userIdChanged();

        return new SetUserIdEvent(originalVisitorId, newUserId, updatedVisitorId);
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

    public void disablePushCampaigns() {
        optipushManager.disablePushCampaigns();
    }

    public void enablePushCampaigns() {
        optipushManager.enablePushCampaigns();
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

    @NonNull
    public OptipushManager getOptipushManager() {
        return optipushManager;
    }


    /* *******************
     * Private Instance Methods
     ******************* */

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
