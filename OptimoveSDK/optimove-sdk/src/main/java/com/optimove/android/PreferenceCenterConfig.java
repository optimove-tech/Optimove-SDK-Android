package com.optimove.android;

import androidx.annotation.Nullable;

public class PreferenceCenterConfig {
    @Nullable
    private String region;

    @Nullable
    private String brandGroupId;

    @Nullable
    private int tenantId;

    public PreferenceCenterConfig() {}

    @Nullable
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBrandGroupId() {
        return brandGroupId;
    }

    public void setBrandGroupId(String brandGroupId) {
        this.brandGroupId = brandGroupId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
