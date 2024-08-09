package com.optimove.android.preferencecenter;

public class Config {
    private final String region;
    private final int tenantId;
    private final String brandGroupId;

    public Config(String region, int tenantId, String brandGroupId) {
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
