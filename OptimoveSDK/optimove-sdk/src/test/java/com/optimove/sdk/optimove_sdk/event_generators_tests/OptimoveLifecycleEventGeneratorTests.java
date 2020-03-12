package com.optimove.sdk.optimove_sdk.event_generators_tests;

import android.app.Activity;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.event_generators.OptimoveLifecycleEventGenerator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.AppOpenEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptIn;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptOut;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_OPT_REPORTED_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_IN;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_OUT;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
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
        MockitoAnnotations.initMocks(this);

        lifecycleObserver = new LifecycleObserver();
        when(eventHandlerProvider.getEventHandler()).thenReturn(eventHandler);

        //shared prefs
        when(optitrackPreferences.edit()).thenReturn(editor);
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(-1);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), any())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);

        optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                "some_package_name", optitrackPreferences, deviceInfoProvider);
    }
    @Test
    public void appOpenShouldBeReportedWhenActivityFirstStarts() {
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        verify(eventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), AppOpenEvent.EVENT_NAME)));
    }

    @Test
    public void appOpenShouldBeReportedOnceIfActivityStartsTwoTimesInRow() {
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        lifecycleObserver.onActivityStarted(mock(Activity.class));
        verify(eventHandler,times(1)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), AppOpenEvent.EVENT_NAME)));
    }

    @Test
    public void optInShouldBeSentWhenWasntOptinAndCurrentlyIs() {
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(true);
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(LAST_REPORTED_OPT_OUT);
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));

        verify(eventHandler, timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), OptipushOptIn.EVENT_NAME)));
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertTrue(arg.getExecutionTimeout() > 0)));

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor)
                .putInt(LAST_OPT_REPORTED_KEY, LAST_REPORTED_OPT_IN);
        inOrder.verify(editor)
                .apply();
    }

    @Test
    public void optOutShouldBeSentWhenWasOptinAndCurrentlyIsnt() {
        when(deviceInfoProvider.notificaionsAreEnabled()).thenReturn(false);
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(LAST_REPORTED_OPT_IN);
        lifecycleObserver.addActivityStartedListener(optimoveLifecycleEventGenerator);
        lifecycleObserver.onActivityStarted(mock(Activity.class));

        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertEquals(arg.getOptimoveEvent()
                .getName(), OptipushOptOut.EVENT_NAME)));
        verify(eventHandler,timeout(300)).reportEvent(assertArg(arg -> Assert.assertTrue(arg.getExecutionTimeout() > 0)));

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor)
                .putInt(LAST_OPT_REPORTED_KEY, LAST_REPORTED_OPT_OUT);
        inOrder.verify(editor)
                .apply();
    }
}
