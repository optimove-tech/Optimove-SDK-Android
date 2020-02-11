package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.os.Build;

import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInitializer;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushFcmTokenHandler;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

public class OptipushHandlerProvider {

    private OptipushBuffer optipushBuffer;
    private RegistrationDao registrationDao;
    private RequirementProvider requirementProvider;
    private HttpClient httpClient;
    private LifecycleObserver lifecycleObserver;
    private Context context;
    private InstallationIDProvider installationIDProvider;


    public OptipushHandlerProvider(RegistrationDao registrationDao, RequirementProvider requirementProvider,
                                   HttpClient httpClient, LifecycleObserver lifecycleObserver,
                                   Context context, InstallationIDProvider installationIDProvider) {
        this.optipushBuffer = new OptipushBuffer(registrationDao);
        this.registrationDao = registrationDao;
        this.requirementProvider = requirementProvider;
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;
        this.installationIDProvider = installationIDProvider;

    }

    public void processConfigs(OptipushConfigs optipushConfigs, int tenantId, UserInfo userInfo) {
        if (!requirementProvider.isGooglePlayServicesAvailable()) {
            return;
        }

        boolean succeeded = new OptimoveFirebaseInitializer(context).setup(optipushConfigs);
        if (!succeeded) {
            return;
        }
        Object appVersionObject = OptiUtils.getBuildConfig(ApplicationHelper.getBasePackageName(context),
                "VERSION_NAME");

        String appVersion = String.valueOf(appVersionObject);

        Metadata installationMetadata =
                new Metadata(SdkMetadataEvent.NATIVE_SDK_VERSION, appVersion, Build.VERSION.RELEASE,
                        Build.MODEL);

        OptipushUserRegistrar optipushUserRegistrar =
                OptipushUserRegistrar.create(optipushConfigs.getRegistrationServiceEndpoint(), httpClient,
                        context.getPackageName(), tenantId, requirementProvider, registrationDao, userInfo,
                        installationIDProvider,
                        lifecycleObserver, installationMetadata);

        OptipushManager optipushManager =
                new OptipushManager(optipushUserRegistrar, context);

        optipushBuffer.setNext(optipushManager);
        new OptipushFcmTokenHandler().completeLastTokenRefreshIfFailed();
    }

    public OptipushHandler getOptipushHandler() {
        return optipushBuffer;
    }
}
