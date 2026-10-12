package com.optimove.android.main.sdk_configs.configs;

import androidx.annotation.Nullable;

public class GamifyConfigs {

    @Nullable
    private String adactUrl;
    @Nullable
    private String loyaltyWidgetUrl;

    @Nullable
    public String getAdactUrl() {
        return adactUrl;
    }

    public void setAdactUrl(@Nullable String adactUrl) {
        this.adactUrl = adactUrl;
    }

    @Nullable
    public String getLoyaltyWidgetUrl() {
        return loyaltyWidgetUrl;
    }

    public void setLoyaltyWidgetUrl(@Nullable String loyaltyWidgetUrl) {
        this.loyaltyWidgetUrl = loyaltyWidgetUrl;
    }
}
