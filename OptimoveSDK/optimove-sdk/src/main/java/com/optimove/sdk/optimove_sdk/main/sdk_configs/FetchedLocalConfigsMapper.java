package com.optimove.sdk.optimove_sdk.main.sdk_configs;

import androidx.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.LogsConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedTenantConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.HashMap;
import java.util.Map;

public class FetchedLocalConfigsMapper {

    public static Configs mapFetchedConfigsToLocal(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                                   @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        int tenantId = fetchedTenantConfigs.optitrackMetaData.siteId;

        LogsConfigs logsConfigs = getLogsConfigs(fetchedGlobalConfig, fetchedTenantConfigs);
        RealtimeConfigs realtimeConfigs = getRealtimeConfigs(fetchedTenantConfigs);
        OptitrackConfigs optitrackConfigs = getOptitrackConfigs(tenantId, fetchedTenantConfigs);
        Map<String, EventConfigs> eventConfigsMap = new HashMap<>();
        eventConfigsMap.putAll(fetchedTenantConfigs.eventsConfigs);
        eventConfigsMap.putAll(fetchedGlobalConfig.coreEventsConfigs); // second! to override tenant configs

        return new Configs(tenantId, fetchedTenantConfigs.enableRealtime,
                fetchedTenantConfigs.enableRealtimeThroughOptistream, fetchedTenantConfigs.supportAirship,
                logsConfigs,
                realtimeConfigs,
                optitrackConfigs,
                fetchedGlobalConfig.fetchedOptipushConfigs.mbaasEndpoint,
                eventConfigsMap);
    }

    private static LogsConfigs getLogsConfigs(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                              @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        LogsConfigs logsConfigs = new LogsConfigs();
        logsConfigs.setTenantId(fetchedTenantConfigs.optitrackMetaData.siteId);
        logsConfigs.setLogsServiceEndpoint(fetchedGlobalConfig.fetchedGeneralConfigs.logsServiceEndpoint);
        logsConfigs.setProdLogsEnabled(fetchedTenantConfigs.prodLogsEnabled);
        return logsConfigs;
    }

    private static RealtimeConfigs getRealtimeConfigs(@NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        RealtimeConfigs realtimeConfigs = new RealtimeConfigs();
        realtimeConfigs.setRealtimeGateway(fetchedTenantConfigs.realtimeMetaData.realtimeGateway);
        realtimeConfigs.setRealtimeToken(fetchedTenantConfigs.realtimeMetaData.realtimeToken);
        return realtimeConfigs;
    }

    private static OptitrackConfigs getOptitrackConfigs(int tenantId,
                                                        @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        OptitrackConfigs optitrackConfigs = new OptitrackConfigs();
        optitrackConfigs.setSiteId(tenantId);
        optitrackConfigs.setOptitrackEndpoint(fetchedTenantConfigs.optitrackMetaData.optitrackEndpoint);
        optitrackConfigs.setMaxNumberOfParameters(fetchedTenantConfigs.optitrackMetaData.maxActionCustomDimensions);

        return optitrackConfigs;
    }

}
