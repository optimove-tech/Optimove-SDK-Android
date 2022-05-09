package com.optimove.android.main.sdk_configs.configs;

public class OptitrackConfigs {


    private String optitrackEndpoint;
    private int siteId;

    private int maxNumberOfParameters;


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

    public int getMaxNumberOfParameters() {
        return maxNumberOfParameters;
    }

    public void setMaxNumberOfParameters(int maxNumberOfParameters) {
        this.maxNumberOfParameters = maxNumberOfParameters;
    }
}
