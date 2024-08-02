package com.optimove.android.preferencecenter;

import java.util.List;

public class PreferenceUpdate {
    private final String id;
    private final List<OptimovePreferenceCenter.Channel> subscribedChannels;

    PreferenceUpdate(String id, List<OptimovePreferenceCenter.Channel> subscribedChannels) {
        this.id = id;
        this.subscribedChannels = subscribedChannels;
    }

    public String getId() {
        return id;
    }

    public List<OptimovePreferenceCenter.Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
