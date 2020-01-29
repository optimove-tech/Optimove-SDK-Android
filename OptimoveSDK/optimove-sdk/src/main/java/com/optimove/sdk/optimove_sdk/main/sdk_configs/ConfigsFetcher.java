package com.optimove.sdk.optimove_sdk.main.sdk_configs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedTenantConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import java.util.Set;

public class ConfigsFetcher {


    private static final String GLOBAL_CONFIG_VERSION = "v2";
    public static final String TENANT_CONFIG_FILE_BASE_URL = "https://sdk-cdn.optimove.net/mobilesdkconfig/";
    public static final String GLOBAL_CONFIG_FILE_BASE_URL = "https://sdk-cdn.optimove.net/configs/mobile/global/";

    @Nullable
    private FetchedTenantConfigs fetchedTenantConfigs;
    @Nullable
    private FetchedGlobalConfig fetchedGlobalConfig;

    private boolean isUrgent;
    @NonNull
    private String configName;
    @NonNull
    private String tenantToken;
    @NonNull
    private SharedPreferences localConfigKeysPreferences;
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private FileUtils fileUtils;
    @NonNull
    private Context context;

    public interface ConfigsListener {
        void setConfigs(Configs configs);
    }

    public interface ConfigsErrorListener {
        void error(String error);
    }

    private ConfigsFetcher(@NonNull String tenantToken, @NonNull String configName,
                            boolean isUrgent,
                           @NonNull SharedPreferences localConfigKeysPreferences,
                           @NonNull HttpClient httpClient, @NonNull FileUtils fileUtils,
                           @NonNull Context context) {
        this.isUrgent = isUrgent;
        this.configName = configName;
        this.localConfigKeysPreferences = localConfigKeysPreferences;
        this.tenantToken = tenantToken;
        this.httpClient = httpClient;
        this.fileUtils = fileUtils;
        this.context = context;
    }


    public void fetchConfigs(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        if (isUrgent) {
            getLocalConfig(configsListener, configsErrorListener);
        } else {
            fetchRemoteConfig(configsListener, configsErrorListener);
        }
    }

    private void fetchRemoteConfig(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        HttpClient.RequestBuilder<FetchedGlobalConfig> globalConfigRequestBuilder = httpClient
                .getObject(GLOBAL_CONFIG_FILE_BASE_URL, FetchedGlobalConfig.class)
                .destination("%s/%s/%s.json", GLOBAL_CONFIG_VERSION,
                        OptiUtils.getSdkEnv(ApplicationHelper.getBasePackageName(context)), "configs");
        HttpClient.RequestBuilder<FetchedTenantConfigs> tenantConfigRequestBuilder = httpClient
                .getObject(TENANT_CONFIG_FILE_BASE_URL, FetchedTenantConfigs.class)
                .destination("%s/%s.json", tenantToken, configName);
        globalConfigRequestBuilder.successListener(fetchedGlobalConfig -> {
            try {
                this.fetchedGlobalConfig = fetchedGlobalConfig;
                sendConfigsIfGlobalAndTenantArrived(configsListener);
            } catch (JsonSyntaxException exception) {
                OptiLogger.failedToGetRemoteConfigurationFile(exception.getLocalizedMessage());
                getLocalConfig(configsListener, configsErrorListener);
            }
        })
                .errorListener(error -> {
                    OptiLogger.failedToGetRemoteConfigurationFile(error.getLocalizedMessage());
                    getLocalConfig(configsListener, configsErrorListener);
                })
                .send();
        tenantConfigRequestBuilder.successListener(fetchedTenantConfigs -> {
            try {
                this.fetchedTenantConfigs = fetchedTenantConfigs;
                sendConfigsIfGlobalAndTenantArrived(configsListener);
            } catch (JsonSyntaxException exception) {
                OptiLogger.failedToGetRemoteConfigurationFile(exception.getLocalizedMessage());
                getLocalConfig(configsListener, configsErrorListener);
            }
        })
                .errorListener(error -> {
                    OptiLogger.failedToGetRemoteConfigurationFile(error.getLocalizedMessage());
                    getLocalConfig(configsListener, configsErrorListener);
                })
                .send();
    }

    private void sendConfigsIfGlobalAndTenantArrived(ConfigsListener configsListener) {
        if (fetchedGlobalConfig != null && fetchedTenantConfigs != null) {
            Configs configs =
                    FetchedLocalConfigsMapper.mapFetchedConfigsToLocal(ApplicationHelper.getFullPackageName(context), fetchedGlobalConfig, fetchedTenantConfigs);
            backupInitData(configs);
            configsListener.setConfigs(configs);
        }
    }

    private void getLocalConfig(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        boolean isConfigurationStored = localConfigKeysPreferences.getBoolean(configName, false);
        if (!isConfigurationStored) {
            deleteRedundantLocalConfigs();
            configsErrorListener.error("Requested configs name not found locally");
            return;
        }

        new Thread(() -> {
            String configString = fileUtils.readFile(context)
                    .named(configName)
                    .from(FileUtils.SourceDir.INTERNAL)
                    .asString();
            if (configString == null) {
                configsErrorListener.error("Local configs reading error");
            } else {
                try {
                    Configs configs = new Gson().fromJson(configString, Configs.class);
                    // in case of failed app update fetch
                    if ((configs != null) && (configsAreValid(configs))) {
                        configsListener.setConfigs(configs);
                    } else {
                        configsErrorListener.error("Local configs corrupted");
                    }
                } catch (JsonSyntaxException exception) {
                    configsErrorListener.error("Local configs corrupted");
                }


            }
        }).start();
    }

    private boolean configsAreValid(Configs configs) {
        return configs.getTenantId() != 0;
    }

    private void backupInitData(final Configs configs) {
        new Thread(() -> {
            OptiLogger.f110();
            Gson gson = new Gson();
            localConfigKeysPreferences.edit()
                    .putBoolean(configName, true)
                    .apply();

            fileUtils.write(context, gson.toJson(configs))
                    .to(configName)
                    .in(FileUtils.SourceDir.INTERNAL)
                    .now();
        }).start();
    }


    private void deleteRedundantLocalConfigs() {
        new Thread(() -> {
            Set<String> savedNames = localConfigKeysPreferences.getAll()
                    .keySet();

            if (savedNames.size() <= 1) { //Only the one latest config is saved
                return;
            }
            SharedPreferences.Editor editor = localConfigKeysPreferences.edit();
            for (String configName : savedNames) {

                if (configName.equals(this.configName)) {
                    continue;
                }
                editor.remove(configName);
                fileUtils.deleteFile(context)
                        .named(configName)
                        .from(FileUtils.SourceDir.INTERNAL)
                        .now();
                OptiLogger.f115(configName);
            }
            editor.apply();
        }).start();
    }

    public static HttpClientStep builder() {
        return new Builder();
    }

    public interface HttpClientStep {
        TenantTokenStep httpClient(HttpClient httpClient);
    }

    public interface TenantTokenStep {
        ConfigNameStep tenantToken(String tenantToken);
    }

    public interface ConfigNameStep {
        IsUrgentStep configName(String configName);
    }

//    public interface DeviceStateMonitorStep {
//        IsUrgentStep deviceStateMonitor(DeviceStateMonitor deviceStateMonitor);
//    }

    public interface IsUrgentStep {
        SharedPrefsStep urgent(boolean isUrgent);
    }

    public interface SharedPrefsStep {
        FileProviderStep sharedPrefs(SharedPreferences sharedPreferences);
    }

    public interface FileProviderStep {
        ContextStep fileProvider(FileUtils fileUtils);
    }

    public interface ContextStep {
        Build context(Context context);
    }

    public interface Build {
        ConfigsFetcher build();
    }

    public static class Builder implements ContextStep, SharedPrefsStep, IsUrgentStep,
            ConfigNameStep, TenantTokenStep, HttpClientStep, FileProviderStep, Build {

        private boolean isUrgent;
        private String configName;
        private String tenantToken;
        private SharedPreferences localConfigKeysPreferences;
        private HttpClient httpClient;
        private FileUtils fileUtils;
        private Context context;

        @Override
        public TenantTokenStep httpClient(@NonNull HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public ConfigNameStep tenantToken(@NonNull String tenantToken) {
            this.tenantToken = tenantToken;
            return this;
        }

        @Override
        public IsUrgentStep configName(@NonNull String configName) {
            this.configName = configName;
            return this;
        }


        @Override
        public SharedPrefsStep urgent(boolean isUrgent) {
            this.isUrgent = isUrgent;
            return this;
        }

        @Override
        public FileProviderStep sharedPrefs(@NonNull SharedPreferences localConfigKeysPreferences) {
            this.localConfigKeysPreferences = localConfigKeysPreferences;
            return this;
        }

        @Override
        public ContextStep fileProvider(@NonNull FileUtils fileUtils) {
            this.fileUtils = fileUtils;
            return this;
        }

        @Override
        public Build context(@NonNull Context context) {
            this.context = context;
            return this;
        }

        @Override
        public ConfigsFetcher build() {
            return new ConfigsFetcher(tenantToken, configName, isUrgent,
                    localConfigKeysPreferences, httpClient, fileUtils, context);

        }
    }


}
