package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
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
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_NUMBER_TYPE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_STRING_TYPE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.USER_ID_MAX_LENGTH;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventValidatorTests {
    @Mock
    private EventHandler nextEventHandler;
    @Mock
    private Map<String, EventConfigs> eventConfigsMap;

    private EventValidator eventValidator;

    private int maxNumberOfParams = 10;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        eventValidator = new EventValidator(eventConfigsMap, maxNumberOfParams);
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
                , EventValidator.ValidationIssueCode.EVENT_MISSING.rawValue)));
    }

    //1020
    @Test
    public void eventWithTooManyParamsShouldBeTruncatedWithValidationIssues() {
        String eventName = "some_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);

        //add many random configured params
        HashMap<String, Object> eventParams = new HashMap<>();
        for (int i = 0; i < eventValidator.getMaxNumberOfParams() + 6; i ++) {
            String randomKey = String.valueOf(Math.random() * Math.random());
            double randomValue = Math.random();
            eventParams.put(randomKey, randomValue);
            EventConfigs.ParameterConfig parameterConfig = mock(EventConfigs.ParameterConfig.class);
            when(parameterConfig.getType()).thenReturn(PARAMETER_NUMBER_TYPE);
            parameterConfigMap.put(randomKey, parameterConfig);
        }



        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, eventParams);


        eventValidator.reportEvent(Collections.singletonList(optimoveEvent));
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues(),
                EventValidator.ValidationIssueCode.TOO_MANY_PARAMS.rawValue) && (arg.get(0)
                .getParameters()
                .size() == eventValidator.getMaxNumberOfParams()))));
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
                EventValidator.ValidationIssueCode.PARAM_DOESNT_APPEAR_IN_CONFIG.rawValue))));
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
                .getValidationIssues(), EventValidator.ValidationIssueCode.MANDATORY_PARAM_MISSING.rawValue))));
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
                , EventValidator.ValidationIssueCode.PARAM_VALUE_TOO_LONG.rawValue))));
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
                , EventValidator.ValidationIssueCode.PARAM_VALUE_TYPE_INCORRECT.rawValue))));
    }

    //1071
    @Test
    public void eventShouldContainValidationIssueIfsetUserIdAndUserIdIsTooLong() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < USER_ID_MAX_LENGTH + 1; i++) {
            longUserId.append("d");
        }

        EventConfigs eventConfigs = mock(EventConfigs.class);

        when(eventConfigsMap.get(SetUserIdEvent.EVENT_NAME)).thenReturn(eventConfigs);

        SetUserIdEvent setUserIdEvent =
                new SetUserIdEvent("some_visitor_id", longUserId.toString(), "some_updated_visitor_id");

        eventValidator.reportEvent(Collections.singletonList(setUserIdEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues()
                , EventValidator.ValidationIssueCode.USER_ID_TOO_LONG.rawValue))));
    }

    @Test
    public void eventShouldtContainValidationIssueIfsetUserIdValid() {
        EventConfigs eventConfigs = mock(EventConfigs.class);

        when(eventConfigsMap.get(SetUserIdEvent.EVENT_NAME)).thenReturn(eventConfigs);

        SetUserIdEvent setUserIdEvent =
                new SetUserIdEvent("some_visitor_id", "some_user_id", "some_updated_visitor_id");

        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig mandatoryParamConfig = mock(EventConfigs.ParameterConfig.class);
        when(mandatoryParamConfig.getType()).thenReturn(PARAMETER_STRING_TYPE);
        parameterConfigMap.put(SetUserIdEvent.USER_ID_PARAM_KEY, mandatoryParamConfig);
        parameterConfigMap.put(SetUserIdEvent.ORIGINAL_VISITOR_ID_PARAM_KEY, mandatoryParamConfig);
        parameterConfigMap.put(SetUserIdEvent.UPDATED_VISITOR_ID_PARAM_KEY, mandatoryParamConfig);

        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);
        eventValidator.reportEvent(Collections.singletonList(setUserIdEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                        .getValidationIssues()
                , null)));
    }

    //1080
    @Test
    public void eventShouldContainValidationIssueIfsetUserEmailAndEmailInvalid() {

        EventConfigs eventConfigs = mock(EventConfigs.class);

        when(eventConfigsMap.get(SetEmailEvent.EVENT_NAME)).thenReturn(eventConfigs);

        SetEmailEvent setEmailEvent = new SetEmailEvent("dfgdgsdfg");
        eventValidator.reportEvent(Collections.singletonList(setEmailEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertTrue(validationsContainStatus(arg.get(0)
                        .getValidationIssues()
                , EventValidator.ValidationIssueCode.EMAIL_IS_INVALID.rawValue))));
    }

    @Test
    public void eventShouldntContainValidationIssueIfsetUserEmailAndEmailValid() {

        EventConfigs eventConfigs = mock(EventConfigs.class);

        when(eventConfigsMap.get(SetEmailEvent.EVENT_NAME)).thenReturn(eventConfigs);

        SetEmailEvent setEmailEvent = new SetEmailEvent("dfgdfg@gmail.com");

        Map<String, EventConfigs.ParameterConfig> parameterConfigMap = new HashMap<>();
        EventConfigs.ParameterConfig mandatoryParamConfig = mock(EventConfigs.ParameterConfig.class);
        when(mandatoryParamConfig.getType()).thenReturn(PARAMETER_STRING_TYPE);
        parameterConfigMap.put(SetEmailEvent.EMAIL_PARAM_KEY, mandatoryParamConfig);
        when(eventConfigs.getParameterConfigs()).thenReturn(parameterConfigMap);
        eventValidator.reportEvent(Collections.singletonList(setEmailEvent));

        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.get(0)
                        .getValidationIssues()
                , null)));
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
