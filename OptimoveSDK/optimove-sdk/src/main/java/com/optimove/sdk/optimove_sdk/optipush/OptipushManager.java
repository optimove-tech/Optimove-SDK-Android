package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInitializer;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushFcmTokenHandler;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

public final class OptipushManager {

    @NonNull
    private RegistrationDao registrationDao;
    @NonNull
    private DeviceInfoProvider deviceInfoProvider;
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private Context context;

    @Nullable
    private OptipushUserRegistrar optipushUserRegistrar;

    public OptipushManager(@NonNull RegistrationDao registrationDao,@NonNull DeviceInfoProvider deviceInfoProvider,
                           @NonNull HttpClient httpClient,@NonNull LifecycleObserver lifecycleObserver,
                           @NonNull Context context) {
        this.registrationDao = registrationDao;
        this.deviceInfoProvider = deviceInfoProvider;
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;
    }

    public void tokenWasChanged() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.userTokenChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .save();
        }
    }

    public void userIdChanged() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.userIdChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .save();
        }
    }

    public void optipushMessageCommand(RemoteMessage remoteMessage) {
        if (registrationDao.isPushCampaignsDisabled()){
            return;
        }
        NotificationCreator notificationCreator = new NotificationCreator(context);
        NotificationData notificationData = JsonUtils.parseJsonMap(remoteMessage.getData(),  NotificationData.class);
        if (notificationData == null) {
            OptiLoggerStreamsContainer.fatal("Seems like you forgot to add the proguard rules of Optimove");
            return;
        }
        new OptipushMessageCommand(context, Optimove.getInstance()
                .getEventHandlerProvider()
                .getEventHandler(),
                new DeviceInfoProvider(context), notificationCreator)
                .processRemoteMessage(remoteMessage, notificationData);
    }

    public void disablePushCampaigns(){
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.disablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .disablePushCampaigns()
                    .save();
        }
    }

    public void enablePushCampaigns(){
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.enablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .enablePushCampaigns()
                    .save();
        }
    }


    public void processConfigs(OptipushConfigs optipushConfigs, int tenantId, UserInfo userInfo) {
        if (!deviceInfoProvider.isGooglePlayServicesAvailable()) {
            return;
        }

        boolean succeeded = new OptimoveFirebaseInitializer(context).setup(optipushConfigs);
        if (!succeeded) {
            return;
        }

        this.optipushUserRegistrar =
                OptipushUserRegistrar.create(optipushConfigs.getRegistrationServiceEndpoint(), httpClient,
                        context.getPackageName(), tenantId, deviceInfoProvider, registrationDao, userInfo,
                        lifecycleObserver, getMetadata());

        new OptipushFcmTokenHandler().syncTokenIfRequired();
    }

    private Metadata getMetadata() {
        PackageManager packageManager = context.getPackageManager();
        String appVersion;
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            appVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersion = "VersionNotFound";
        }
        return new Metadata(BuildConfig.VERSION_NAME, appVersion, Build.VERSION.RELEASE,
                Build.MODEL);
    }
}

