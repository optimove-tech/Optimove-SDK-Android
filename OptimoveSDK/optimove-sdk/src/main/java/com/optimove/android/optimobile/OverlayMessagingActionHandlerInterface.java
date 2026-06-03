package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface OverlayMessagingActionHandlerInterface {
    void handle(Context context, OverlayAction action);

    enum OverlayActionType {
        DEEP_LINK_BUTTON_CLICK
    }

    class OverlayAction {
        public final OverlayMessagingMessage message;
        private final OverlayActionType type;
        @Nullable private final JSONObject data;

        OverlayAction(OverlayMessagingMessage message, OverlayActionType type, @Nullable JSONObject data) {
            this.message = message;
            this.type = type;
            this.data = data;
        }

        public OverlayActionType getType() {
            return type;
        }

        @Nullable
        public JSONObject getData() {
            return data;
        }
    }
}
