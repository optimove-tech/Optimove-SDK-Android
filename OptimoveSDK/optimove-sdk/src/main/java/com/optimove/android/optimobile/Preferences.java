package com.optimove.android.optimobile;

public class Preferences {
    private final int[] configuredChannels;
    private final Topic[] customerPreferences;

    Preferences(int[] configuredChannels, Topic[] customerPreferences) {
        this.configuredChannels = configuredChannels;
        this.customerPreferences = customerPreferences;
    }

    public int[] getConfiguredChannels() {
        return configuredChannels;
    }

    public Topic[] getCustomerPreferences() {
        return customerPreferences;
    }
}
