package com.optimove.sdk.optimove_sdk.optipush.registration;


import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.InstallationRequest;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

import org.json.JSONException;
import org.json.JSONObject;

public class OptipushUserRegistrar implements LifecycleObserver.ActivityStarted {

    private String registrationEndPoint;
    private HttpClient httpClient;
    private String packageName;
    private int tenantId;
    private DeviceInfoProvider deviceInfoProvider;
    private RegistrationDao registrationDao;
    private UserInfo userInfo;
    private Metadata metadata;

    private OptipushUserRegistrar(String registrationEndPoint,
                                  HttpClient httpClient, String packageName, int tenantId,
                                  DeviceInfoProvider deviceInfoProvider,
                                  RegistrationDao registrationDao, UserInfo userInfo,
                                  Metadata metadata) {
        this.registrationEndPoint = registrationEndPoint;
        this.httpClient = httpClient;
        this.packageName = packageName;
        this.tenantId = tenantId;
        this.deviceInfoProvider = deviceInfoProvider;
        this.registrationDao = registrationDao;
        this.userInfo = userInfo;
        this.metadata = metadata;
    }

    public static OptipushUserRegistrar create(String registrationEndPoint,
                                               HttpClient httpClient, String packageName, int tenantId,
                                               DeviceInfoProvider deviceInfoProvider,
                                               RegistrationDao registrationDao, UserInfo userInfo,
                                               LifecycleObserver lifecycleObserver, Metadata metadata) {
        OptipushUserRegistrar optipushUserRegistrar = new OptipushUserRegistrar(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo, metadata);
        optipushUserRegistrar.registerIfNeeded();
        lifecycleObserver.addActivityStartedListener(optipushUserRegistrar);

        return optipushUserRegistrar;
    }

    public void userTokenChanged() {
        if (registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        } else {
            OptiLoggerStreamsContainer.error("User token changed but doesn't exist in storage");
        }
    }

    public void userIdChanged() {
        if (registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        }
    }

    @Override
    public void activityStarted() {
        if (checkIfOptInOutWasChanged() && registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        }
    }

    public void disablePushCampaigns() {
        registrationDao.editFlags()
                .disablePushCampaigns()
                .save();
        if (registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        }
    }

    public void enablePushCampaigns() {
        registrationDao.editFlags()
                .enablePushCampaigns()
                .save();
        if (registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        }
    }

    private void registerIfNeeded() {
        if ((registrationDao.isSetInstallationMarkedAsFailed()
                || checkIfOptInOutWasChanged()
                || (registrationDao.getFailedUserAliases() != null)
                || (!registrationDao.isApiV3Synced()))
                && registrationDao.getLastToken() != null) {
            dispatchSetInstallation();
        }
    }

    private boolean checkIfOptInOutWasChanged() {
        boolean currentUserOptIn = deviceInfoProvider.notificaionsAreEnabled();
        boolean previousUserOptIn = registrationDao.wasTheUserOptIn();
        return currentUserOptIn != previousUserOptIn;
    }

    private void dispatchSetInstallation() {
        InstallationRequest installationRequest =
                InstallationRequest.builder()
                        .withInstallationId(userInfo.getInstallationId())
                        .withVisitorId(userInfo.getInitialVisitorId())
                        .withCustomerId(userInfo.getUserId())
                        .withDeviceToken(registrationDao.getLastToken())
                        .withPushProvider("fcm")
                        .withPackageName(packageName)
                        .withOs("android")
                        .withOptIn(deviceInfoProvider.notificaionsAreEnabled())
                        .withIsDev(false)
                        .withIsPushCampaignsDisabled(registrationDao.isPushCampaignsDisabled())
                        .withMetadata(metadata)
                        .build();
        try {
            JSONObject installationRequestJson = new JSONObject(new Gson().toJson(installationRequest));
            OptiLoggerStreamsContainer.debug("Sending installation info with data: %s", installationRequestJson.toString());
            registrationDao.editFlags()
                    .markApiV3AsSynced()
                    .save();
            httpClient.postJsonWithoutJsonResponse(registrationEndPoint, installationRequestJson)
                    .errorListener(this::setInstallationFailed)
                    .successListener(this::setInstallationSucceeded)
                    .destination("%s/%s/%s/%s", "v3", "tenants", tenantId,
                            "installation")
                    .send();
        } catch (JSONException e) {
            setInstallationFailed(e);
        }

    }

    private void setInstallationSucceeded(JSONObject jsonObject) {
        registrationDao.editFlags()
                .unmarkSetInstallationAsFailed()
                .updateLastOptInStatus(deviceInfoProvider.notificaionsAreEnabled())
                .unmarkAddUserAliaseAsFailed() //for previous versions fails - the rare case when there are fails
                // before an app upgrade
                .save();
    }

    private void setInstallationFailed(Exception error) {
        OptiLoggerStreamsContainer.debug("Set installation failed - %s", error.getMessage());
        registrationDao.editFlags()
                .markSetInstallationAsFailed()
                .save();
    }
}

