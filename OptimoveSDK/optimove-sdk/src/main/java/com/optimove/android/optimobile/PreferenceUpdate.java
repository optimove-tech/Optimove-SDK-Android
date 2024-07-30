package com.optimove.android.optimobile;

public class PreferenceUpdate {
    private final String id;
    private final int[] subscribedChannels;

    PreferenceUpdate(String id, int[] subscribedChannels) {
        this.id = id;
        this.subscribedChannels = subscribedChannels;
    }

    public String getId() {
        return id;
    }

    public int[] getSubscribedChannels() {
        return subscribedChannels;
    }
}
