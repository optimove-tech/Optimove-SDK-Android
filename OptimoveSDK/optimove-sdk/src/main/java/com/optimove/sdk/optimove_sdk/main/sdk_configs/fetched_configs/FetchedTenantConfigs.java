package com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Map;

public class FetchedTenantConfigs {

    @SerializedName("enableRealtime")
    public boolean enableRealtime;
    @SerializedName("enableRealtimeThroughOptistream")
    public boolean enableRealtimeThroughOptistream;
    @SerializedName("supportAirship")
    public boolean supportAirship;
    @SerializedName("prodLogsEnabled")
    public boolean prodLogsEnabled;
    @SerializedName("realtimeMetaData")
    public RealtimeMetaData realtimeMetaData;
    @SerializedName("optitrackMetaData")
    public OptitrackMetaData optitrackMetaData;
    @SerializedName("events")
    public Map<String , EventConfigs> eventsConfigs;

    public class OptitrackMetaData {

        @SerializedName("optitrackEndpoint")
        @Expose
        public String optitrackEndpoint;
        @SerializedName("maxActionCustomDimensions")
        @Expose
        public int maxActionCustomDimensions;
        @SerializedName("siteId")
        @Expose
        public int siteId;

    }

    public class RealtimeMetaData {

        @SerializedName("realtimeToken")
        @Expose
        public String realtimeToken;
        @SerializedName("realtimeGateway")
        @Expose
        public String realtimeGateway;

    }
}