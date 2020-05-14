package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

public class OptitrackConfigs {


    private String optitrackEndpoint;
    private int siteId;

    private boolean enableAdvertisingIdReport;


    public String getOptitrackEndpoint() {
        return optitrackEndpoint;
    }

    public void setOptitrackEndpoint(String optitrackEndpoint) {
        this.optitrackEndpoint = optitrackEndpoint;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public boolean isEnableAdvertisingIdReport() {
        return enableAdvertisingIdReport;
    }

    public void setEnableAdvertisingIdReport(boolean enableAdvertisingIdReport) {
        this.enableAdvertisingIdReport = enableAdvertisingIdReport;
    }
}
