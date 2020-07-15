package com.optimove.sdk.optimove_sdk.main.event_handlers;

import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.USER_ID_MAX_LENGTH;

public class EventValidator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;

    public EventValidator(Map<String, EventConfigs> eventConfigsMap) {
        this.eventConfigsMap = eventConfigsMap;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        // mutate each event that has validation issues
        for (OptimoveEvent optimoveEvent : optimoveEvents) {
            List<OptimoveEvent.ValidationIssue> validationIssues = getValidationIssuesIfAny(optimoveEvent);
            if (validationIssues != null) {
                optimoveEvent.setValidationIssues(validationIssues);
            }
        }

        reportEventNext(optimoveEvents);
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> getValidationIssuesIfAny(OptimoveEvent optimoveEvent) {
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();

        String eventName = optimoveEvent.getName();

        EventConfigs eventConfig = eventConfigsMap.get(eventName);

        if (eventConfig == null) {
            String message = eventName + " is an undefined event";
            OptiLoggerStreamsContainer.error(message);
            validationIssues.add(new OptimoveEvent.ValidationIssue(1010, message));
            return validationIssues;
        }

        List<OptimoveEvent.ValidationIssue> missingMandatoryParams =
                checkIfAllMandatoryParamsAreExisted(optimoveEvent, eventConfig, optimoveEvent.getParameters());
        if (missingMandatoryParams != null) {
            validationIssues.addAll(missingMandatoryParams);
        }

        List<OptimoveEvent.ValidationIssue> paramsValidationIssues =
                findValidationIssuesInsideParams(eventConfig, optimoveEvent.getParameters());
        if (paramsValidationIssues != null) {
            validationIssues.addAll(paramsValidationIssues);
        }

        OptimoveEvent.ValidationIssue userIdValidationIssue = verifyUserIdIfSetUserId(optimoveEvent);
        if (userIdValidationIssue != null) {
            validationIssues.add(userIdValidationIssue);
        }

        if (!validationIssues.isEmpty()) {
            return validationIssues;
        } else {
            return null;
        }
    }

    //1040
    @Nullable
    private List<OptimoveEvent.ValidationIssue> checkIfAllMandatoryParamsAreExisted(OptimoveEvent optimoveEvent,
                                                                                    EventConfigs eventConfig,
                                                                                    Map<String, Object> parameters) {
        List<OptimoveEvent.ValidationIssue> missingMandatoryParams = new ArrayList<>();

        for (String paramConfigKey : eventConfig.getParameterConfigs()
                .keySet()) {
            EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs()
                    .get(paramConfigKey);
            if (parameterConfig == null) {
                continue; // protection
            }
            if (!parameterConfig.isOptional() && parameters.get(paramConfigKey) == null) {
                String message = String.format("event %s has a mandatory parameter, %s which is undefined or empty",
                        optimoveEvent.getName(), paramConfigKey);
                OptiLoggerStreamsContainer.error(message);
                missingMandatoryParams.add(new OptimoveEvent.ValidationIssue(1040, message));
            }
        }
        if (!missingMandatoryParams.isEmpty()) {
            return missingMandatoryParams;
        } else {
            return null;
        }
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> findValidationIssuesInsideParams(EventConfigs eventConfig,
                                                                                 Map<String, Object> parameters) {
        List<OptimoveEvent.ValidationIssue> paramValidationIssues = new ArrayList<>();

        for (String key : parameters.keySet()) {
            EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs()
                    .get(key);
            if (parameterConfig == null) {
                String message = key + " is an undefined parameter. It will not be tracked and cannot be used " +
                        "within a trigger";
                OptiLoggerStreamsContainer.error(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(1030, message));
                continue;
            }
            Object value = parameters.get(key);
            if (value == null) {
                continue;
            }
            if (isIncorrectParameterValueType(parameterConfig, value)) {
                String message = key + " should be of type " + parameterConfig.getType();
                OptiLoggerStreamsContainer.error(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(1060, message));
            }
            if (isValueTooLarge(value)) {
                String message = String.format("%s has exceeded the limit of allowed number of characters. The " +
                        "character limit is %s", key, OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH);
                OptiLoggerStreamsContainer.error(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(1050, message));
            }
        }

        if (!paramValidationIssues.isEmpty()) {
            return paramValidationIssues;
        } else {
            return null;
        }
    }

    //1071
    @Nullable
    private OptimoveEvent.ValidationIssue verifyUserIdIfSetUserId(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME) && (optimoveEvent instanceof SetUserIdEvent)
                && ((SetUserIdEvent) optimoveEvent).getUserId() != null && ((SetUserIdEvent) optimoveEvent).getUserId()
                .length() > USER_ID_MAX_LENGTH) {
            String message = String.format("userId, %s, is too " +
                    "long, the userId limit is %s", ((SetUserIdEvent) optimoveEvent).getUserId(), USER_ID_MAX_LENGTH);
            OptiLoggerStreamsContainer.error(message);
            return new OptimoveEvent.ValidationIssue(1071, message);
        }
        return null;
    }

    private boolean isIncorrectParameterValueType(EventConfigs.ParameterConfig parameterConfig, Object value) {
        switch (parameterConfig.getType()) {
            case OptitrackConstants.PARAMETER_STRING_TYPE:
                return !(value instanceof String);
            case OptitrackConstants.PARAMETER_NUMBER_TYPE:
                return !(value instanceof Number);
            case OptitrackConstants.PARAMETER_BOOLEAN_TYPE:
                return !(value instanceof Boolean);
            default:
                return true;
        }
    }

    private boolean isValueTooLarge(Object value) {
        return value.toString()
                .length() > OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH;
    }

}
