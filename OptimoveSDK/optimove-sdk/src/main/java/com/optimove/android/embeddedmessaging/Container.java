package com.optimove.android.embeddedmessaging;

import androidx.annotation.Nullable;

public class Container {
    private String containerId;
    private EmbeddedMessage[] messages;

    public Container(String containerId, @Nullable EmbeddedMessage[] messages) {
        this.containerId = containerId;
        this.messages = messages;
    }

    public String getContainerId() {
        return this.getContainerId();
    }

    public EmbeddedMessage[] getMessages() {
        return this.messages;
    }
}
