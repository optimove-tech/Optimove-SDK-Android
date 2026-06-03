package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface OverlayMessagingActionHandlerInterface {
    void handle(Context context, OverlayMessagingMessage message, OverlayAction action);

    enum OverlayActionType {
        DEEP_LINK_BUTTON_CLICK
    }

    class OverlayAction {
        private final OverlayActionType type;
        @Nullable private final JSONObject data;

        OverlayAction(OverlayActionType type, @Nullable JSONObject data) {
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
