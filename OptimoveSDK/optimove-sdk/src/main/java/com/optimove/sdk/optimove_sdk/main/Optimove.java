package com.optimove.sdk.optimove_sdk.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.SdkLogsServiceOutputStream;
import com.optimove.sdk.optimove_sdk.optipush.OptipushManager;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optitrack.MatomoAdapter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.CONFIG_NAME;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;
import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TOKEN;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_BUFFER_SIZE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_SP_NAME;

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
    private RequirementProvider requirementProvider;
    private AtomicBoolean configSet;
    private LifecycleObserver lifecycleObserver;

    private Optimove(Context context) {
        this.context = context;
        this.coreSharedPreferences = context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE, Context.MODE_PRIVATE);
        this.requirementProvider = new RequirementProvider(context);
        this.tenantInfo = null;
        this.userInfo = UserInfo.newInstance(context);

        this.localConfigKeysPreferences =
                context.getSharedPreferences(TenantConfigsKeys.LOCAL_INIT_SP_FILE, Context.MODE_PRIVATE);
        EventHandlerFactory eventHandlerFactory = EventHandlerFactory.builder()
                .optitrackAdapter(new MatomoAdapter())
                .userInfo(userInfo)
                .httpClient(HttpClient.getInstance(context))
                .maximumBufferSize(OPTITRACK_BUFFER_SIZE)
                .context(context)
                .build();
        this.lifecycleObserver = new LifecycleObserver();
        this.eventHandlerProvider = new EventHandlerProvider(eventHandlerFactory, lifecycleObserver);

        this.optipushManager = new OptipushManager(new RegistrationDao(context),
                requirementProvider, HttpClient.getInstance(context), lifecycleObserver, context);
        this.optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                ApplicationHelper.getFullPackageName(context),
                context.getSharedPreferences(OPTITRACK_SP_NAME, Context.MODE_PRIVATE), requirementProvider);
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
                OptiLogger.f82();
                shared.lifecycleObserver.addActivityStoppedListener(shared.optimoveLifecycleEventGenerator);
                shared.lifecycleObserver.addActivityStartedListener(shared.optimoveLifecycleEventGenerator);
                ((Application) applicationContext).registerActivityLifecycleCallbacks(shared.lifecycleObserver);
                shared.fetchConfigs(false);
            }
        };
        if (!OptiUtils.isRunningOnMainThread()) {
            OptiLogger.f83();
            OptiUtils.runOnMainThread(initCommand);
        } else {
            initCommand.run();
        }
    }
    /**
     * Initializes the {@code Optimove SDK}. <b>Must</b> be called from the <b>Main</b> thread.<br>
     * Must be called as soon as possible ({@link Application#onCreate()} is the ideal place), and before any call to {@link Optimove#getInstance()}.
     *
     * @param context    The instance of the current {@code Context} object.
     * @param tenantInfo The {@link TenantInfo} as provided by <i>Optimove</i>.
     * @param isStgEnv An indication whether this is a staging environment.
     * @param minLogLevel Logcat minimum log level to show.
     */
    public static void configure(Context context, TenantInfo tenantInfo, Boolean isStgEnv, LogLevel minLogLevel) {
        OptiLoggerStreamsContainer.setMinLogLevelToShow(minLogLevel);

        if (isStgEnv) {
            SharedPreferences coreSharedPreferences =
                    context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE, Context.MODE_PRIVATE);
            OptiLoggerStreamsContainer.addOutputStream(new SdkLogsServiceOutputStream(context,
                    coreSharedPreferences.getInt(TENANT_ID, -1)));
        }
        configure(context,tenantInfo);
    }

    /**
     * THIS IS AN <b>INTERNAL</b> FUNCTION, <b>NOT</b> TO BE CALLED BY THE CLIENT.
     * <p>
     * Initializes the {@code Optimove SDK} from local the configuration file.<br>
     * <b>Discussion</b>: Background components need lean initialization that supports flows where the Application's {@code onCreate} callback wasn't called yet (e.g. Content providers
     * and some observed crashes on Services in Android 8.0). This flow requires only {@code Context} and is faster.
     */
    public static void configureUrgently(Context context) {
        OptiLogger.f84();
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
            OptiLogger.f116();
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
        OptiLogger.f117(tenantInfo.getTenantId());

        optipushManager.processConfigs(configs.getOptipushConfigs(), tenantInfo.getTenantId(), userInfo);
        eventHandlerProvider.processConfigs(configs);
        //sends initial events
        EventGenerator eventGenerator =
                EventGenerator.builder()
                        .withUserInfo(userInfo)
                        .withPackageName(ApplicationHelper.getFullPackageName(context))
                        .withDeviceId(userInfo.getInstallationId())
                        .withRequirementProvider(requirementProvider)
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
     * Attaches an <i>email address</i> to the current user.
     *
     * @param email the <i>email address</i> to attach
     */
    public void setUserEmail(String email) {
        if (OptiUtils.isNullNoneOrUndefined(email)) {
            OptiLogger.providedEmailIsNull();
            return;
        }
        String trimmedEmail = email.trim();
        if (!OptiUtils.isValidEmailAddress(trimmedEmail)) {
            OptiLogger.f89(email);
            return;
        }

        if (this.userInfo.getEmail() != null && this.userInfo.getEmail()
                .equals(trimmedEmail)) {
            OptiLogger.providedEmailWasAlreadySet(email);
            return;
        }
        this.userInfo.setEmail(trimmedEmail);

        SetEmailEvent event = new SetEmailEvent(trimmedEmail);
        reportEvent(event);
    }

    /**
     * Sets the User ID of the current user and starts the {@code Visitor} to {@code Customer} conversion flow.<br>
     * <b>Note</b>: The user ID must be the same user ID that is passed to Optimove at the daily ETL
     *
     * @param sdkId The new User' SDK ID to set
     */
    public void setUserId(String sdkId) {
        if (OptiUtils.isNullNoneOrUndefined(sdkId)) {
            OptiLogger.f90(sdkId);
            return;
        }
        String newUserId = sdkId.trim(); // Safe to trim now as it could never be null

        if (this.userInfo.getUserId() != null && this.userInfo.getUserId()
                .equals(newUserId)) {
            OptiLogger.f91(sdkId);
            return;
        }

        String originalVisitorId = this.userInfo.getVisitorId();
        String updatedVisitorId = OptiUtils.SHA1(newUserId)
                .substring(0, 16);

        this.userInfo.setUserId(newUserId);
        this.userInfo.setVisitorId(updatedVisitorId);

        SetUserIdEvent setUserIdEvent = new SetUserIdEvent(originalVisitorId, newUserId, updatedVisitorId);

        eventHandlerProvider.getEventHandler()
                .reportEvent(new EventContext(setUserIdEvent));
        optipushManager
                .userIdChanged();
    }

    /**
     * Convenience method that performs both the {@code setUserId} and the {@code setUserEmail} flows from a single call.
     *
     * @param sdkId The new User's SDK ID to set
     * @param email the <i>email address</i> to attach
     * @see Optimove#setUserId(String)
     * @see Optimove#setUserEmail(String)
     */
    public void registerUser(String sdkId, String email) {
        setUserId(sdkId);
        setUserEmail(email);
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
        if (optimoveEvent.getName() == null) {
            OptiLogger.f95();
            return;
        }

        eventHandlerProvider.getEventHandler()
                .reportEvent(new EventContext(optimoveEvent));
    }

    public void reportScreenVisit(@NonNull String screenName) {
        this.reportScreenVisit(screenName, null);
    }

    @SuppressWarnings("ConstantConditions")
    public void reportScreenVisit(@NonNull String screenName, @Nullable String screenCategory) {
        if (OptiUtils.isEmptyOrWhitespace(screenName)) {
            OptiLogger.f97(screenName == null ? "null" : screenName);
            return;
        }

        eventHandlerProvider.getEventHandler()
                .reportEvent(new EventContext(new SetPageVisitEvent(screenName.trim(), screenCategory)));
    }

    public void disablePushCampaigns(){
        optipushManager.disablePushCampaigns();
    }

    public void enablePushCampaigns(){
        optipushManager.enablePushCampaigns();
    }

    /**
     * @deprecated Use the {@link Optimove#reportScreenVisit(String)} instead.
     */
    @Deprecated
    public void setScreenVisit(@NonNull Activity activity, @NonNull String screenTitle) {
        this.reportScreenVisit(screenTitle, null);
    }

    /**
     * @deprecated Use the {@link Optimove#reportScreenVisit(String,String)} instead.
     */
    @Deprecated
    public void setScreenVisit(@NonNull Activity activity, @NonNull String screenTitle,
                               @Nullable String screenCategory) {
        this.reportScreenVisit(screenTitle, screenCategory);
    }

    /**
     * @deprecated Use the {@link Optimove#reportScreenVisit(String)} instead.
     */
    @Deprecated
    public void setScreenVisit(@NonNull String screenPath, @NonNull String screenTitle) {
        this.reportScreenVisit(screenTitle, null);
    }

    /**
     * @deprecated Use the {@link Optimove#reportScreenVisit(String,String)} instead.
     */
    @Deprecated
    public void setScreenVisit(@NonNull String screenPath, @NonNull String screenTitle,
                               @Nullable String screenCategory) {
        this.reportScreenVisit(screenTitle, screenCategory);
    }

    /**
     * @deprecated No need to call startTestMode.
     */
    @Deprecated
    public void startTestMode(@Nullable SdkOperationListener operationListener) {
    }

    /**
     * @deprecated No need to call stopTestMode.
     */
    @Deprecated
    public void stopTestMode(@Nullable SdkOperationListener operationListener) {
    }

    /**
     * Registers a new {@link OptimoveSuccessStateListener} to receive any updates about the {@code SDK's Success State} in a thread safe manner.
     *
     * @deprecated No need to register for lifecycle events. You can use the SDK directly.
     */
    @Deprecated
    public static void registerSuccessStateListener(OptimoveSuccessStateListener stateListener) {
        stateListener.onConfigurationSucceed();
    }

    /**
     * Unregisters an already registered {@link OptimoveSuccessStateListener} from receiving further updates about the {@code SDK's Success State} in a thread safe manner.<br>
     *
     * @deprecated No need to unregister from lifecycle events anymore.
     */
    @Deprecated
    public static void unregisterSuccessStateListener(OptimoveSuccessStateListener stateListener) {

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
