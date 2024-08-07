package com.optimove.android;

import androidx.annotation.Nullable;

public class PreferenceCenterConfig {
    private final String region;
    private final int tenantId;
    private final String brandGroupId;

    public PreferenceCenterConfig(String region, int tenantId, String brandGroupId) {
        this.region = region;
        this.brandGroupId = brandGroupId;
        this.tenantId = tenantId;
    }

    public String getRegion() {
        return region;
    }

    public String getBrandGroupId() {
        return brandGroupId;
    }

    public int getTenantId() {
        return tenantId;
    }
}
