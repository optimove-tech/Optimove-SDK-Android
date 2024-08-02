package com.optimove.android.preferencecenter;

import java.util.List;

public class PreferenceCenterTopic {
    private final String id;
    private final String name;
    private final String description;
    private final List<OptimovePreferenceCenter.Channel> subscribedChannels;

    PreferenceCenterTopic(String id, String name, String description, List<OptimovePreferenceCenter.Channel> subscribedChannels) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subscribedChannels = subscribedChannels;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<OptimovePreferenceCenter.Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
