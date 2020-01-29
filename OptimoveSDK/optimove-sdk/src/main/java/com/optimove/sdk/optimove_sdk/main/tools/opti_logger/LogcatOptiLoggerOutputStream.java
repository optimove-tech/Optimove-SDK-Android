package com.optimove.sdk.optimove_sdk.main.tools.opti_logger;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("LogNotTimber")
public class LogcatOptiLoggerOutputStream implements OptiLoggerOutputStream {

  public LogcatOptiLoggerOutputStream() {
  }

  @Override
  public boolean isVisibleToClient() {
    return true;
  }

  @Override
  public void reportLog(LogLevel logLevel, String logClass, String logMethod, String message) {
    String finalTag = String.format("OptimoveSDK-%s/%s", logClass, logMethod);
    switch (logLevel) {
      case DEBUG:
        Log.d(finalTag, message);
        break;
      case INFO:
        Log.i(finalTag, message);
        break;
      case WARN:
        Log.w(finalTag, message);
        break;
      case ERROR:
        Log.e(finalTag, message);
        break;
      case FATAL:
        Log.wtf(finalTag, message);
        break;
    }
  }
}
