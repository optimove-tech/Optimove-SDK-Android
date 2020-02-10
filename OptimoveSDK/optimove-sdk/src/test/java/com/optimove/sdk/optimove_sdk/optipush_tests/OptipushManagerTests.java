package com.optimove.sdk.optimove_sdk.optipush_tests;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.optipush.OptipushManager;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OptipushManagerTests {

    @Mock
    private RegistrationDao registrationDao;
    @Mock
    private OptipushUserRegistrar optipushUserRegistrar;
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
    public void userIdChangedShouldBeForwardedToUserRegistrar() {
        optipushManager.userIdChanged();
        verify(optipushUserRegistrar).userIdChanged();
    }

    @Test
    public void tokenWasChangedShouldCallOptipushUserRegistrar() {
        optipushManager.tokenWasChanged();
        verify(optipushUserRegistrar).userTokenChanged();
    }
}
