package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

import org.json.JSONObject;

class OverlayMessagingMessage {
    final long id;
    final JSONObject content;

    OverlayMessagingMessage(long id, @NonNull JSONObject content) {
        this.id = id;
        this.content = content;
    }

    public JSONObject getContent() {
        return content;
    }
}
