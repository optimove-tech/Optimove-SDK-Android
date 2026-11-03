package com.optimove.android.gamifywidgetsdk;

import androidx.annotation.Nullable;

/**
 * Parameters for opening the loyalty widget from the configured {@code loyaltyWidgetUrl}.
 *
 * <p>Maps to the Web SDK {@code openWidget({ userId, brand })} contract:
 * {@code {loyaltyWidgetUrl}/{tenantId}[/brand/{brand}]/{userId}}
 */
public class OpenLoyaltyWidgetParams {

    @Nullable public String userId;
    @Nullable public String brand;

    public OpenLoyaltyWidgetParams() {
    }

    public OpenLoyaltyWidgetParams(@Nullable String userId, @Nullable String brand) {
        this.userId = userId;
        this.brand = brand;
    }
}
