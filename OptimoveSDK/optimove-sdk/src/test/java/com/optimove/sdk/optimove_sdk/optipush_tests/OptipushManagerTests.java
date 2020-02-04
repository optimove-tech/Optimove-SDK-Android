package com.optimove.sdk.optimove_sdk.optipush_tests;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.optipush.OptipushManager;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OptipushManagerTests {

    @Mock
    private RegistrationDao registrationDao;
    @Mock
    private OptipushUserRegistrar optipushUserRegistrar;
    @Mock
    private RequirementProvider requirementProvider;
    @Mock
    private Context context;
    @Mock
    private RegistrationDao.FlagsEditor flagsEditor;

    private OptipushManager optipushManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        optipushManager = new OptipushManager(optipushUserRegistrar,
                context);
        when(registrationDao.editFlags()).thenReturn(flagsEditor);
        when(flagsEditor.unmarkTokenRefreshAsFailed()).thenReturn(flagsEditor);

    }


    @Test
    public void addRegisiteredUserOnDeviceShouldRegisterNewUser() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";
        optipushManager.addRegisteredUserOnDevice(visitorId, userId);
        verify(optipushUserRegistrar).userIdChanged(visitorId, userId);
    }

    @Test
    public void tokenWasChangedShouldCallOptipushUserRegistrar() {
        optipushManager.tokenWasChanged();
        verify(optipushUserRegistrar).userTokenChanged();
    }
}
