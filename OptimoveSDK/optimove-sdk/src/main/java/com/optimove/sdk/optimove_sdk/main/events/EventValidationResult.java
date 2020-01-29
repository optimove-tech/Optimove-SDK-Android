package com.optimove.sdk.optimove_sdk.main.events;

public enum EventValidationResult {
  VALID,
  /**
   * The event's {@code name} was not found in the <i>custom events contract</i>
   */
  UNKNOWN_EVENT_NAME_ERROR,
  /**
   * A parameter's {@code name} was not found in the <i>custom events contract</i>
   */
  UNKNOWN_PARAMETER_NAME_ERROR,
  /**
   * A parameter's {@code values} size was <b>too large</b> (max is <b>255</b> chars)
   */
  VALUE_TOO_LARGE_ERROR,
  /**
   * The event is missing a {@code manadatory parameter} as defined in the <i>custom events contract</i>
   */
  MISSING_MANDATORY_PARAMETER_ERROR,
  /**
   * A parameter's {@code value type} didn't match the type that was defined in the <i>custom events contract</i>
   */
  INCORRECT_VALUE_TYPE_ERROR,
  /**
   * A general error
   */
  ERROR
}
