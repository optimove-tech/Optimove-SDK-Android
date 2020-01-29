package com.optimove.sdk.optimove_sdk.optipush.registration;


import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.AddAliasRequest;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.SetUserRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class OptipushUserRegistrar implements LifecycleObserver.ActivityStarted {

    private String registrationEndPoint;
    private HttpClient httpClient;
    private String packageName;
    private int tenantId;
    private RequirementProvider requirementProvider;
    private RegistrationDao registrationDao;
    private UserInfo userInfo;
    private InstallationIDProvider installationIDProvider;

    private OptipushUserRegistrar(String registrationEndPoint,
                                  HttpClient httpClient, String packageName, int tenantId,
                                  RequirementProvider requirementProvider,
                                  RegistrationDao registrationDao, UserInfo userInfo, InstallationIDProvider installationIDProvider) {
        this.registrationEndPoint = registrationEndPoint;
        this.httpClient = httpClient;
        this.packageName = packageName;
        this.tenantId = tenantId;
        this.requirementProvider = requirementProvider;
        this.registrationDao = registrationDao;
        this.userInfo = userInfo;
        this.installationIDProvider = installationIDProvider;
    }

    public static OptipushUserRegistrar create(String registrationEndPoint,
                                               HttpClient httpClient, String packageName, int tenantId,
                                               RequirementProvider requirementProvider,
                                               RegistrationDao registrationDao, UserInfo userInfo,
                                               InstallationIDProvider installationIDProvider,
                                               LifecycleObserver lifecycleObserver) {
        OptipushUserRegistrar optipushUserRegistrar = new OptipushUserRegistrar(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider);
        optipushUserRegistrar.synchronizeRegistration();
        lifecycleObserver.addActivityStartedListener(optipushUserRegistrar);

        return optipushUserRegistrar;
    }

    @Override
    public void activityStarted(){
        optInOutUser();
    }
    private void synchronizeRegistration() {
        if (registrationDao.isSetUserMarkedAsFailed()) {
            dispatchSetUserRequest(registrationDao.getLastToken());
        } else {
            optInOutUser();
        }
        addAllPreviousFailedUserAliases();
    }

    private void optInOutUser() {
        boolean userOptIn = requirementProvider.notificaionsAreEnabled();
        boolean wasTheUserOptIn = registrationDao.wasTheUserOptIn();
        String lastToken = registrationDao.getLastToken();
        if (lastToken != null && wasTheUserOptIn != userOptIn) {
            dispatchSetUserRequest(lastToken);
        }
    }

    private void addAllPreviousFailedUserAliases() {
        Set<String> failedUserAliases = registrationDao.getFailedUserAliases();
        if (failedUserAliases != null) {
            dispatchAddUserAliases(userInfo.getInitialVisitorId(), failedUserAliases);
        }
    }

    public void userIdChanged(String initialVisitorId, String userId) {
        Set<String> failedUserAliases = registrationDao.getFailedUserAliases();
        if (failedUserAliases != null) {
            //make a copy so that shared prefs wont be affected directly
            Set<String> failedUserAliasesCopy = new HashSet<>(failedUserAliases);
            failedUserAliasesCopy.add(userId);
            dispatchAddUserAliases(initialVisitorId, failedUserAliasesCopy);
        } else {
            dispatchAddUserAliases(initialVisitorId, new HashSet<String>() {{
                add(userId);
            }});
        }
    }

    public void userTokenChanged() {
        dispatchSetUserRequest(registrationDao.getLastToken());
    }

    private void dispatchSetUserRequest(String token) {
        SetUserRequest setUserRequest =
                SetUserRequest.builder()
                        .withOptIn(requirementProvider.notificaionsAreEnabled())
                        .withDeviceId(installationIDProvider.getInstallationID())
                        .withPackageName(packageName)
                        .withOs("android")
                        .withDeviceToken(token)
                        .build();
        try {
            JSONObject setUserRequestJson = new JSONObject(new Gson().toJson(setUserRequest));
            OptiLoggerStreamsContainer.debug("Setting user with data: %s", setUserRequestJson.toString());
            httpClient.postJsonWithoutJsonResponse(registrationEndPoint, setUserRequestJson)
                    .errorListener(this::setUserError)
                    .successListener(this::setUserSuccess)
                    .destination("%s/%s/%s/%s", "tenants",tenantId,"users",
                            userInfo.getInitialVisitorId())
                    .send();
        } catch (JSONException e) {
            setUserError(e);
        }

    }

    private void dispatchAddUserAliases(String initialiVisitorId, Set<String> userAliases) {
        AddAliasRequest addAliasRequest = new AddAliasRequest(userAliases);
        try {
            JSONObject addAliasRequestJson = new JSONObject(new Gson().toJson(addAliasRequest));
            OptiLoggerStreamsContainer.debug("Adding user alias with data: %s", addAliasRequestJson.toString());

            httpClient.putJsonWithoutJsonResponse(registrationEndPoint, addAliasRequestJson)
                    .errorListener(error -> addUserAliasError(error, userAliases))
                    .successListener(this::addUserAliasSuccess)
                    .destination("%s/%s/%s/%s","tenants",tenantId, "users", initialiVisitorId)
                    .send();
        } catch (JSONException e) {
            addUserAliasError(e, userAliases);
        }
    }

    private void setUserSuccess(JSONObject jsonObject) {
        registrationDao.editFlags()
                .unmarkSetUserAsFailed()
                .updateLastOptInStatus(requirementProvider.notificaionsAreEnabled())
                .save();
    }

    private void setUserError(Exception error) {
        OptiLoggerStreamsContainer.debug("Set user failed - %s", error.getMessage());
        registrationDao.editFlags()
                .markSetUserAsFailed()
                .save();
    }

    private void addUserAliasSuccess(JSONObject jsonObject) {
        registrationDao.editFlags()
                .unmarkAddUserAliaseAsFailed()
                .save();
    }

    private void addUserAliasError(Exception error, Set<String> userAliases) {
        OptiLoggerStreamsContainer.debug("Add user alias failed - %s", error.getMessage());
        registrationDao.editFlags()
                .markAddUserAliasesAsFailed(userAliases)
                .save();
    }
}

