package com.optimove.sdk.optimove_sdk.main.events;

import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import java.util.Map;

public class OptimoveEventValidator {

  private OptimoveEvent event;
  @NonNull
  private EventConfigs eventConfig;

  public OptimoveEventValidator(@NonNull OptimoveEvent event, @NonNull EventConfigs eventConfig) {
    this.event = event;
    this.eventConfig = eventConfig;
  }

  public EventValidationResult validateEvent() {
    Map<String, Object> parameters = event.getParameters(); // No need to validate additional params
    if (isMissingMandatoryParameter(eventConfig, parameters)) {
      return EventValidationResult.MISSING_MANDATORY_PARAMETER_ERROR;
    }
    return lookInsideEventParameters(eventConfig, parameters);
  }

  private EventValidationResult lookInsideEventParameters(EventConfigs eventConfig, Map<String, Object> parameters) {
    for (String key : parameters.keySet()) {
      EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs().get(key);
      if (isInvalidParameterName(parameterConfig)) {
        continue;
      }
      Object value = parameters.get(key);
      if (value == null) {
        continue;
      }
      if (isIncorrectParameterValueType(parameterConfig, value)) {
        OptiLoggerStreamsContainer.error("Invalid Parameter Type for parameter %s in event %s with value %s (\"%s\") but the event's configurations expected type \"%s\"",
            key, event.getName(), value.toString(), value.getClass().getSimpleName(), parameterConfig.getType());
        return EventValidationResult.INCORRECT_VALUE_TYPE_ERROR;
      }
      if (isValueTooLarge(value)) {
        OptiLoggerStreamsContainer.error("Parameter %s in event %s has value %s that is too long (%d characters when the maximum is %d)",
            key, event.getName(), value.toString(), value.toString().length(), OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH);
        return EventValidationResult.VALUE_TOO_LARGE_ERROR;
      }
    }
    return EventValidationResult.VALID;
  }

  private boolean isMissingMandatoryParameter(EventConfigs eventConfig, Map<String, Object> parameters) {
    for (String paramConfigKey : eventConfig.getParameterConfigs().keySet()) {
      EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs().get(paramConfigKey);
      if (!parameterConfig.isOptional() && !parameters.containsKey(paramConfigKey)) {
        OptiLogger.f129(paramConfigKey, event.getName());
        return true;
      }
    }
    return false;
  }

  private boolean isInvalidParameterName(EventConfigs.ParameterConfig parameterConfig) {
    return parameterConfig == null;
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
    return value.toString().length() > OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH;
  }
}
