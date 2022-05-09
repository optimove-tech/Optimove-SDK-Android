package com.optimove.android.main.tools.opti_logger;

/**
 * Implementations of this interface are eligible for printing any logs created by the {@code Optimove SDK} to their desired location.
 */
public interface OptiLoggerOutputStream {

  boolean isVisibleToClient();

  void reportLog(LogLevel logLevel, String logClass, String logMethod, String message);
}
