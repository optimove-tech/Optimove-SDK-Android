package com.optimove.android.preferencecenter;

import java.util.List;

public class PreferenceUpdate {
    private final String topicId;
    private final List<OptimovePreferenceCenter.Channel> subscribedChannels;

    PreferenceUpdate(String id, List<OptimovePreferenceCenter.Channel> subscribedChannels) {
        this.topicId = id;
        this.subscribedChannels = subscribedChannels;
    }

    public String getId() {
        return topicId;
    }

    public List<OptimovePreferenceCenter.Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
