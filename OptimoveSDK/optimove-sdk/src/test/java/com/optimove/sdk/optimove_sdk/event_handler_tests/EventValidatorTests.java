package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_BOOLEAN_TYPE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_STRING_TYPE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH;
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

    //1010
    @Test
    public void eventShouldContainValidationIssueIfNotExistedInConfigFile() {
        String eventName = "some_name";
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());

        when(eventConfigsMap.containsKey(eventName)).thenReturn(false);

        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                        .getValidationIssues()
                        .get(0)
                        .getStatus()
                , 1010)));
    }

    //1030
    @Test
    public void eventShouldContainValidationIssueIfContainsANonExistingParam() {
        String eventName = "some_name";
        HashMap<String, Object> eventParams = new HashMap<>();
        eventParams.put("some_non_existing_param", "some_value");
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);

        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();

        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);


        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues(),
                1030))));
    }

    //1040
    @Test
    public void eventShouldContainValidationIssueIfMissingMandatoryParam() {
        String eventName = "some_name";
        HashMap<String, Object> eventParams = new HashMap<>();
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);

        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig mandatoryParamConfig = mock(EventConfigs.ParameterConfig.class);
        parameterConfigMap.put("some_mandatory_event_param", mandatoryParamConfig);
        when(mandatoryParamConfig.isOptional()).thenReturn(false);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);


        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                .getValidationIssues(), 1040))));
    }

    //1050
    @Test
    public void eventShouldContainValidationIssueIfParamValueExceededTheMaximumAllowance() {
        String eventName = "some_name";
        HashMap<String, Object> eventParams = new HashMap<>();
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < PARAMETER_VALUE_MAX_LENGTH + 1; i++) {
            longValue.append("d");
        }
        eventParams.put("some_param", longValue);
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);

        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig mandatoryParamConfig = mock(EventConfigs.ParameterConfig.class);
        when(mandatoryParamConfig.getType()).thenReturn(PARAMETER_STRING_TYPE);
        parameterConfigMap.put("some_param", mandatoryParamConfig);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues()
                , 1050))));
    }
    //1060
    @Test
    public void eventShouldContainValidationIssueIfParamValueTypeDoesntFitTheConfig() {
        String eventName = "some_name";
        HashMap<String, Object> eventParams = new HashMap<>();
        eventParams.put("some_param", "dfgdfgsdgdf");
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);

        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig mandatoryParamConfig = mock(EventConfigs.ParameterConfig.class);
        when(mandatoryParamConfig.getType()).thenReturn(PARAMETER_BOOLEAN_TYPE);
        parameterConfigMap.put("some_param", mandatoryParamConfig);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues()
                , 1060))));
    }

    private boolean validationsContainStatus(List<OptimoveEvent.ValidationIssue> validationIssues, int desiredStatus) {
        if (validationIssues == null) {
            return false;
        }
        for (OptimoveEvent.ValidationIssue validationIssue : validationIssues) {
            if (validationIssue.getStatus() == desiredStatus) {
                return true;
            }
        }
        return false;
    }


    @Test
    public void eventShouldBeReportedToNext() {
        String eventName = "some_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();

        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());

        when(eventConfigsMap.containsKey(eventName)).thenReturn(true);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);

        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(Collections.singletonList(optimoveEvent));
    }
}
