package com.optimove.sdk.optimove_sdk.main;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.internal.Preconditions;
import com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogcatOptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.SdkLogsServiceOutputStream;

import java.util.Date;

import static com.optimove.sdk.optimove_sdk.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;

public class OptimoveInitProvider extends ContentProvider {

    static final String EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY = "com.optimove.sdk.optimove_sdk.optimoveinitprovider";

    public OptimoveInitProvider() {
    }

    public void attachInfo(Context context, ProviderInfo info) {
        Preconditions.checkNotNull(info, "OptimoveInitProvider ProviderInfo cannot be null.");
        if (EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY.equals(info.authority)) {
            throw new IllegalStateException("Incorrect provider authority in manifest. Most likely due to a missing applicationId variable in application's build.gradle.");
        }
        super.attachInfo(context, info);
    }


    @Override
    public boolean onCreate() {
        String date = new Date().toString(); // volley bug workaround
        Context context = getContext();
        if (context == null) {
            return false;
        }


        String packageName = ApplicationHelper.getBasePackageName(context);
        initializeLogger(context, packageName);

        if (packageName == null){
            OptiLoggerStreamsContainer.warn("Auto init disabled because BuildConfig values were not found, " +
                    "please initialize the SDK manually");
            return false;
        }


        boolean disableAutoInit = (boolean) OptiUtils.getBuildConfig(packageName, "OPTIMOVE_DISABLE_AUTO_INIT", false);
        if (disableAutoInit) {
            OptiLoggerStreamsContainer.info("Client flagged to disable the auto init");
            return false;
        }

        TenantInfo tenantInfo = getTenantInfo(context);
        if (tenantInfo != null) {
            Optimove.configure(context, tenantInfo);
        }
        return false;
    }
    public static void initializeLogger(Context context, String packageName) {
        Boolean isClientStgEnv =
                (Boolean) OptiUtils.getBuildConfig(packageName, BuildConfigKeys.OPTIMOVE_CLIENT_STG_ENV_KEY, false);
        SharedPreferences coreSharedPreferences =
                context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE, Context.MODE_PRIVATE);
        LogLevel sdkLogsServiceMinLogLevel = isClientStgEnv ? LogLevel.DEBUG : LogLevel.FATAL;
        OptiLoggerStreamsContainer.setMinLogLevelRemote(sdkLogsServiceMinLogLevel);
        OptiLoggerStreamsContainer.addOutputStream(new SdkLogsServiceOutputStream(context,
                coreSharedPreferences.getInt(TENANT_ID, -1)));

        Object minLogLevelBuildConfigObject =
                OptiUtils.getBuildConfig(packageName, BuildConfigKeys.OPTIMOVE_MIN_LOG_LEVEL_KEY, null);
        LogLevel logcatMinLogLevel = minLogLevelBuildConfigObject == null ? LogLevel.WARN :
                LogLevel.fromString(String.valueOf(minLogLevelBuildConfigObject));
        OptiLoggerStreamsContainer.setMinLogLevelToShow(logcatMinLogLevel);
        OptiLoggerStreamsContainer.addOutputStream(new LogcatOptiLoggerOutputStream());
    }

    @Nullable
    private TenantInfo getTenantInfo(Context context) {
        String token;
        String configName;

        Object tokenObject =
                OptiUtils.getBuildConfig(ApplicationHelper.getBasePackageName(context), "OPTIMOVE_TENANT_TOKEN");
        if (tokenObject == null) {
            return null;
        }
        token = tokenObject.toString();

        Object configNameObject =
                OptiUtils.getBuildConfig(ApplicationHelper.getBasePackageName(context), "OPTIMOVE_CONFIG_NAME");
        if (configNameObject == null) {
            return null;
        }
        configName = configNameObject.toString();

        return new TenantInfo(token, configName);
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
