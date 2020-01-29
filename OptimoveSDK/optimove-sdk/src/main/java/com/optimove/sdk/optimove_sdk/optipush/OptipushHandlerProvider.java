package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushFcmTokenHandler;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

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
        OptimoveFirebaseInteractor optimoveFirebaseInteractor = new OptimoveFirebaseInteractor(optipushConfigs);

        boolean succeeded = optimoveFirebaseInteractor.setup(optipushConfigs);
        if (!succeeded) {
            return;
        }
        new OptipushFcmTokenHandler().completeLastTokenRefreshIfFailed();

        OptipushUserRegistrar optipushUserRegistrar =
                OptipushUserRegistrar.create(optipushConfigs.getRegistrationServiceEndpoint(), httpClient,
                        context.getPackageName(), tenantId, requirementProvider, registrationDao, userInfo,
                        installationIDProvider,
                        lifecycleObserver);

        OptipushManager optipushManager =
                new OptipushManager(optimoveFirebaseInteractor, optipushUserRegistrar, context);

        optipushBuffer.setNext(optipushManager);
    }

    public OptipushHandler getOptipushHandler() {
        return optipushBuffer;
    }
}
