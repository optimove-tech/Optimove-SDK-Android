package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.CustomDimensionIds;

public class OptitrackConfigs {


    private String eventCategoryName;
    private CustomDimensionIds customDimensionIds;

    private String optitrackEndpoint;
    private int siteId;

    private boolean enableAdvertisingIdReport;

    public String getEventCategoryName() {
        return eventCategoryName;
    }

    public void setEventCategoryName(String eventCategoryName) {
        this.eventCategoryName = eventCategoryName;
    }

    public CustomDimensionIds getCustomDimensionIds() {
        return customDimensionIds;
    }

    public void setCustomDimensionIds(
            CustomDimensionIds customDimensionIds) {
        this.customDimensionIds = customDimensionIds;
    }

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
