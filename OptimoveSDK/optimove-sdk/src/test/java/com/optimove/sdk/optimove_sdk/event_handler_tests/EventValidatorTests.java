package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_NATIVE_MOBILE_PARAM_KEY;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EventValidatorTests {
    @Mock
    private EventHandler nextEventHandler;
    @Mock
    private Map<String, EventConfigs> eventConfigsMap;

    private EventValidator eventValidator;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        eventValidator = new EventValidator(eventConfigsMap);
        eventValidator.setNext(nextEventHandler);
    }

    @Test
    public void eventShouldNotBeReportedIfDoesntExistInConfigs() {
        String eventName = "some_name";
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);

        when(eventConfigsMap.containsKey(eventName)).thenReturn(false);

        eventValidator.reportEvent(eventContext);
        verifyZeroInteractions(nextEventHandler);
    }

    @Test
    public void eventShouldNotBeReportedIfNotValid() {
        String eventName = "some_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig parameterConfig = mock(EventConfigs.ParameterConfig.class);
        parameterConfigMap.put("some_parameter",parameterConfig);
        when(parameterConfig.isOptional()).thenReturn(false);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);

        when(eventConfigsMap.containsKey(eventName)).thenReturn(true);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);

        eventValidator.reportEvent(eventContext);
        verifyZeroInteractions(nextEventHandler);
    }

    @Test
    public void eventShouldBeReportedToNextIfValid() {
        String eventName = "some_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();

        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);

        when(eventConfigsMap.containsKey(eventName)).thenReturn(true);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);

        eventValidator.reportEvent(eventContext);
        verify(nextEventHandler).reportEvent(eventContext);
    }
}
