package com.optimove.android.optimobile;

import android.content.Context;

public abstract class OverlayMessagingActionHandler {
    /**
     * Called when a link-button CTA is tapped in an overlay message.
     *
     * @return true to consume the action (SDK will not open the URL);
     *         false to let the SDK open the URL as default.
     */
    public boolean onLinkAction(Context context, OverlayMessagingMessage message, LinkActionPayload payload) {
        return false;
    }
}
