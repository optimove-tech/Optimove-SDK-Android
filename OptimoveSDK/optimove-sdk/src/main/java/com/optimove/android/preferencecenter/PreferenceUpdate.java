package com.optimove.android.preferencecenter;

import java.util.List;

public class PreferenceUpdate {
    private final String topicId;
    private final List<Channel> subscribedChannels;

    public PreferenceUpdate(String topicId, List<Channel> subscribedChannels) {
        this.topicId = topicId;
        this.subscribedChannels = subscribedChannels;
    }

    public String getTopicId() {
        return topicId;
    }

    public List<Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
