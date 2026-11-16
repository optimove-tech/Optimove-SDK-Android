package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public final class OverlayMessagingClosedInfo {

    @NonNull
    private final OverlayMessagingCloseReason reason;

    OverlayMessagingClosedInfo(@NonNull OverlayMessagingCloseReason reason) {
        this.reason = reason;
    }

    @NonNull
    public OverlayMessagingCloseReason getReason() {
        return reason;
    }
}
