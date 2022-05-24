package com.optimove.android.main.sdk_configs.fetched_configs;

import com.google.gson.annotations.SerializedName;
import com.optimove.android.main.sdk_configs.reused_configs.EventConfigs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FetchedTenantConfigs {

    @SerializedName("enableRealtime")
    public boolean enableRealtime;
    @SerializedName("enableRealtimeThroughOptistream")
    public boolean enableRealtimeThroughOptistream;
    @SerializedName("prodLogsEnabled")
    public boolean prodLogsEnabled;
    @SerializedName("realtimeMetaData")
    public RealtimeMetaData realtimeMetaData;
    @SerializedName("optitrackMetaData")
    public OptitrackMetaData optitrackMetaData;
    @SerializedName("events")
    public Map<String , EventConfigs> eventsConfigs;

    public FetchedTenantConfigs(JSONObject config) throws JSONException {
        this.enableRealtime = config.getBoolean("enableRealtime");
        this.enableRealtimeThroughOptistream = config.getBoolean("enableRealtimeThroughOptistream");
        this.prodLogsEnabled = config.getBoolean("prodLogsEnabled");
        this.realtimeMetaData = new RealtimeMetaData(config.getJSONObject("realtimeMetaData")
                .getString(
                        "realtimeToken"), config.getJSONObject("realtimeMetaData")
                .getString("realtimeGateway"));
        this.optitrackMetaData = new OptitrackMetaData(config.getJSONObject("optitrackMetaData").getString(
                "optitrackEndpoint"), config.getJSONObject("optitrackMetaData").getInt(
                "maxActionCustomDimensions"), config.getJSONObject("optitrackMetaData").getInt(
                "siteId"));

        JSONObject events = config.getJSONObject("event");
        Map<String, EventConfigs> eventsConfigs = new HashMap<>();

        Iterator<String> eventKeys = events.keys();

        while(eventKeys.hasNext()) {
            String eventName = eventKeys.next();
            JSONObject eventConfigs = events.getJSONObject(eventName);

            eventsConfigs.put(eventName, new EventConfigs(eventConfigs));
        }

        this.eventsConfigs = eventsConfigs;
    }

    public static class OptitrackMetaData {

        private final String optitrackEndpoint;
        private final int maxActionCustomDimensions;
        private final int siteId;

        public OptitrackMetaData(String optitrackEndpoint, int maxActionCustomDimensions, int siteId) {
            this.optitrackEndpoint = optitrackEndpoint;
            this.maxActionCustomDimensions = maxActionCustomDimensions;
            this.siteId = siteId;
        }

        public String getOptitrackEndpoint() {
            return optitrackEndpoint;
        }

        public int getMaxActionCustomDimensions() {
            return maxActionCustomDimensions;
        }

        public int getSiteId() {
            return siteId;
        }
    }

    public static class RealtimeMetaData {

        private final String realtimeToken;
        private final String realtimeGateway;

        public RealtimeMetaData(String realtimeToken, String realtimeGateway) {
            this.realtimeToken = realtimeToken;
            this.realtimeGateway = realtimeGateway;
        }

        public String getRealtimeToken() {
            return realtimeToken;
        }

        public String getRealtimeGateway() {
            return realtimeGateway;
        }
    }
}