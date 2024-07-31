package com.optimove.android.optimobile;

public class Preferences {
    private final OptimovePreferenceCenter.Channel[] configuredChannels;
    private final Topic[] customerPreferences;

    Preferences(OptimovePreferenceCenter.Channel[] configuredChannels, Topic[] customerPreferences) {
        this.configuredChannels = configuredChannels;
        this.customerPreferences = customerPreferences;
    }

    public OptimovePreferenceCenter.Channel[] getConfiguredChannels() {
        return configuredChannels;
    }

    public Topic[] getCustomerPreferences() {
        return customerPreferences;
    }
}
