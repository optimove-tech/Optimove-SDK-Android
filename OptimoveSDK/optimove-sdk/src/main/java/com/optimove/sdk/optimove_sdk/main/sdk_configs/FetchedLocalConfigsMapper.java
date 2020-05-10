package com.optimove.sdk.optimove_sdk.main.sdk_configs;

import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.LogsConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedTenantConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.HashMap;
import java.util.Map;

public class FetchedLocalConfigsMapper {

    public static Configs mapFetchedConfigsToLocal(@NonNull String packageName,
                                                   @NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                                   @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        int tenantId = fetchedTenantConfigs.optitrackMetaData.siteId;

        LogsConfigs logsConfigs = getLogsConfigs(fetchedGlobalConfig, fetchedTenantConfigs);
        RealtimeConfigs realtimeConfigs = getRealtimeConfigs(fetchedTenantConfigs);
        OptitrackConfigs optitrackConfigs = getOptitrackConfigs(tenantId, fetchedGlobalConfig, fetchedTenantConfigs);
        OptipushConfigs optipushConfigs = getOptipushConfigs(fetchedGlobalConfig, fetchedTenantConfigs, packageName);
        Map<String, EventConfigs> eventConfigsMap = new HashMap<>();
        eventConfigsMap.putAll(fetchedTenantConfigs.eventsConfigs);
        eventConfigsMap.putAll(fetchedGlobalConfig.coreEventsConfigs); // second! to override tenant configs

        return new Configs(tenantId, fetchedTenantConfigs.enableRealtime,
                fetchedTenantConfigs.enableRealtimeThroughOptistream, logsConfigs,
                realtimeConfigs,
                optitrackConfigs,
                optipushConfigs,
                eventConfigsMap);
    }

    private static LogsConfigs getLogsConfigs(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                              @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        LogsConfigs logsConfigs = new LogsConfigs();
        logsConfigs.setTenantId(fetchedTenantConfigs.optitrackMetaData.siteId);
        logsConfigs.setLogsServiceEndpoint(fetchedGlobalConfig.fetchedGeneralConfigs.logsServiceEndpoint);
        return logsConfigs;
    }

    private static RealtimeConfigs getRealtimeConfigs(@NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        RealtimeConfigs realtimeConfigs = new RealtimeConfigs();
        realtimeConfigs.setRealtimeGateway(fetchedTenantConfigs.realtimeMetaData.realtimeGateway);
        realtimeConfigs.setRealtimeToken(fetchedTenantConfigs.realtimeMetaData.realtimeToken);
        return realtimeConfigs;
    }

    private static OptitrackConfigs getOptitrackConfigs(int tenantId, @NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                                        @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        OptitrackConfigs optitrackConfigs = new OptitrackConfigs();
        optitrackConfigs.setSiteId(tenantId);
        optitrackConfigs.setEnableAdvertisingIdReport(fetchedTenantConfigs.mobile.optipushMetaData.enableAdvertisingIdReport);
        optitrackConfigs.setEventCategoryName(fetchedGlobalConfig.fetchedOptitrackConfigs.eventCategoryName);
        optitrackConfigs.setOptitrackEndpoint(fetchedTenantConfigs.optitrackMetaData.optitrackEndpoint);
        optitrackConfigs.setCustomDimensionIds(fetchedGlobalConfig.fetchedOptitrackConfigs.customDimensionIds);

        return optitrackConfigs;
    }

    private static OptipushConfigs getOptipushConfigs(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                                      @NonNull FetchedTenantConfigs fetchedTenantConfigs,
                                                      @NonNull String packageName) {
        OptipushConfigs optipushConfigs = new OptipushConfigs();
        optipushConfigs.setPushTopicsRegistrationEndpoint(fetchedTenantConfigs.mobile.optipushMetaData.pushTopicsRegistrationEndpoint);
        optipushConfigs.setRegistrationServiceEndpoint(fetchedGlobalConfig.fetchedOptipushConfigs.mbaasEndpoint);
        OptipushConfigs.FirebaseConfigs appControllerFirebaseConfigs = optipushConfigs.new FirebaseConfigs();
        appControllerFirebaseConfigs.setAppId(fetchedTenantConfigs.mobile.firebaseProjectKeys.appIds.androidAppIds.get(packageName));
        appControllerFirebaseConfigs.setDbUrl(fetchedTenantConfigs.mobile.firebaseProjectKeys.dbUrl);
        appControllerFirebaseConfigs.setProjectId(fetchedTenantConfigs.mobile.firebaseProjectKeys.projectId);
        appControllerFirebaseConfigs.setSenderId(fetchedTenantConfigs.mobile.firebaseProjectKeys.senderId);
        appControllerFirebaseConfigs.setStorageBucket(fetchedTenantConfigs.mobile.firebaseProjectKeys.storageBucket);
        appControllerFirebaseConfigs.setWebApiKey(fetchedTenantConfigs.mobile.firebaseProjectKeys.webApiKey);
        optipushConfigs.setAppControllerProjectConfigs(appControllerFirebaseConfigs);

        OptipushConfigs.FirebaseConfigs clientServiceProjectConfigs = optipushConfigs.new FirebaseConfigs();
        clientServiceProjectConfigs.setAppId(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.appIds.androidAppIds.get("android.master.app"));
        clientServiceProjectConfigs.setDbUrl(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.dbUrl);
        clientServiceProjectConfigs.setProjectId(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.projectId);
        clientServiceProjectConfigs.setSenderId(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.senderId);
        clientServiceProjectConfigs.setStorageBucket(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.storageBucket);
        clientServiceProjectConfigs.setWebApiKey(fetchedTenantConfigs.mobile.clientsServiceProjectKeys.webApiKey);
        optipushConfigs.setClientServiceProjectConfigs(clientServiceProjectConfigs);
        return optipushConfigs;
    }
}
