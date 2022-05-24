package com.optimove.android.main.tools.opti_logger;

import androidx.annotation.NonNull;

import com.optimove.android.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteLogsServiceOutputStream implements OptiLoggerOutputStream {

    private static final String LOG_SERVICE_BASE_URL = "https://mbaas-qa.optimove.net/";

    private final String packageName;
    private final OkHttpClient httpClient;
    private int tenantId;

    public RemoteLogsServiceOutputStream(OkHttpClient httpClient, String packageName, int tenantId) {
        this.httpClient = httpClient;
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
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                getRequestBody(logClass, logMethod, parseLogLevelJsonValue(logLevel), message).toString());

        Request logRequest = new Request.Builder().url(LOG_SERVICE_BASE_URL + String.format(
                "%s/%s", "report","log")).post(body).build();

        httpClient.newCall(logRequest).enqueue(new BlankCallback());
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

    static class BlankCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }
        @Override
        public void onResponse(@NonNull Call call,@NonNull Response response) throws IOException {

        }
    }
}
