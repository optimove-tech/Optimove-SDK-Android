package com.optimove.sdk.optimove_sdk.event_generators_tests;

import android.app.Activity;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.common.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.common.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.common.UserInfo;
import com.optimove.sdk.optimove_sdk.main.event_generators.OptimoveLifecycleEventGenerator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.AppOpenEvent;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OptimoveLifecycleEventGeneratorTests {
    @Mock
    private EventHandlerProvider eventHandlerProvider;
    @Mock
    private UserInfo userInfo;
    @Mock
    private EventHandler eventHandler;

    @Mock
    private SharedPreferences optitrackPreferences;
    @Mock
    SharedPreferences.Editor editor;

    @Mock
    private DeviceInfoProvider deviceInfoProvider;


    private OptimoveLifecycleEventGenerator optimoveLifecycleEventGenerator;
    private LifecycleObserver lifecycleObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        lifecycleObserver = new LifecycleObserver();
        when(eventHandlerProvider.getEventHandler()).thenReturn(eventHandler);

        //shared prefs
        when(optitrackPreferences.edit()).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), any())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);

        optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                "some_package_name");
    }
    @Test
    public void appOpenShouldBeReportedWhenActivityFirstStarts() {
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        verify(eventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                .getName(), AppOpenEvent.EVENT_NAME)));
    }

    @Test
    public void appOpenShouldBeReportedOnceIfActivityStartsTwoTimesInRow() {
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        verify(eventHandler,times(1)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                .getName(), AppOpenEvent.EVENT_NAME)));
    }
}
