package com.optimove.sdk.optimove_sdk.event_generators_tests;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.event_generators.EventGenerator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetAdvertisingIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_OPT_REPORTED_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_IN;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EventGeneratorTests {


    @Mock
    SharedPreferences optitrackPreferences;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    UserInfo userInfo;


    private String packageName = "package_name";
    private String encryptedDeviceId = "some_encrypted_device_id";
    private String location = "some_location";
    @Mock
    private DeviceInfoProvider deviceInfoProvider;
    @Mock
    private TenantInfo tenantInfo;
    @Mock
    private EventHandlerProvider eventHandlerProvider;
    @Mock
    private EventHandler eventHandler;
    @Mock
    private Context context;


    private EventGenerator eventGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        //shared prefs
        when(optitrackPreferences.edit()).thenReturn(editor);
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(-1);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), any())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);

        when(userInfo.getAdvertisingId()).thenReturn("asdgsdfg");

        when(eventHandlerProvider.getEventHandler()).thenReturn(eventHandler);

        Location location = mock(Location.class);
        when(deviceInfoProvider.getDeviceLocation(context)).thenReturn(location);
        when(deviceInfoProvider.getDeviceLanguage()).thenReturn("some_device_lang");


        eventGenerator =
                EventGenerator.builder()
                        .withUserInfo(userInfo)
                        .withPackageName(packageName)
                        .withDeviceId(encryptedDeviceId)
                        .withRequirementProvider(deviceInfoProvider)
                        .withTenantInfo(tenantInfo)
                        .withEventHandlerProvider(eventHandlerProvider)
                        .withContext(context)
                        .build();
    }

    @Test
    public void userAgentEventShouldBeSentWhenInitialized() {
        eventGenerator.generateStartEvents(false);
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), UserAgentHeaderEvent.EVENT_NAME)));
    }
    @Test
    public void sdkMetadataEventShouldBeSentWhenInitialized() {
        eventGenerator.generateStartEvents(false);
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), SdkMetadataEvent.EVENT_NAME)));
    }

    @Test
    public void adIdShouldBeReportedIfAdIdAllowedAndCanReportAdAndAdvertisingIdIsNotNull() {
        when(deviceInfoProvider.canReportAdId()).thenReturn(true);
        when(userInfo.getAdvertisingId()).thenReturn("sdfgdsf");
        eventGenerator.generateStartEvents(true);
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), SetAdvertisingIdEvent.EVENT_NAME)));
    }
    @Test
    public void adIdShouldntBeReportedIfAdvertisingIdIsntAllowed() {
        eventGenerator.generateStartEvents(false);
        verify(eventHandler, times(0)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), SetAdvertisingIdEvent.EVENT_NAME)));
    }
    @Test
    public void adIdShouldntBeReportedIfCantReportAdId() {
        when(deviceInfoProvider.canReportAdId()).thenReturn(false);
        eventGenerator.generateStartEvents(true);
        verify(eventHandler, times(0)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), SetAdvertisingIdEvent.EVENT_NAME)));
    }
    @Test
    public void adIdShouldntBeReportedIfAdvertisingIdIsNull() {
        when(deviceInfoProvider.canReportAdId()).thenReturn(true);
        when(userInfo.getAdvertisingId()).thenReturn(null);
        eventGenerator.generateStartEvents(true);
        verify(eventHandler, times(0)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), SetAdvertisingIdEvent.EVENT_NAME)));
    }
    @Test
    public void neitherOptInNorOptOutShouldBeSentWhenWasOptinAndCurrentlyOptIn() {
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(LAST_REPORTED_OPT_IN);
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        eventGenerator.generateStartEvents(false);
        verifyZeroInteractions(editor);
    }
}
