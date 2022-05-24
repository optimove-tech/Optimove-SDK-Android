package com.optimove.android.main.sdk_configs.reused_configs;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventConfigs {

    @SerializedName("supportedOnRealTime")
    private final boolean supportedOnRealTime;
    @SerializedName("parameters")
    private final Map<String, Boolean> parameterConfigs;


    public EventConfigs(JSONObject eventConfigs) throws JSONException {
        JSONObject parameters = eventConfigs.getJSONObject("parameters");
        Map<String, Boolean> parameterConfigs = new HashMap<>();

        Iterator<String> paramKeys = parameters.keys();
        while(paramKeys.hasNext()) {
            parameterConfigs.put(paramKeys.next(), true);
        }

        this.supportedOnRealTime = eventConfigs.getBoolean("supportedOnRealTime");
        this.parameterConfigs = parameterConfigs;
    }
    public EventConfigs(boolean supportedOnRealTime,
                        Map<String, Boolean> parameterConfigs) {
        this.supportedOnRealTime = supportedOnRealTime;
        this.parameterConfigs = parameterConfigs;
    }

    public boolean isSupportedOnRealtime() {
        return supportedOnRealTime;
    }


    @NonNull
    public Map<String, Boolean> getParameterConfigs() {
        return parameterConfigs;
    }
}
