package com.optimove.android.main.sdk_configs.fetched_configs;

import com.optimove.android.main.sdk_configs.reused_configs.EventConfigs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FetchedGlobalConfig {

    private final String logsServiceEndpoint;
    private final Map<String , EventConfigs> coreEventsConfigs;

    public FetchedGlobalConfig(String logsServiceEndpoint,
                               Map<String, EventConfigs> coreEventsConfigs) {
        this.logsServiceEndpoint = logsServiceEndpoint;
        this.coreEventsConfigs = coreEventsConfigs;
    }

    public FetchedGlobalConfig(JSONObject globalConfig) throws JSONException {
        JSONObject generalConfigs = globalConfig.getJSONObject("general");
        String logsEndpoint = generalConfigs.getString("logs_service_endpoint");
        JSONObject coreEvents = globalConfig.getJSONObject("core_events");
        Map<String, EventConfigs> coreEventsConfigs = new HashMap<>();

        Iterator<String> eventKeys = coreEvents.keys();

        while(eventKeys.hasNext()) {
            String eventName = eventKeys.next();
            JSONObject eventConfigs = coreEvents.getJSONObject(eventName);

            coreEventsConfigs.put(eventName, new EventConfigs(eventConfigs));
        }

        this.logsServiceEndpoint = logsEndpoint;
        this.coreEventsConfigs = coreEventsConfigs;
    }

    public String getLogsServiceEndpoint() {
        return logsServiceEndpoint;
    }

    public Map<String, EventConfigs> getCoreEventsConfigs() {
        return coreEventsConfigs;
    }
 }
