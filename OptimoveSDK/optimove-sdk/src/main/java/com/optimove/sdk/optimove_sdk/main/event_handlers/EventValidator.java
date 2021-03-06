package com.optimove.sdk.optimove_sdk.main.event_handlers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.USER_ID_MAX_LENGTH;

public class EventValidator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;
    private int maxNumberOfParams;

    public EventValidator(Map<String, EventConfigs> eventConfigsMap, int maxNumberOfParams) {
        this.eventConfigsMap = eventConfigsMap;
        this.maxNumberOfParams = maxNumberOfParams;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        // mutate each event that has validation issues
        List<OptimoveEvent> optimoveEventsToReport = new ArrayList<>();
        for (OptimoveEvent optimoveEvent : optimoveEvents) {
            List<OptimoveEvent.ValidationIssue> validationIssues = getValidationIssuesIfAny(optimoveEvent);
            if (validationIssues != null) {
                optimoveEvent.setValidationIssues(validationIssues);
            }

            optimoveEventsToReport.add(removeParamsIfTooMany(optimoveEvent));
        }

        reportEventNext(optimoveEventsToReport);
    }

    public enum ValidationIssueCode {
        EVENT_MISSING(1010, true),
        TOO_MANY_PARAMS(1020, false),
        PARAM_DOESNT_APPEAR_IN_CONFIG(1030, false),
        MANDATORY_PARAM_MISSING(1040, true),
        PARAM_VALUE_TOO_LONG(1050, true),
        PARAM_VALUE_TYPE_INCORRECT(1060, true),
        USER_ID_TOO_LONG(1071, true),
        EMAIL_IS_INVALID(1080, true);

        public int rawValue;
        public boolean isError;

        ValidationIssueCode(int rawValue, boolean isError) {
            this.rawValue = rawValue;
            this.isError = isError;
        }

    }

    public int getMaxNumberOfParams() {
        return maxNumberOfParams;
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> getValidationIssuesIfAny(OptimoveEvent optimoveEvent) {
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();

        String eventName = optimoveEvent.getName();

        EventConfigs eventConfig = eventConfigsMap.get(eventName);

        if (eventConfig == null) {
            String message = eventName + " is an undefined event";
            OptiLoggerStreamsContainer.businessLogicError(message);
            validationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.EVENT_MISSING.rawValue,
                    message, ValidationIssueCode.EVENT_MISSING.isError));
            return validationIssues;
        }

        List<OptimoveEvent.ValidationIssue> missingMandatoryParams =
                checkIfAllMandatoryParamsExist(optimoveEvent, eventConfig, optimoveEvent.getParameters());
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
        }
        return null;
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> checkIfAllMandatoryParamsExist(OptimoveEvent optimoveEvent,
                                                                               EventConfigs eventConfig,
                                                                               @Nullable  Map<String, Object> parameters) {
        List<OptimoveEvent.ValidationIssue> missingMandatoryParams = new ArrayList<>();

        for (String paramConfigKey : eventConfig.getParameterConfigs()
                .keySet()) {
            EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs()
                    .get(paramConfigKey);
            if (parameterConfig == null) {
                continue;
            }

            if(!parameterConfig.isOptional() && (parameters == null || !parameters.containsKey(paramConfigKey))) {
                String message = String.format("event %s has a mandatory parameter, %s, which is undefined or empty",
                        optimoveEvent.getName(), paramConfigKey);
                OptiLoggerStreamsContainer.businessLogicError(message);
                missingMandatoryParams.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.MANDATORY_PARAM_MISSING.rawValue, message, ValidationIssueCode.MANDATORY_PARAM_MISSING.isError));
            }
        }
        if (!missingMandatoryParams.isEmpty()) {
            return missingMandatoryParams;
        }
        return null;
    }

    @Nullable
    private List<OptimoveEvent.ValidationIssue> findValidationIssuesInsideParams(EventConfigs eventConfig,
                                                                                 @Nullable Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }

        List<OptimoveEvent.ValidationIssue> paramValidationIssues = new ArrayList<>();

        for (String key : parameters.keySet()) {
            EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs()
                    .get(key);
            if (parameterConfig == null) {
                String message = String.format("parameter %s has not been configured for this event. It " +
                        "will not be tracked and cannot be used within a trigger.", key);
                OptiLoggerStreamsContainer.warn(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_DOESNT_APPEAR_IN_CONFIG.rawValue, message, ValidationIssueCode.PARAM_DOESNT_APPEAR_IN_CONFIG.isError));
                continue;
            }
            Object value = parameters.get(key);
            if (value == null) {
                continue;
            }
            if (isIncorrectParameterValueType(parameterConfig, value)) {
                String message = String.format("%s should be of type %s", key, parameterConfig.getType());
                OptiLoggerStreamsContainer.businessLogicError(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_VALUE_TYPE_INCORRECT.rawValue, message, ValidationIssueCode.PARAM_VALUE_TYPE_INCORRECT.isError));
            }
            if (isValueTooLarge(value)) {
                String message = String.format("%s has exceeded the limit of allowed number of characters. The " +
                        "character limit is %s", key, OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH);
                OptiLoggerStreamsContainer.businessLogicError(message);
                paramValidationIssues.add(new OptimoveEvent.ValidationIssue(ValidationIssueCode.PARAM_VALUE_TOO_LONG.rawValue,
                        message, ValidationIssueCode.PARAM_VALUE_TOO_LONG.isError));
            }
        }

        if (!paramValidationIssues.isEmpty()) {
            return paramValidationIssues;
        }
        return null;
    }


    @Nullable
    private OptimoveEvent.ValidationIssue verifyUserIdIfSetUserId(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME) && (optimoveEvent instanceof SetUserIdEvent)
                && ((SetUserIdEvent) optimoveEvent).getUserId() != null && ((SetUserIdEvent) optimoveEvent).getUserId()
                .length() > USER_ID_MAX_LENGTH) {
            String message = String.format("userId, %s, is too " +
                    "long, the userId limit is %s", ((SetUserIdEvent) optimoveEvent).getUserId(), USER_ID_MAX_LENGTH);
            OptiLoggerStreamsContainer.businessLogicError(message);
            return new OptimoveEvent.ValidationIssue(ValidationIssueCode.USER_ID_TOO_LONG.rawValue, message,
                    ValidationIssueCode.USER_ID_TOO_LONG.isError);
        }
        return null;
    }

    @Nullable
    private OptimoveEvent.ValidationIssue verifyEmailIfSetEmail(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getName()
                .equals(SetEmailEvent.EVENT_NAME) && (optimoveEvent instanceof SetEmailEvent)
                && ((SetEmailEvent) optimoveEvent).getEmail() != null && !OptiUtils.isValidEmailAddress(((SetEmailEvent) optimoveEvent).getEmail())) {
            String message = String.format("Email, %s, is invalid", ((SetEmailEvent) optimoveEvent).getEmail());
            OptiLoggerStreamsContainer.businessLogicError(message);
            return new OptimoveEvent.ValidationIssue(ValidationIssueCode.EMAIL_IS_INVALID.rawValue, message,
                    ValidationIssueCode.EMAIL_IS_INVALID.isError);
        }
        return null;
    }

    @NonNull
    private OptimoveEvent removeParamsIfTooMany(OptimoveEvent optimoveEvent) {
        if (optimoveEvent.getParameters() == null || optimoveEvent.getParameters().size() <= maxNumberOfParams) {
            return optimoveEvent;
        }
        String message = String.format("event %s contains %s parameters while the allowed number of parameters " +
                        "is %s. Some parameters were removed to process the event.",
                optimoveEvent.getName(), optimoveEvent.getParameters()
                        .size(), maxNumberOfParams);
        OptiLoggerStreamsContainer.warn(message);
        int count = 0;
        Map<String, Object> newParams = new HashMap<>();
        for (String paramKey : optimoveEvent.getParameters()
                .keySet()) {
            if (count < maxNumberOfParams) {
                newParams.put(paramKey, optimoveEvent.getParameters()
                        .get(paramKey));
            }
            count++;
        }
        OptimoveEvent truncatedParamsSimpleEvent = new SimpleCustomEvent(optimoveEvent.getName(), newParams);
        OptimoveEvent.ValidationIssue tooManyParamsValidationIssue =
                new OptimoveEvent.ValidationIssue(ValidationIssueCode.TOO_MANY_PARAMS.rawValue, message,
                        ValidationIssueCode.TOO_MANY_PARAMS.isError);
        List<OptimoveEvent.ValidationIssue> newArrayOfValidationIssues = new ArrayList<>();
        if (optimoveEvent.getValidationIssues() != null) {
            newArrayOfValidationIssues.addAll(optimoveEvent.getValidationIssues());
        }
        newArrayOfValidationIssues.add(tooManyParamsValidationIssue);
        truncatedParamsSimpleEvent.setValidationIssues(newArrayOfValidationIssues);
        return truncatedParamsSimpleEvent;
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
