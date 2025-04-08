package com.optimove.android.embeddedmessaging;

import androidx.annotation.Nullable;

public class Container {
    private String containerId;
    private String title;
    private EmbeddedMessage[] messages;

    public Container(String containerId, String title, @Nullable EmbeddedMessage[] messages) {
        this.containerId = containerId;
        this.title = title;
        this.messages = messages;
    }

    public String getContainerId() {
        return this.getContainerId();
    }

    public String getTitle() {
       return this.title;
    }

    public EmbeddedMessage[] getMessages() {
        return this.messages;
    }
}
