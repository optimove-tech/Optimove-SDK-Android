package com.optimove.android.event_handler_tests;

import com.optimove.android.main.event_handlers.EventHandler;
import com.optimove.android.main.event_handlers.EventNormalizer;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.main.events.SimpleCustomEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.Mockito.verify;

public class EventNormalizerTests {

    @Mock
    private EventHandler nextEventHandler;



    private EventNormalizer eventNormalizer;
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        eventNormalizer = new EventNormalizer(56);
        eventNormalizer.setNext(nextEventHandler);
    }

    @Test
    public void customEventShouldBeNormalized() {
        String unformattedName = "Some event ";
        String formattedName = "some_event";
        String unformattedParameter = "Some parameter key ";
        String formattedParameter = "some_parameter_key";
        Map<String, Object> customEventsParameters = new HashMap<>();
        customEventsParameters.put(unformattedParameter, "some");
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(unformattedName, customEventsParameters);
        eventNormalizer.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                .getName(), formattedName)));
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(arg.get(0)
                .getParameters().containsKey(formattedParameter))));
    }

    @Test
    public void eventWithNoParamsShouldBeReportedNormally() {
        String formattedName = "some_event";
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(formattedName, null);
        eventNormalizer.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                .getName(), formattedName)));
    }
}
