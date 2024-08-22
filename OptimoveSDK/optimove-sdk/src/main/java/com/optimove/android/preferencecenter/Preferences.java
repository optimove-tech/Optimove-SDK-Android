package com.optimove.android.preferencecenter;

import java.util.List;

public class Preferences {
    private final List<Channel> configuredChannels;
    private final List<Topic> customerPreferences;

    Preferences(List<Channel> configuredChannels, List<Topic> customerPreferences) {
        this.configuredChannels = configuredChannels;
        this.customerPreferences = customerPreferences;
    }

    public List<Channel> getConfiguredChannels() {
        return configuredChannels;
    }

    public List<Topic> getCustomerPreferences() {
        return customerPreferences;
    }
}
