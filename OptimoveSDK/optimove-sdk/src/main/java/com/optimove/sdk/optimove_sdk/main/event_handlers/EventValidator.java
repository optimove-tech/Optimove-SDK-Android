package com.optimove.sdk.optimove_sdk.main.event_handlers;

import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
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
    public enum ValidationIssueCode {
        EVENT_MISSING(1010),
        PARAM_DOESNT_APPEAR_IN_CONFIG(1030),
        MANDATORY_PARAM_MISSING(1040),
        PARAM_VALUE_TOO_LONG(1050),
        PARAM_VALUE_TYPE_INCORRECT(1060),
        USER_ID_TOO_LONG(1071),
        EMAIL_IS_INVALID(1080);

        public int rawValue;

        ValidationIssueCode(int rawValue) {
            this.rawValue = rawValue;
        }
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> getValidationIssuesIfAny(OptimoveEvent optimoveEvent) {
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();

        String eventName = optimoveEvent.getName();

        EventConfigs eventConfig = eventConfigsMap.get(eventName);

        if (eventConfig == null) {
            String message = eventName + " is an undefined event";
            OptiLoggerStreamsContainer.error(message);
            validationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.EVENT_MISSING.rawValue, message));
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
        OptimoveEvent.ValidationIssue userEmailValidationIssue = verifyEmailIfSetEmail(optimoveEvent);
        if (userEmailValidationIssue != null) {
            validationIssues.add(userEmailValidationIssue);
        }

        if (!validationIssues.isEmpty()) {
            return validationIssues;
        } else {
            return null;
        }
    }

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
                missingMandatoryParams.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.MANDATORY_PARAM_MISSING.rawValue, message));
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
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_DOESNT_APPEAR_IN_CONFIG.rawValue, message));
                continue;
            }
            Object value = parameters.get(key);
            if (value == null) {
                continue;
            }
            if (isIncorrectParameterValueType(parameterConfig, value)) {
                String message = key + " should be of type " + parameterConfig.getType();
                OptiLoggerStreamsContainer.error(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_VALUE_TYPE_INCORRECT.rawValue, message));
            }
            if (isValueTooLarge(value)) {
                String message = String.format("%s has exceeded the limit of allowed number of characters. The " +
                        "character limit is %s", key, OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH);
                OptiLoggerStreamsContainer.error(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_VALUE_TOO_LONG.rawValue,
                        message));
            }
        }

        if (!paramValidationIssues.isEmpty()) {
            return paramValidationIssues;
        } else {
            return null;
        }
    }

    @Nullable
    private OptimoveEvent.ValidationIssue verifyUserIdIfSetUserId(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME) && (optimoveEvent instanceof SetUserIdEvent)
                && ((SetUserIdEvent) optimoveEvent).getUserId() != null && ((SetUserIdEvent) optimoveEvent).getUserId()
                .length() > USER_ID_MAX_LENGTH) {
            String message = String.format("userId, %s, is too " +
                    "long, the userId limit is %s", ((SetUserIdEvent) optimoveEvent).getUserId(), USER_ID_MAX_LENGTH);
            OptiLoggerStreamsContainer.error(message);
            return new OptimoveEvent.ValidationIssue(ValidationIssueCode.USER_ID_TOO_LONG.rawValue, message);
        }
        return null;
    }

    @Nullable
    private OptimoveEvent.ValidationIssue verifyEmailIfSetEmail(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getName()
                .equals(SetEmailEvent.EVENT_NAME) && (optimoveEvent instanceof SetEmailEvent)
                && ((SetEmailEvent) optimoveEvent).getEmail() != null && !OptiUtils.isValidEmailAddress(((SetEmailEvent) optimoveEvent).getEmail())) {
            String message = String.format("Email, %s, is invalid", ((SetEmailEvent) optimoveEvent).getEmail());
            OptiLoggerStreamsContainer.error(message);
            return new OptimoveEvent.ValidationIssue(ValidationIssueCode.EMAIL_IS_INVALID.rawValue, message);
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
