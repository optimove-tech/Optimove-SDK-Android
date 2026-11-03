package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public final class OverlayMessagingClosedInfo {

    @NonNull
    private final String reason;

    OverlayMessagingClosedInfo(@NonNull String reason) {
        this.reason = reason;
    }

    /**
     * One of the {@link OverlayMessagingCloseReason} constants. New values may be added in future
     * releases; treat any unrecognised value as "closed".
     */
    @NonNull
    public String getReason() {
        return reason;
    }
}
