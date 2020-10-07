package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

import androidx.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Map;

public class Configs {


    private int tenantId;
    private boolean enableRealtime;
    private boolean enableRealtimeThroughOptistream;
    private boolean airship;
    private LogsConfigs logsConfigs;
    private RealtimeConfigs realtimeConfigs;
    private OptitrackConfigs optitrackConfigs;
    private OptipushConfigs optipushConfigs;
    private Map<String, EventConfigs> eventsConfigs;

    public Configs(int tenantId, boolean enableRealtime,
                   boolean enableRealtimeThroughOptistream,
                   boolean airshipSupported,
                   @NonNull LogsConfigs logsConfigs,
                   @NonNull RealtimeConfigs realtimeConfigs,
                   @NonNull OptitrackConfigs optitrackConfigs,
                   @NonNull OptipushConfigs optipushConfigs,
                   @NonNull Map<String, EventConfigs> eventsConfigs) {
        this.tenantId = tenantId;
        this.enableRealtime = enableRealtime;
        this.enableRealtimeThroughOptistream = enableRealtimeThroughOptistream;
        this.airship = airshipSupported;
        this.logsConfigs = logsConfigs;
        this.realtimeConfigs = realtimeConfigs;
        this.optitrackConfigs = optitrackConfigs;
        this.optipushConfigs = optipushConfigs;
        this.eventsConfigs = eventsConfigs;
    }

    public LogsConfigs getLogsConfigs() {
        return logsConfigs;
    }

    public void setLogsConfigs(@NonNull LogsConfigs logsConfigs) {
        this.logsConfigs = logsConfigs;
    }

    public RealtimeConfigs getRealtimeConfigs() {
        return realtimeConfigs;
    }

    public void setRealtimeConfigs(@NonNull RealtimeConfigs realtimeConfigs) {
        this.realtimeConfigs = realtimeConfigs;
    }

    public OptitrackConfigs getOptitrackConfigs() {
        return optitrackConfigs;
    }

    public void setOptitrackConfigs(@NonNull OptitrackConfigs optitrackConfigs) {
        this.optitrackConfigs = optitrackConfigs;
    }

    public OptipushConfigs getOptipushConfigs() {
        return optipushConfigs;
    }

    public void setOptipushConfigs(@NonNull OptipushConfigs optipushConfigs) {
        this.optipushConfigs = optipushConfigs;
    }

    public Map<String, EventConfigs> getEventsConfigs() {
        return eventsConfigs;
    }

    public void setEventsConfigs(
            @NonNull Map<String, EventConfigs> eventsConfigs) {
        this.eventsConfigs = eventsConfigs;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isEnableRealtime() {
        return enableRealtime;
    }

    public void setEnableRealtime(boolean enableRealtime) {
        this.enableRealtime = enableRealtime;
    }

    public boolean isEnableRealtimeThroughOptistream() {
        return enableRealtimeThroughOptistream;
    }

    public void setEnableRealtimeThroughOptistream(boolean enableRealtimeThroughOptistream) {
        this.enableRealtimeThroughOptistream = enableRealtimeThroughOptistream;
    }

    public boolean isAirship() {
        return airship;
    }

    public void setAirship(boolean airship) {
        this.airship = airship;
    }
}
