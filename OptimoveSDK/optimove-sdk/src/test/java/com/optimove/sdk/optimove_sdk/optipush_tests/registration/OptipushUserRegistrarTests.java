package com.optimove.sdk.optimove_sdk.optipush_tests.registration;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import org.json.JSONArray;
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
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
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
    private RequirementProvider requirementProvider;
    @Mock
    private RegistrationDao registrationDao;
    @Mock
    private RegistrationDao.FlagsEditor flagsEditor;
    @Mock
    private UserInfo userInfo;
    @Mock
    private LifecycleObserver lifecycleObserver;
    @Mock
    private InstallationIDProvider installationIDProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        //registration
        when(registrationDao.editFlags()).thenReturn(flagsEditor);
        when(flagsEditor.markSetUserAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.markAddUserAliasesAsFailed(anySet())).thenReturn(flagsEditor);
        when(flagsEditor.unmarkSetUserAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.unmarkAddUserAliaseAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.setDeviceId(anyString())).thenReturn(flagsEditor);
        when(flagsEditor.updateLastOptInStatus(anyBoolean())).thenReturn(flagsEditor);

        //http
        when(httpClient.postJsonWithoutJsonResponse(any(), any())).thenReturn(requestBuilder);
        when(httpClient.putJsonWithoutJsonResponse(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.errorListener(any())).thenReturn(requestBuilder);
        when(requestBuilder.successListener(any())).thenReturn(requestBuilder);
        when(requestBuilder.destination(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.destination(any(), any(), any())).thenReturn(requestBuilder);


        //failed ops
        when(registrationDao.isTokenRefreshMarkedAsFailed()).thenReturn(false);
        when(registrationDao.isSetUserMarkedAsFailed()).thenReturn(false);
        when(registrationDao.wasTheUserOptIn()).thenReturn(true);
        when(registrationDao.getFailedUserAliases()).thenReturn(null);

        when(requirementProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.getLastToken()).thenReturn(token);

        //installationIDProvider
        when(installationIDProvider.getInstallationID()).thenReturn("some_device_id");

    }

    @Test
    public void optipushUserRegistrarCreateShouldOptOutUserIfCurrentlyOptoutAndWasOptin() {
        when(requirementProvider.notificaionsAreEnabled()).thenReturn(false);
        when(registrationDao.wasTheUserOptIn()).thenReturn(true);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo,installationIDProvider,
                lifecycleObserver);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonOptinMatchOptin(arg, false)))));
    }

    @Test
    public void optipushUserRegistrarCreateShouldOptInUserIfCurrentlyOptinAndWasOptout() {
        when(requirementProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.wasTheUserOptIn()).thenReturn(false);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider,
                lifecycleObserver);

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonOptinMatchOptin(arg, true)))));
    }

    @Test
    public void optipushUserRegistrarCreateShouldntOptOrOutIfThereIsNoToken() {
        when(requirementProvider.notificaionsAreEnabled()).thenReturn(true);
        when(registrationDao.wasTheUserOptIn()).thenReturn(false);
        when(registrationDao.getLastToken()).thenReturn(null);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider,
                lifecycleObserver);

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
        when(registrationDao.isSetUserMarkedAsFailed()).thenReturn(true);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo,
                installationIDProvider, lifecycleObserver);

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
    public void optipushUserRegistrarCreateShouldAddPreviousFailedAliases() {
        String userAlias = "some_user_alias";
        Set<String> userAliases = new HashSet<>();
        userAliases.add(userAlias);
        when(registrationDao.getFailedUserAliases()).thenReturn(userAliases);

        OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);

        verify(httpClient).putJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonAliasesHasAlias(arg, userAlias)))));
    }

    private boolean jsonAliasesHasAlias(JSONObject jsonObject, String alias) {
        try {
            JSONArray newAliasesJson = jsonObject.getJSONArray("new_aliases");
            for (int i = 0; i < newAliasesJson.length(); i++) {
                if (newAliasesJson.get(i)
                        .equals(alias)) {
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            return false;
        }
    }

    @Test
    public void userTokenChangedShouldSendTheNewToken() {

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);

        optipushUserRegistrar.userTokenChanged();

        verify(httpClient).postJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonTokenMatchToken(arg, token)))));
    }

    @Test
    public void userIdChangedShouldSendTheCurrentUserIdIfNoFailedAliases() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);

        optipushUserRegistrar.userIdChanged(visitorId, userId);

        verify(httpClient).putJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonAliasesHasAlias(arg, userId)))));
    }

    @Test
    public void userIdChangedShouldSendThePreviousFailedAliasIfThereIsOne() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";
        String userAlias = "some_user_alias";
        Set<String> userAliases = new HashSet<>();
        userAliases.add(userAlias);

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);

        when(registrationDao.getFailedUserAliases()).thenReturn(userAliases);
        optipushUserRegistrar.userIdChanged(visitorId, userId);

        verify(httpClient).putJsonWithoutJsonResponse((assertArg(arg -> Assert.assertEquals(arg, registrationEndPoint))),
                (assertArg(arg -> Assert.assertTrue(jsonAliasesHasAlias(arg, userAlias)))));
    }

    @Test
    public void userTokenChangedShouldSetSetUserFailedIfHttpError() {
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return requestBuilder;
        }).when(requestBuilder)
                .errorListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);
        optipushUserRegistrar.userTokenChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .markSetUserAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }

    @Test
    public void userTokenChangedShouldUnmarkTokenUpdateAsFailedIfHttpSuccess() {
        doAnswer(invocation -> {
            Response.Listener<JSONObject> successListener =
                    (Response.Listener<JSONObject>) invocation.getArguments()[0];
            successListener.onResponse(null);
            return requestBuilder;
        }).when(requestBuilder)
                .successListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);
        optipushUserRegistrar.userTokenChanged();

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .unmarkSetUserAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }
    @Test
    public void userChangedShouldSetFailedAliasIfHttpError() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return requestBuilder;
        }).when(requestBuilder)
                .errorListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);
        optipushUserRegistrar.userIdChanged(visitorId, userId);

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .markAddUserAliasesAsFailed(assertArg(arg -> Assert.assertTrue(arg.contains(userId))));
        inOrder.verify(flagsEditor)
                .save();

    }
    @Test
    public void userChangedShouldRemoveFailedAliasIfHttpSuccess() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";
        doAnswer(invocation -> {
            Response.Listener<JSONObject> successListener =
                    (Response.Listener<JSONObject>) invocation.getArguments()[0];
            successListener.onResponse(null);
            return requestBuilder;
        }).when(requestBuilder)
                .successListener(any());

        OptipushUserRegistrar optipushUserRegistrar = OptipushUserRegistrar.create(registrationEndPoint, httpClient,
                packageName, tenantId, requirementProvider, registrationDao, userInfo, installationIDProvider, lifecycleObserver);
        optipushUserRegistrar.userIdChanged(visitorId, userId);

        InOrder inOrder = Mockito.inOrder(flagsEditor);

        inOrder.verify(flagsEditor)
                .unmarkAddUserAliaseAsFailed();
        inOrder.verify(flagsEditor)
                .save();

    }
}


