package com.optimove.android.preferencecenter;

import java.util.List;

public class Topic {
    private final String id;
    private final String name;
    private final String description;
    private final List<Channel> subscribedChannels;

    Topic(String id, String name, String description, List<Channel> subscribedChannels) {
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

    public List<Channel> getSubscribedChannels() {
        return subscribedChannels;
    }
}
