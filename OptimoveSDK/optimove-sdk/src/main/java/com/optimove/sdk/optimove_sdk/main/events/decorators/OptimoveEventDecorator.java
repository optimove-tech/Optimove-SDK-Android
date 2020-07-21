package com.optimove.sdk.optimove_sdk.main.events.decorators;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.HashMap;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_DEVICE_TYPE_DEFAULT_VALUE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_DEVICE_TYPE_PARAM_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_NATIVE_MOBILE_DEFAULT_VALUE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_NATIVE_MOBILE_PARAM_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_OS_DEFAULT_VALUE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_OS_PARAM_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_PLATFORM_DEFAULT_VALUE;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_PLATFORM_PARAM_KEY;

/**
 * Decorator class to enforce BL on the incoming {@link OptimoveEvent}s:
 * <ul>
 * <li>Add additional attributes in accordance to the event's configs</li>
 * </ul>
 */
public class OptimoveEventDecorator extends OptimoveEvent {

  protected OptimoveEvent optimoveEvent;
  protected Map<String, Object> modifiedEventParams;
  private int maxNumberOfParamsToAdd;

  public OptimoveEventDecorator(OptimoveEvent optimoveEvent, int maxNumberOfParamsToAdd) {
    super(optimoveEvent.getTimestamp());
    this.optimoveEvent = optimoveEvent;
    this.validationIssues = optimoveEvent.getValidationIssues();
    this.maxNumberOfParamsToAdd =  maxNumberOfParamsToAdd;
    this.setupParameters();
  }
  public OptimoveEventDecorator(OptimoveEvent optimoveEvent, EventConfigs eventConfigs,  int maxNumberOfParamsToAdd) {
    super(optimoveEvent.getTimestamp());
    this.optimoveEvent = optimoveEvent;
    this.validationIssues = optimoveEvent.getValidationIssues();
    this.maxNumberOfParamsToAdd =  maxNumberOfParamsToAdd;
    this.setupParameters();
    this.processEventConfigurations(eventConfigs);
  }


  private void setupParameters(){
    this.modifiedEventParams = new HashMap<>();
    Map<String, Object> optimoveEventParameters = optimoveEvent.getParameters();
    if (optimoveEventParameters != null) {
      for (String paramKey : optimoveEventParameters.keySet()) {
        Object val = optimoveEventParameters.get(paramKey);
        if (val instanceof String) {
          modifiedEventParams.put(paramKey, val.toString().trim());
        } else {
          modifiedEventParams.put(paramKey, val);
        }
      }
    }
  }

  /**
   * Once the {@link EventConfigs} was found, pass
   * them to the decorator to perform BL rules
   * that depend on the event's configurations.<br>
   * Use this method when the event's configurations is not available at the moment of creation.
   *
   * @param eventConfig The decorated event's configurations
   */
  public void processEventConfigurations(EventConfigs eventConfig) {
    modifiedEventParams.putAll(getAdditionalAttributesMap(eventConfig));
  }

  @Override
  public String getName() {
    return optimoveEvent.getName();
  }

  @Override
  public Map<String, Object> getParameters() {
    return modifiedEventParams;
  }

  public void setParameters(Map<String, Object> parameters){
    this.modifiedEventParams = parameters;
  }

  /**
   * Based on the provided event's configurations, create a {@code Map<String, Object>} of the <i>Addition Attributes</i>.<br>
   * <b>Note</b>: Only the additional attributes that were stated in the event's configurations are returned by this method
   *
   * @param eventConfig The decorated event's configuration
   * @return The {@code Map<String, Object>} with the Additional Attributes
   */
  private Map<String, Object> getAdditionalAttributesMap(EventConfigs eventConfig) {
    Map<String, Object> parameters = new HashMap<>(4);
    Map<String, EventConfigs.ParameterConfig> parameterConfigs = eventConfig.getParameterConfigs();
    if (parameterConfigs.containsKey(EVENT_NATIVE_MOBILE_PARAM_KEY) && maxNumberOfParamsToAdd > 0) {
      maxNumberOfParamsToAdd--;
      parameters.put(EVENT_NATIVE_MOBILE_PARAM_KEY, EVENT_NATIVE_MOBILE_DEFAULT_VALUE);
    }
    if (parameterConfigs.containsKey(EVENT_PLATFORM_PARAM_KEY) && maxNumberOfParamsToAdd > 0) {
      maxNumberOfParamsToAdd--;
      parameters.put(EVENT_PLATFORM_PARAM_KEY, EVENT_PLATFORM_DEFAULT_VALUE);
    }
    if (parameterConfigs.containsKey(EVENT_DEVICE_TYPE_PARAM_KEY) && maxNumberOfParamsToAdd > 0) {
      maxNumberOfParamsToAdd--;
      parameters.put(EVENT_DEVICE_TYPE_PARAM_KEY, EVENT_DEVICE_TYPE_DEFAULT_VALUE);
    }
    if (parameterConfigs.containsKey(EVENT_OS_PARAM_KEY) && maxNumberOfParamsToAdd > 0) {
      parameters.put(EVENT_OS_PARAM_KEY, EVENT_OS_DEFAULT_VALUE);
    }
    return parameters;
  }
}
