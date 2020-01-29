package com.optimove.sdk.optimove_sdk.event_generators_tests;

import android.app.Activity;

import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.event_generators.OptimoveLifecycleEventGenerator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.core_events.AppOpenEvent;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
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

    private OptimoveLifecycleEventGenerator optimoveLifecycleEventGenerator;
    private LifecycleObserver lifecycleObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        lifecycleObserver = new LifecycleObserver();
        when(eventHandlerProvider.getEventHandler()).thenReturn(eventHandler);

        optimoveLifecycleEventGenerator = new OptimoveLifecycleEventGenerator(eventHandlerProvider, userInfo,
                "some_package_name", mock(InstallationIDProvider.class));
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
}
