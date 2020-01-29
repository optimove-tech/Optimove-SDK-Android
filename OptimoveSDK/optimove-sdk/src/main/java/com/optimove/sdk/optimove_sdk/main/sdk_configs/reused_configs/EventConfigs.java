package com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class EventConfigs {

    @SerializedName("id")
    private int id;
    @SerializedName("supportedOnOptitrack")
    private boolean supportedOnOptitrack;
    @SerializedName("supportedOnRealTime")
    private boolean supportedOnRealTime;
    @SerializedName("parameters")
    private Map<String, ParameterConfig> parameterConfigs;

    public EventConfigs() {
    }

    public int getId() {
        return id;
    }

    public boolean isSupportedOnOptitrack() {
        return supportedOnOptitrack;
    }

    public boolean isSupportedOnRealtime() {
        return supportedOnRealTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSupportedOnOptitrack(boolean supportedOnOptitrack) {
        this.supportedOnOptitrack = supportedOnOptitrack;
    }


    public void setSupportedOnRealTime(boolean supportedOnRealTime) {
        this.supportedOnRealTime = supportedOnRealTime;
    }

    public void setParameterConfigs(
            Map<String, ParameterConfig> parameterConfigs) {
        this.parameterConfigs = parameterConfigs;
    }

    @NonNull
    public Map<String, ParameterConfig> getParameterConfigs() {
        return parameterConfigs;
    }

    public class ParameterConfig {

        @SerializedName("type")
        private String type;
        @SerializedName("optiTrackDimensionId")
        private int dimensionId;
        @SerializedName("optional")
        private boolean isOptional;

        public ParameterConfig() {
        }

        @NonNull
        public String getType() {
            return type;
        }

        public int getDimensionId() {
            return dimensionId;
        }

        public boolean isOptional() {
            return isOptional;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDimensionId(int dimensionId) {
            this.dimensionId = dimensionId;
        }

        public void setOptional(boolean optional) {
            isOptional = optional;
        }
    }



}
