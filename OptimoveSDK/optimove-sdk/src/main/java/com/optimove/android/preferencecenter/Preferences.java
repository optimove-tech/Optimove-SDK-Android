package com.optimove.android.preferencecenter;

import java.util.List;

public class Preferences {
    private List<OptimovePreferenceCenter.Channel> configuredChannels;
    private List<Topic> customerPreferences;

    Preferences(List<OptimovePreferenceCenter.Channel> configuredChannels, List<Topic> customerPreferences) {
        this.configuredChannels = configuredChannels;
        this.customerPreferences = customerPreferences;
    }

    public List<OptimovePreferenceCenter.Channel> getConfiguredChannels() {
        return configuredChannels;
    }

    public List<Topic> getCustomerPreferences() {
        return customerPreferences;
    }
}
