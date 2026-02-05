package com.optimove.android.event_handler_tests;

import com.optimove.android.main.event_handlers.EventDecorator;
import com.optimove.android.main.event_handlers.EventHandler;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.main.events.SimpleCustomEvent;
import com.optimove.android.main.sdk_configs.reused_configs.EventConfigs;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.optimove.android.optistream.OptitrackConstants.EVENT_DEVICE_TYPE_PARAM_KEY;
import static com.optimove.android.optistream.OptitrackConstants.EVENT_NATIVE_MOBILE_PARAM_KEY;
import static com.optimove.android.optistream.OptitrackConstants.EVENT_OS_PARAM_KEY;
import static com.optimove.android.optistream.OptitrackConstants.EVENT_PLATFORM_PARAM_KEY;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventDecoratorTests {

    @Mock
    private EventHandler nextEventHandler;
    @Mock
    private Map<String, EventConfigs> eventConfigsMap;

    private EventDecorator eventDecorator;
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        eventDecorator = new EventDecorator(eventConfigsMap, 4);
        eventDecorator.setNext(nextEventHandler);
    }

    @Test
    public void reportedEventsShouldBeFullyDecoratedIfNumberOfParamsAllows() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();

        parameterConfigMap.put(EVENT_PLATFORM_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_DEVICE_TYPE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_OS_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_NATIVE_MOBILE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));

        when(eventConfigsMap.containsKey(eventName)).thenReturn(true);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);

        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        eventDecorator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_PLATFORM_PARAM_KEY)));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_DEVICE_TYPE_PARAM_KEY)));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_OS_PARAM_KEY)));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_NATIVE_MOBILE_PARAM_KEY)));
    }
    @Test
    public void reportedEventsShouldBePartiallyDecoratedIfNumberOfParamsDoesntAllow() {
        String eventName = "some_event_name";
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("some_param", "some_value");
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.containsKey(eventName)).thenReturn(true);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);

        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();

        parameterConfigMap.put(EVENT_PLATFORM_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_DEVICE_TYPE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_OS_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        parameterConfigMap.put(EVENT_NATIVE_MOBILE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);
        eventDecorator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_PLATFORM_PARAM_KEY)));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_DEVICE_TYPE_PARAM_KEY)));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && arg.get(0).getParameters() != null && arg.get(0).getParameters().containsKey(EVENT_NATIVE_MOBILE_PARAM_KEY)));
        //shouldnt reach the EVENT_OS_PARAM_KEY param
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && (arg.get(0).getParameters() == null || !arg.get(0).getParameters().containsKey(EVENT_OS_PARAM_KEY))));
    }
    @Test
    public void nonConfiguredEventShouldntBeDecorated() {
        String eventName = "some_event_name";
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("some_param", "some_value");

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);
        eventDecorator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && (arg.get(0).getParameters() == null || !arg.get(0).getParameters().containsKey(EVENT_PLATFORM_PARAM_KEY))));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && (arg.get(0).getParameters() == null || !arg.get(0).getParameters().containsKey(EVENT_DEVICE_TYPE_PARAM_KEY))));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && (arg.get(0).getParameters() == null || !arg.get(0).getParameters().containsKey(EVENT_NATIVE_MOBILE_PARAM_KEY))));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && (arg.get(0).getParameters() == null || !arg.get(0).getParameters().containsKey(EVENT_OS_PARAM_KEY))));
    }

    @Test
    public void eventWithNoParamsShouldBeReportedNormally() {
        String eventName = "some_event";
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, null);
        eventDecorator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(argThat(arg -> arg != null && !arg.isEmpty() && eventName.equals(arg.get(0).getName())));
    }
}
