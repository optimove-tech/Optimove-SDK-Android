package com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Map;

public class FetchedTenantConfigs {

    @SerializedName("realtimeMetaData")
    public RealtimeMetaData realtimeMetaData;
    @SerializedName("optitrackMetaData")
    public OptitrackMetaData optitrackMetaData;
    @SerializedName("mobile")
    public Mobile mobile;
    @SerializedName("events")
    public Map<String , EventConfigs> eventsConfigs;

    public class Mobile {

        @SerializedName("optipushMetaData")
        public OptipushMetaData optipushMetaData;
        @SerializedName("firebaseProjectKeys")
        public FirebaseProjectKeys firebaseProjectKeys;
        @SerializedName("clientsServiceProjectKeys")
        public FirebaseProjectKeys clientsServiceProjectKeys;

    }

    public class OptipushMetaData {

        @SerializedName("enableAdvertisingIdReport")
        @Expose
        public Boolean enableAdvertisingIdReport;
        @SerializedName("pushTopicsRegistrationEndpoint")
        @Expose
        public String pushTopicsRegistrationEndpoint;

    }
    public class OptitrackMetaData {

        @SerializedName("optitrackEndpoint")
        @Expose
        public String optitrackEndpoint;
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
        @SerializedName("options")
        @Expose
        public Options options;

    }
    public class Options {

        @SerializedName("showDimmer")
        @Expose
        public Boolean showDimmer;
        @SerializedName("showWatermark")
        @Expose
        public Boolean showWatermark;
        @SerializedName("popupCallback")
        @Expose
        public Object popupCallback;

    }
    public class FirebaseProjectKeys {

        @SerializedName("appIds")
        @Expose
        public AppIds appIds;
        @SerializedName("webApiKey")
        @Expose
        public String webApiKey;
        @SerializedName("dbUrl")
        @Expose
        public String dbUrl;
        @SerializedName("senderId")
        @Expose
        public String senderId;
        @SerializedName("storageBucket")
        @Expose
        public String storageBucket;
        @SerializedName("projectId")
        @Expose
        public String projectId;

    }

    public class AppIds {
        @SerializedName("android")
        @Expose
        public Map<String , String> androidAppIds;

    }
}