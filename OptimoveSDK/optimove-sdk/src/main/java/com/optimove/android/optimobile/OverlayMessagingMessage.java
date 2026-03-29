package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public class OverlayMessagingMessage {
    public final long id;
    public final String html;

    OverlayMessagingMessage(long id, @NonNull String html) {
        this.id = id;
        this.html = html;
    }
}
