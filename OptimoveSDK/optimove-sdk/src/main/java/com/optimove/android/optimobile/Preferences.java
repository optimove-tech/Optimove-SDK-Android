package com.optimove.android.optimobile;

import java.util.List;

public class Preferences {
    private final List<OptimovePreferenceCenter.Channel> configuredChannels;
    private final List<PreferenceCenterTopic> customerPreferences;

    Preferences(List<OptimovePreferenceCenter.Channel> configuredChannels, List<PreferenceCenterTopic> customerPreferences) {
        this.configuredChannels = configuredChannels;
        this.customerPreferences = customerPreferences;
    }

    public List<OptimovePreferenceCenter.Channel> getConfiguredChannels() {
        return configuredChannels;
    }

    public List<PreferenceCenterTopic> getCustomerPreferences() {
        return customerPreferences;
    }
}
