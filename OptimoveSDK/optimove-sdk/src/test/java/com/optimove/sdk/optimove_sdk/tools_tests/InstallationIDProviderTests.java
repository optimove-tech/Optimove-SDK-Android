package com.optimove.sdk.optimove_sdk.tools_tests;

import android.content.Context;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider.INSTALLATION_ID_KEY;
import static com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider.INSTALLATION_SP_FILE;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.DEVICE_ID_KEY;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.REGISTRATION_PREFERENCES_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class InstallationIDProviderTests {

    @Mock
    private Context context;
    @Mock
    private SharedPreferences installationSharedPrefs;
    @Mock
    private SharedPreferences registrationPreferences;

    private InstallationIDProvider installationIDProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(context.getSharedPreferences(INSTALLATION_SP_FILE, Context.MODE_PRIVATE)).thenReturn(installationSharedPrefs);
        when(context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)).thenReturn(registrationPreferences);

        installationIDProvider = new InstallationIDProvider(context);
    }

    @Test
    public void shouldReturnInstallationSPInstallationIdIfExists() {
        String installationId = "some_installation_id1";
        when(installationSharedPrefs.getString(INSTALLATION_ID_KEY, null)).thenReturn(installationId);

        Assert.assertEquals(installationIDProvider.getInstallationID(), installationId);
    }
    @Test
    public void shouldReturnOptipushSPInstallationIdIfExists() {
        String installationId = "some_installation_id2";
        when(registrationPreferences.getString(DEVICE_ID_KEY, null)).thenReturn(installationId);

        Assert.assertEquals(installationIDProvider.getInstallationID(), installationId);
    }
}
