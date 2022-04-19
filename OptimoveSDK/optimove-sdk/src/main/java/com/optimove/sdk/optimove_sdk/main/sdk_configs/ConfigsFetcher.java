package com.optimove.sdk.optimove_sdk.main.sdk_configs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedTenantConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.util.Set;
import java.util.concurrent.Executors;

public class ConfigsFetcher {


    private static final String GLOBAL_CONFIG_VERSION = "v4";
    public static final String TENANT_CONFIG_FILE_BASE_URL = "https://sdk-cdn.optimove.net/mobilesdkconfig/";
    public static final String GLOBAL_CONFIG_FILE_BASE_URL = "https://sdk-cdn.optimove.net/configs/mobile/global/";

    @NonNull
    private final String configName;
    @NonNull
    private final String tenantToken;
    @NonNull
    private final SharedPreferences localConfigKeysPreferences;
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final FileUtils fileUtils;
    @NonNull
    private final Context context;

    @Nullable
    private FetchedTenantConfigs fetchedTenantConfigs;
    @Nullable
    private FetchedGlobalConfig fetchedGlobalConfig;

    public interface ConfigsListener {
        void setConfigs(Configs configs);

    }

    public interface ConfigsErrorListener {
        void error(String error);
    }

    private ConfigsFetcher(@NonNull String tenantToken, @NonNull String configName,
                           @NonNull SharedPreferences localConfigKeysPreferences,
                           @NonNull HttpClient httpClient, @NonNull FileUtils fileUtils,
                           @NonNull Context context) {
        this.configName = configName;
        this.localConfigKeysPreferences = localConfigKeysPreferences;
        this.tenantToken = tenantToken;
        this.httpClient = httpClient;
        this.fileUtils = fileUtils;
        this.context = context;
    }


    public void fetchConfigs(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        fetchRemoteConfig(configsListener, configsErrorListener);
    }

    private void fetchRemoteConfig(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        httpClient.getObject(GLOBAL_CONFIG_FILE_BASE_URL, FetchedGlobalConfig.class)
                .destination("%s/%s/%s.json", GLOBAL_CONFIG_VERSION,
                        OptiUtils.getSdkEnv(context.getPackageName()), "configs")
                .successListener(fetchedGlobalConfig -> setFetchedGlobalConfigs(fetchedGlobalConfig, configsListener,
                        configsErrorListener))
                .errorListener(e -> configFetchFailed(e, configsListener, configsErrorListener))
                .send();
        httpClient.getObject(TENANT_CONFIG_FILE_BASE_URL, FetchedTenantConfigs.class)
                .destination("%s/%s.json", tenantToken, configName)
                .successListener(fetchedTenantConfigs -> setFetchedTenantConfigs(fetchedTenantConfigs, configsListener,
                        configsErrorListener))
                .errorListener(e -> configFetchFailed(e, configsListener, configsErrorListener))
                .send();
    }


    private void setFetchedGlobalConfigs(FetchedGlobalConfig fetchedGlobalConfig, ConfigsListener configsListener,
                                         ConfigsErrorListener configsErrorListener) {
        try {
            this.fetchedGlobalConfig = fetchedGlobalConfig;
            sendConfigsIfGlobalAndTenantArrived(configsListener, configsErrorListener);
        } catch (JsonSyntaxException exception) {
            OptiLoggerStreamsContainer.error("Failed to get remote configuration file due to - %s", exception.getMessage());
            getLocalConfig(configsListener, configsErrorListener);
        }
    }

    private void setFetchedTenantConfigs(FetchedTenantConfigs fetchedTenantConfigs, ConfigsListener configsListener,
                                         ConfigsErrorListener configsErrorListener) {
        try {
            this.fetchedTenantConfigs = fetchedTenantConfigs;
            sendConfigsIfGlobalAndTenantArrived(configsListener, configsErrorListener);
        } catch (JsonSyntaxException exception) {
            OptiLoggerStreamsContainer.error("Failed to get remote configuration file due to - %s", exception.getMessage());
            getLocalConfig(configsListener, configsErrorListener);
        }
    }

    private void configFetchFailed(Throwable throwable, ConfigsListener configsListener,
                                         ConfigsErrorListener configsErrorListener) {
        OptiLoggerStreamsContainer.error("Failed to get remote configuration file due to - %s",
                throwable.getMessage());
        getLocalConfig(configsListener, configsErrorListener);
    }

    private void sendConfigsIfGlobalAndTenantArrived(ConfigsListener configsListener,
                                                     ConfigsErrorListener configsErrorListener) {
        if (fetchedGlobalConfig == null || fetchedTenantConfigs == null) {
            return;
        }
        Configs configs;
        try {
            configs = FetchedLocalConfigsMapper.mapFetchedConfigsToLocal(fetchedGlobalConfig, fetchedTenantConfigs);
        } catch (Throwable throwable) {
            configsErrorListener.error("Failed to build a config from global and tenant configs");
            return;
        }
        if (!verifyConfigValidity(configs)) {
            configsErrorListener.error("Config file is corrupted");
            return;
        }

        backupInitData(configs);
        configsListener.setConfigs(configs);
    }

    private boolean verifyConfigValidity(Configs configs) {
        return configs.getTenantId() != 0
                && configs.getLogsConfigs() != null
                && configs.getEventsConfigs() != null
                && configs.getOptitrackConfigs() != null
                && configs.getRealtimeConfigs() != null;
    }

    private void getLocalConfig(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
        boolean isConfigurationStored = localConfigKeysPreferences.getBoolean(configName, false);
        if (!isConfigurationStored) {
            deleteRedundantLocalConfigs();
            configsErrorListener.error("Requested configs name not found locally");
            return;
        }

        Executors.newSingleThreadExecutor()
                .execute(() -> getLocalConfigFromFile(configsListener,
                        configsErrorListener));
    }

    private void getLocalConfigFromFile(ConfigsListener configsListener, ConfigsErrorListener configsErrorListener) {
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
                if ((configs != null) && (verifyConfigValidity(configs))) {
                    configsListener.setConfigs(configs);
                } else {
                    configsErrorListener.error("Local configs corrupted");
                }
            } catch (Throwable exception) {
                configsErrorListener.error("Local configs corrupted");
            }
        }
    }

    private void backupInitData(final Configs configs) {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    OptiLoggerStreamsContainer.debug("Saving fetched configurations file");
                    Gson gson = new Gson();
                    localConfigKeysPreferences.edit()
                            .putBoolean(configName, true)
                            .apply();

                    fileUtils.write(context, gson.toJson(configs))
                            .to(configName)
                            .in(FileUtils.SourceDir.INTERNAL)
                            .now();
                });
    }


    private void deleteRedundantLocalConfigs() {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
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
                        OptiLoggerStreamsContainer.debug("Deleted local configurations named %s", configName);
                    }
                    editor.apply();
                });
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
        SharedPrefsStep configName(String configName);
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

    public static class Builder implements ContextStep, SharedPrefsStep,
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
        public SharedPrefsStep configName(@NonNull String configName) {
            this.configName = configName;
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
            return new ConfigsFetcher(tenantToken, configName,
                    localConfigKeysPreferences, httpClient, fileUtils, context);

        }
    }


}
