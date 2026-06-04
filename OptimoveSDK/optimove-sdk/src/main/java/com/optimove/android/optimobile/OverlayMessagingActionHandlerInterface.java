package com.optimove.android.optimobile;

import android.content.Context;

import org.json.JSONObject;

public interface OverlayMessagingActionHandlerInterface {
    void handle(Context context, OverlayMessagingMessage message, JSONObject data);

    enum OverlayActionType {
        LINK_ACTION
    }
}
