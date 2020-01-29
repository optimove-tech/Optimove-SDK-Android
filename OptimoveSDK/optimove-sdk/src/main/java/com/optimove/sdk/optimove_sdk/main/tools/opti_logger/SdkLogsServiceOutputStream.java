package com.optimove.sdk.optimove_sdk.main.tools.opti_logger;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SdkLogsServiceOutputStream implements OptiLoggerOutputStream {

  private static final String LOG_SERVICE_BASE_URL = "https://us-central1-mobilepush-161510.cloudfunctions.net/";
  private static final String REPORT_LOG_PATH = "reportLog";

  private Context context;
  private String packageName;
  private int tenantId;

  public SdkLogsServiceOutputStream(Context context, String packageName, int tenantId) {
    this.context = context;
    this.packageName = packageName;
    this.tenantId = tenantId;
  }

  public void setTenantId(int tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public boolean isVisibleToClient() {
    return false;
  }

  @Override
  public void reportLog(LogLevel logLevel, String logClass, String logMethod, String message) {
    HttpClient.getInstance(context)
        .postJson(LOG_SERVICE_BASE_URL, getRequestBody(logClass, logMethod, parseLogLevelJsonValue(logLevel), message))
        .destination(REPORT_LOG_PATH)
        .send();
  }

  private String parseLogLevelJsonValue(LogLevel logLevel) {
    switch (logLevel) {
      case DEBUG:
        return LogLevels.DEBUG;
      case INFO:
        return LogLevels.INFO;
      case WARN:
        return LogLevels.WARN;
      case ERROR:
        return LogLevels.ERROR;
      case FATAL:
        return LogLevels.FATAL;
    }
    throw new IllegalStateException("parseLogLevelJsonValue is missing a switch case!");
  }

  private JSONObject getRequestBody(String logClass, String logMethod, String logLevel, String message) {
    Map<String, Object> json = new HashMap<>(8);
    json.put(BodyKeys.TENANT_ID, tenantId);
    json.put(BodyKeys.APP_NS, packageName);
    //BuildConfig here instead of Optiutils.getSdkEnv to prevent infinity loop
    json.put(BodyKeys.SDK_ENV, BuildConfig.OPTIMOVE_SDK_RUNTIME_ENV);
    json.put(BodyKeys.SDK_PLATFORM, "android");
    json.put(BodyKeys.LEVEL, logLevel);
    json.put(BodyKeys.LOG_FILE_NAME, logClass);
    json.put(BodyKeys.LOG_METHOD_NAME, logMethod);
    json.put(BodyKeys.MESSAGE, message);

    return new JSONObject(json);
  }

  private interface BodyKeys {
    String TENANT_ID = "tenantId";
    String APP_NS = "appNs";
    String SDK_ENV = "sdkEnv";
    String SDK_PLATFORM = "sdkPlatform";
    String LEVEL = "level";
    String LOG_FILE_NAME = "logFileName";
    String LOG_METHOD_NAME = "logMethodName";
    String MESSAGE = "message";
  }

  private interface LogLevels {
    String DEBUG = "debug";
    String INFO = "info";
    String WARN = "warn";
    String ERROR = "error";
    String FATAL = "fatal";
  }
}
