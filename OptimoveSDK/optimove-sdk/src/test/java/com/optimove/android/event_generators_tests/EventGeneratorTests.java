package com.optimove.android.event_generators_tests;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.optimove.android.main.common.EventHandlerProvider;
import com.optimove.android.main.common.TenantInfo;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.event_generators.EventGenerator;
import com.optimove.android.main.event_handlers.EventHandler;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.main.events.core_events.SdkMetadataEvent;
import com.optimove.android.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.android.main.tools.DeviceInfoProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
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
    private String userAgent = "some_user_agent";
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
        MockitoAnnotations.openMocks(this);

        //shared prefs
        when(optitrackPreferences.edit()).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), any())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);

        when(eventHandlerProvider.getEventHandler()).thenReturn(eventHandler);

        Location location = mock(Location.class);
        when(deviceInfoProvider.getDeviceLocation(context)).thenReturn(location);
        when(deviceInfoProvider.getDeviceLanguage()).thenReturn("some_device_lang");


        eventGenerator =
                EventGenerator.builder()
                        .withPackageName(packageName)
                        .withRequirementProvider(deviceInfoProvider)
                        .withTenantInfo(tenantInfo)
                        .withEventHandlerProvider(eventHandlerProvider)
                        .withContext(context)
                        .build();
    }

    @Test
    public void userAgentEventShouldBeSentWhenInitialized() {
        eventGenerator.generateStartEvents();
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertTrue(arrayContains(arg,
                UserAgentHeaderEvent.EVENT_NAME))));
    }
    private boolean arrayContains(List<OptimoveEvent> optimoveEvents, String eventName) {
        for (OptimoveEvent optimoveEvent: optimoveEvents) {
            if (optimoveEvent.getName().equals(eventName)){
                return true;
            }
        }
        return false;
    }

    @Test
    public void sdkMetadataEventShouldBeSentWhenInitialized() {
        eventGenerator.generateStartEvents();
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                .getName(), SdkMetadataEvent.EVENT_NAME)));
    }
}
