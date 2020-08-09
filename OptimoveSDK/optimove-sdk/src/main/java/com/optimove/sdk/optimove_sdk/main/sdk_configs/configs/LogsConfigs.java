package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

public class LogsConfigs {

    private String logsServiceEndpoint;
    private int tenantId;
    private boolean prodLogsEnabled;

    public String getLogsServiceEndpoint() {
        return logsServiceEndpoint;
    }

    public void setLogsServiceEndpoint(String logsServiceEndpoint) {
        this.logsServiceEndpoint = logsServiceEndpoint;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isProdLogsEnabled() {
        return prodLogsEnabled;
    }

    public void setProdLogsEnabled(boolean prodLogsEnabled) {
        this.prodLogsEnabled = prodLogsEnabled;
    }
}
