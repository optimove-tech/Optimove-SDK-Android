package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.Nullable;

public interface OverlayMessagingActionHandlerInterface {
    void handle(Context context, OverlayAction action);

    enum OverlayActionType {
        BUTTON_CLICK
    }

    class OverlayAction {
        private final OverlayActionType type;
        @Nullable
        private final String data;

        OverlayAction(OverlayActionType type, @Nullable String data) {
            this.type = type;
            this.data = data;
        }

        public OverlayActionType getType() {
            return type;
        }

        @Nullable
        public String getData() {
            return data;
        }
    }
}
