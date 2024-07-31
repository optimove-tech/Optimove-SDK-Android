package com.optimove.android.optimobile;

public class PreferenceUpdate {
    private final String id;
    private final OptimovePreferenceCenter.Channel[] subscribedChannels;

    PreferenceUpdate(String id, OptimovePreferenceCenter.Channel[] subscribedChannels) {
        this.id = id;
        this.subscribedChannels = subscribedChannels;
    }

    public String getId() {
        return id;
    }

    public OptimovePreferenceCenter.Channel[] getSubscribedChannels() {
        return subscribedChannels;
    }
}
