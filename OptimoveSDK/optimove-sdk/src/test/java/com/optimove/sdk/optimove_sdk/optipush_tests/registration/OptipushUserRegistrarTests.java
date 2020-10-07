package com.optimove.sdk.optimove_sdk.optipush_tests.registration;

import android.app.Activity;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class OptipushUserRegistrarTests {


    private String registrationEndPoint = "registration_endpoint";
    private String packageName = "some_package_name";
    private int tenantId = 355;
    private String token = "some_token";

    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpClient.RequestBuilder<JSONObject> requestBuilder;
    @Mock
    private DeviceInfoProvider deviceInfoProvider;
    @Mock
    private RegistrationDao registrationDao;
    @Mock
    private RegistrationDao.FlagsEditor flagsEditor;
    @Mock
    private UserInfo userInfo;
    @Mock
    private Metadata metadata;

    private LifecycleObserver lifecycleObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lifecycleObserver = new LifecycleObserver();

        //registration
        when(registrationDao.editFlags()).thenReturn(flagsEditor);
        when(flagsEditor.markSetInstallationAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.unmarkSetInstallationAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.updateLastOptInStatus(anyBoolean())).thenReturn(flagsEditor);
        when(flagsEditor.unmarkAddUserAliaseAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.markApiV3AsSynced()).thenReturn(flagsEditor);

        //http
        when(httpClient.postJsonWithoutJsonResponse(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.errorListener(any())).thenReturn(requestBuilder);
        when(requestBuilder.successListener(any())).thenReturn(requestBuilder);
        when(requestBuilder.destination(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.destination(any(), any(), any())).thenReturn(requestBuilder);


        //failed ops
        when(registrationDao.isTokenRefreshMarkedAsFailed()).thenReturn(false);
        when(registrationDao.isSetInstallationMarkedAsFailed()).thenReturn(false);
        when(registrationDao.wasTheUserOptIn()).thenReturn(true);
        when(registrationDao.getFailedUserAliases()).thenReturn(null);
        when(registrationDao.isApiV3Synced()).thenReturn(true);

        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.getLastToken()).thenReturn(token);


    }

    @Test
    public void optipushUserRegistrarCreateShouldOptOutUserIfCurrentlyOptoutAndWasOptin() {
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(false);
        when(registrationDao.wasTheUserOptIn()).thenReturn(true);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonOptinMatchOptin(arg, false)))));
    }

    @Test
    public void optipushUserRegistrarCreateShouldOptInUserIfCurrentlyOptinAndWasOptout() {
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.wasTheUserOptIn()).thenReturn(false);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonOptinMatchOptin(arg, true)))));
    }

    @Test
    public void optipushUserRegistrarCreateShouldntOptOrOutIfThereIsNoToken() {
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.wasTheUserOptIn()).thenReturn(false);
        when(registrationDao.getLastToken()).thenReturn(null);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        verifyZeroInteractions(httpClient);
    }

    private boolean jsonOptinMatchOptin(JSONObject jsonObject, boolean optIn) {
        try {
            return jsonObject.get("opt_in")
                    .equals(optIn);
        } catch (JSONException e) {
            return false;
        }
    }

    @Test
    public void optipushUserRegistrarCreateShouldUpdateTokenIfPreviousFailed() {
        when(registrationDao.isSetInstallationMarkedAsFailed()).thenReturn(true);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo, lifecycleObserver, metadata);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonTokenMatchToken(arg, token)))));
    }

    private boolean jsonTokenMatchToken(JSONObject jsonObject, String token) {
        try {
            return jsonObject.get("device_token")
                    .equals(token);
        } catch (JSONException e) {
            return false;
        }
    }

    @Test
    public void optipushUserRegistrarCreateShouldSetInstallationIfThereAreOldFailedAliases() {
        String userAlias = "some_user_alias";
        Set<String> userAliases = new HashSet<>();
        userAliases.add(userAlias);
        when(registrationDao.getFailedUserAliases()).thenReturn(userAliases);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonTokenMatchToken(arg, token)))));
    }



    @Test
    public void userTokenChangedShouldSendTheNewToken() {

        String newToken = "new_token";
        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);
        when(registrationDao.getLastToken()).thenReturn(newToken);

        optipushUserRegistrar.userTokenChanged();

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonTokenMatchToken(arg, newToken)))));
    }

    @Test
    public void userIdChangedShouldSendTheCurrentUserId() {
        String userId = "some_user_id";
        when(userInfo.getUserId()).thenReturn(userId);

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        optipushUserRegistrar.userIdChanged();

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonSetInstallationHasUserId(arg, userId)))));
    }

    private boolean jsonSetInstallationHasUserId(JSONObject jsonObject, String userId) {
        try {
            return jsonObject.get("customer_id")
                    .equals(userId);
        } catch (JSONException e) {
            return false;
        }
    }

    @Test
    public void userTokenChangedShouldSetSetInstallationFailedIfHttpError() {
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return requestBuilder;
        }).when(requestBuilder)
                .errorListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);
        optipushUserRegistrar.userTokenChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .markSetInstallationAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }

    @Test
    public void userTokenChangedShouldUnmarkInstallationFailedIfHttpSuccess() {
        doAnswer(invocation -> {
            Response.Listener<JSONObject> successListener =
                    (Response.Listener<JSONObject>) invocation.getArguments()[0];
            successListener.onResponse(null);
            return requestBuilder;
        }).when(requestBuilder)
                .successListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);
        optipushUserRegistrar.userTokenChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .unmarkSetInstallationAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }
    @Test
    public void userChangedShouldSetFailedAliasIfHttpError() {
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return requestBuilder;
        }).when(requestBuilder)
                .errorListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);
        optipushUserRegistrar.userTokenChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .markSetInstallationAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }
    @Test
    public void userChangedShouldRemoveFailedAliasIfHttpSuccess() {
        doAnswer(invocation -> {
            Response.Listener<JSONObject> successListener =
                    (Response.Listener<JSONObject>) invocation.getArguments()[0];
            successListener.onResponse(null);
            return requestBuilder;
        }).when(requestBuilder)
                .successListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);
        optipushUserRegistrar.userIdChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .unmarkSetInstallationAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }

    @Test
    public void activityStartedOptinChangedAndTokenExistsShouldTriggerInstallationRequest(){
        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, deviceInfoProvider, registrationDao, userInfo,
                lifecycleObserver, metadata);

        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(false);

        lifecycleObserver.onActivityStarted(Mockito.mock(Activity.class));

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonOptinMatchOptin(arg, false)))));
    }

}


