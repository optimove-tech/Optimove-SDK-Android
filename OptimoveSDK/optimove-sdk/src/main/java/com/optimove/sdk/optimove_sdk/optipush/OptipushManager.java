package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.common.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.common.UserInfo;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

import java.util.concurrent.atomic.AtomicBoolean;

public final class OptipushManager {


    // Don't forget to set back to false once processing is done
    private AtomicBoolean tokenRefreshInProgress;
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

    public OptipushManager(@NonNull RegistrationDao registrationDao,
                           @NonNull DeviceInfoProvider deviceInfoProvider,
                           @NonNull HttpClient httpClient, @NonNull LifecycleObserver lifecycleObserver,
                           @NonNull Context context) {
        this.registrationDao = registrationDao;
        this.deviceInfoProvider = deviceInfoProvider;
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;
        this.tokenRefreshInProgress = new AtomicBoolean(false);
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
        if (registrationDao.isPushCampaignsDisabled()) {
            return;
        }
        NotificationCreator notificationCreator = new NotificationCreator(context);
        NotificationData notificationData = JsonUtils.parseJsonMap(remoteMessage.getData(), NotificationData.class);
        if (notificationData == null) {
            OptiLoggerStreamsContainer.error("No notification data");
            return;
        }
        new OptipushMessageCommand(context, Optimove.getInstance()
                .getEventHandlerProvider()
                .getEventHandler(),
                new DeviceInfoProvider(context), notificationCreator)
                .processRemoteMessage(remoteMessage, notificationData);
    }

    public void disablePushCampaigns() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.disablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .disablePushCampaigns()
                    .save();
        }
    }

    public void enablePushCampaigns() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.enablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .enablePushCampaigns()
                    .save();
        }
    }


    public void processConfigs(String optipushRegistrationServiceEndpoint, int tenantId, UserInfo userInfo) {
        if (!deviceInfoProvider.isGooglePlayServicesAvailable()) {
            OptiLoggerStreamsContainer.error("GooglePlay services are not available");
            return;
        }
        this.optipushUserRegistrar =
                OptipushUserRegistrar.create(optipushRegistrationServiceEndpoint, httpClient,
                        context.getPackageName(), tenantId, deviceInfoProvider, registrationDao, userInfo,
                        lifecycleObserver, getMetadata());
        syncToken();
    }

    public void syncToken() {
        if (optipushUserRegistrar == null){
            // refresh is about to happen due to config fetch
            return;
        }
        if (!tokenRefreshInProgress.compareAndSet(false, true)) {
            // refresh is in progress
            return;
        }

        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            OptiLoggerStreamsContainer.error("Failed to get token");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        String lastToken = registrationDao.getLastToken();
                        if (lastToken == null || !lastToken.equals(token)) {
                            registrationDao.editFlags()
                                    .putNewToken(token)
                                    .save();
                            optipushUserRegistrar.userTokenChanged();
                        }
                        tokenRefreshInProgress.set(false);
                    });
        } catch (IllegalStateException e) {
            OptiLoggerStreamsContainer.warn(e.getMessage());
        } catch (Throwable throwable) {
            OptiLoggerStreamsContainer.error(throwable.getMessage());
        }


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

