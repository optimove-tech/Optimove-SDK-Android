package com.optimove.android.optimobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

public abstract class OverlayMessagingActionHandler {

    /**
     * Called on the main thread when a link-button CTA is tapped in an overlay message.
     * <p>
     * The default implementation opens {@link LinkActionPayload#url} via the system browser.
     * Override to handle navigation yourself instead — your implementation fully replaces
     * the default; the SDK will not open the URL.
     * <p>
     * If this method throws, the exception is logged and the SDK does <b>not</b> fall back
     * to opening the URL (fail-closed).
     *
     * @param context The activity context. Use to start activities or access resources.
     * @param message The overlay message that triggered the action.
     * @param payload The typed action payload; {@link LinkActionPayload#url} is the target URL.
     */
    @UiThread
    public void onLinkAction(@NonNull Context context, @NonNull OverlayMessagingMessage message, @NonNull LinkActionPayload payload) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payload.url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
