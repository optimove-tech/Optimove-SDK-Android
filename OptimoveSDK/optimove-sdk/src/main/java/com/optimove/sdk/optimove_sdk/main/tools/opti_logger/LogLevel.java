package com.optimove.sdk.optimove_sdk.main.tools.opti_logger;

import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;

public enum LogLevel {
  DEBUG(0),
  INFO(1),
  WARN(2),
  ERROR(3),
  FATAL(4);

  private int rawLevel;

  @Nullable
  public static LogLevel fromString(String name) {
    if (OptiUtils.isEmptyOrWhitespace(name)) {
      return null;
    }
    String cleanName = name.trim().toUpperCase();
    LogLevel logLevel;
    try {
      logLevel = LogLevel.valueOf(cleanName);
    } catch (IllegalArgumentException e) {
      OptiLoggerStreamsContainer.warn("Illegal LogLevel name %s was passed to LogLevel#fromString", name);
      return null;
    }
    return logLevel;
  }

  LogLevel(int rawLevel) {
    this.rawLevel = rawLevel;
  }

  public int getRawLevel() {
    return rawLevel;
  }
}
