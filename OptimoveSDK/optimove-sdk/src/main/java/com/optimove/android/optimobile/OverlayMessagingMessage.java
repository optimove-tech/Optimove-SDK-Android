package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class OverlayMessagingMessage {

    public enum MessageType {
        SESSION,
        IMMEDIATE
    }

    private final long id;
    private final JSONObject content;
    private final JSONObject data;
    private final MessageType type;

    OverlayMessagingMessage(long id, @NonNull JSONObject content, @Nullable JSONObject data, @NonNull MessageType type) {
        this.id = id;
        this.content = content;
        this.data = data;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public JSONObject getContent() {
        return content;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    public MessageType getType() {
        return type;
    }
}
