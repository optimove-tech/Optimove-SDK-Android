package com.optimove.android.optimobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

public abstract class OverlayMessagingActionHandler {
    /**
     * Called when a link-button CTA is tapped in an overlay message.
     * The default implementation opens the URL via the system browser.
     * Override to handle navigation yourself instead.
     */
    public void onLinkAction(@NonNull Context context, @NonNull OverlayMessagingMessage message, @NonNull LinkActionPayload payload) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payload.url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
