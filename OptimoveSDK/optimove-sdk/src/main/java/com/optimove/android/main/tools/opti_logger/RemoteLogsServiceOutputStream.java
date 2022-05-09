package com.optimove.android.main.tools.opti_logger;

import android.content.Context;

import com.optimove.android.BuildConfig;
import com.optimove.android.main.tools.networking.HttpClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RemoteLogsServiceOutputStream implements OptiLoggerOutputStream {

    private static final String LOG_SERVICE_BASE_URL = "https://mbaas-qa.optimove.net/";

    private Context context;
    private int tenantId;

    public RemoteLogsServiceOutputStream(Context context, int tenantId) {
        this.context = context;
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
                .destination("%s/%s","report","log")
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
        json.put(BodyKeys.APP_NS, context.getPackageName());
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
