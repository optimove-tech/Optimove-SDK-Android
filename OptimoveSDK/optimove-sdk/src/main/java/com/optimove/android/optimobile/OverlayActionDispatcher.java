package com.optimove.android.optimobile;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

class OverlayActionDispatcher {

    private static final String TAG = "OverlayActionDispatcher";

    enum ActionType {
        LINK_ACTION
    }

    @Nullable
    private OverlayMessagingActionHandler handler;

    void setHandler(@Nullable OverlayMessagingActionHandler handler) {
        this.handler = handler;
    }

    /** Returns true if consumed (SDK must not run its default). */
    boolean dispatch(ActionType type, Context context, OverlayMessagingMessage message, JSONObject data) {
        if (handler == null) return false;
        try {
            switch (type) {
                case LINK_ACTION:
                    return handler.onLinkAction(context, message, new LinkActionPayload(data.optString("url")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Overlay action handler threw", e);
            return true; // fail-closed
        }
        return false;
    }
}
