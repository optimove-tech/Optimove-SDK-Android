package com.optimove.android.main.sdk_configs.fetched_configs;

import com.google.gson.annotations.SerializedName;
import com.optimove.android.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Map;

public class FetchedGlobalConfig {

    @SerializedName("general")
    public FetchedGeneralConfigs fetchedGeneralConfigs;
    @SerializedName("core_events")
    public Map<String , EventConfigs> coreEventsConfigs;


    public class FetchedGeneralConfigs {
        @SerializedName("logs_service_endpoint")
        public String logsServiceEndpoint;
    }

}
