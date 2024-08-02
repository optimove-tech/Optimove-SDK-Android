package com.optimove.android.preferencecenter;

import java.util.List;

public class Preferences {
    private List<OptimovePreferenceCenter.Channel> configuredChannels;
    private List<PreferenceCenterTopic> customerPreferences;

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
