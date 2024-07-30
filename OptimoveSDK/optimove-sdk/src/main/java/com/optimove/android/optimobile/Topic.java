package com.optimove.android.optimobile;

public class Topic {
    private final String id;
    private final String name;
    private final String description;
    private final int[] subscribedChannels;

    Topic(String id, String name, String description, int[] subscribedChannels) {
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

    public int[] getSubscribedChannels() {
        return subscribedChannels;
    }
}
