package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class OverlayMessagingMessage {

    public enum MessageType {
        SESSION,
        IMMEDIATE
    }

    private final long id;
    private final JSONObject content;
    private final MessageType type;

    OverlayMessagingMessage(long id, @NonNull JSONObject content, @NonNull MessageType type) {
        this.id = id;
        this.content = content;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public JSONObject getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }
}
