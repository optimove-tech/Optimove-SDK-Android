package com.optimove.android.preferencecenter;

import java.util.List;

public class PreferenceUpdate {
    private final String topicId;
    private final List<OptimovePreferenceCenter.Channel> subscribedChannels;

    public PreferenceUpdate(String topicId, List<OptimovePreferenceCenter.Channel> subscribedChannels) {
        this.topicId = topicId;
        this.subscribedChannels = subscribedChannels;
    }

    public String getTopicId() {
        return topicId;
    }

    public List<OptimovePreferenceCenter.Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
