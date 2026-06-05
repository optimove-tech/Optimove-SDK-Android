package com.optimove.android.optimobile;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.json.JSONObject;

class OverlayActionDispatcher {

    private static final String TAG = "OverlayActionDispatcher";

    enum ActionType {
        LINK_ACTION
    }

    @NonNull
    private OverlayMessagingActionHandler handler = new OverlayMessagingActionHandler() {};

    @UiThread
    void setHandler(@Nullable OverlayMessagingActionHandler handler) {
        this.handler = handler != null ? handler : new OverlayMessagingActionHandler() {};
    }

    @UiThread
    void dispatch(ActionType type, @NonNull Context context, @NonNull OverlayMessagingMessage message, @NonNull JSONObject data) {
        try {
            switch (type) {
                case LINK_ACTION:
                    String url = data.isNull("url") ? "" : data.optString("url", "");
                    handler.onLinkAction(context, message, new LinkActionPayload(url));
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Overlay action handler threw", e);
        }
    }
}
