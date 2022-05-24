package com.optimove.android.main.sdk_configs;

import androidx.annotation.NonNull;

import com.optimove.android.main.sdk_configs.configs.Configs;
import com.optimove.android.main.sdk_configs.configs.LogsConfigs;
import com.optimove.android.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.android.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.android.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.android.main.sdk_configs.fetched_configs.FetchedTenantConfigs;
import com.optimove.android.main.sdk_configs.reused_configs.EventConfigs;

import java.util.HashMap;
import java.util.Map;

public class FetchedLocalConfigsMapper {

    public static Configs mapFetchedConfigsToLocal(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                                   @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        int tenantId = fetchedTenantConfigs.optitrackMetaData.getSiteId();

        LogsConfigs logsConfigs = getLogsConfigs(fetchedGlobalConfig, fetchedTenantConfigs);
        RealtimeConfigs realtimeConfigs = getRealtimeConfigs(fetchedTenantConfigs);
        OptitrackConfigs optitrackConfigs = getOptitrackConfigs(tenantId, fetchedTenantConfigs);
        Map<String, EventConfigs> eventConfigsMap = new HashMap<>();
        eventConfigsMap.putAll(fetchedTenantConfigs.eventsConfigs);
        eventConfigsMap.putAll(fetchedGlobalConfig.getCoreEventsConfigs()); // second! to override tenant configs

        return new Configs(tenantId, fetchedTenantConfigs.enableRealtime,
                fetchedTenantConfigs.enableRealtimeThroughOptistream,
                logsConfigs,
                realtimeConfigs,
                optitrackConfigs,
                eventConfigsMap);
    }

    private static LogsConfigs getLogsConfigs(@NonNull FetchedGlobalConfig fetchedGlobalConfig,
                                              @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        LogsConfigs logsConfigs = new LogsConfigs();
        logsConfigs.setTenantId(fetchedTenantConfigs.optitrackMetaData.getSiteId());
        logsConfigs.setLogsServiceEndpoint(fetchedGlobalConfig.getLogsServiceEndpoint());
        logsConfigs.setProdLogsEnabled(fetchedTenantConfigs.prodLogsEnabled);
        return logsConfigs;
    }

    private static RealtimeConfigs getRealtimeConfigs(@NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        RealtimeConfigs realtimeConfigs = new RealtimeConfigs();
        realtimeConfigs.setRealtimeGateway(fetchedTenantConfigs.realtimeMetaData.getRealtimeGateway());
        realtimeConfigs.setRealtimeToken(fetchedTenantConfigs.realtimeMetaData.getRealtimeToken());
        return realtimeConfigs;
    }

    private static OptitrackConfigs getOptitrackConfigs(int tenantId,
                                                        @NonNull FetchedTenantConfigs fetchedTenantConfigs) {
        OptitrackConfigs optitrackConfigs = new OptitrackConfigs();
        optitrackConfigs.setSiteId(tenantId);
        optitrackConfigs.setOptitrackEndpoint(fetchedTenantConfigs.optitrackMetaData.getOptitrackEndpoint());
        optitrackConfigs.setMaxNumberOfParameters(fetchedTenantConfigs.optitrackMetaData.getMaxActionCustomDimensions());

        return optitrackConfigs;
    }

}
