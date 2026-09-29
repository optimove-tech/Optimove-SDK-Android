package com.optimove.android.optimobile;

import androidx.annotation.Nullable;

/**
 * The lifecycle event that caused a Live Update handler to be invoked.
 */
public enum LiveUpdateEvent {
    /**
     * First time this device sees the activity. Also used when the original start was missed
     * (e.g. collapsed by FCM while offline) and an update arrives for an unknown activity.
     */
    START,
    UPDATE,
    END;

    static @Nullable LiveUpdateEvent fromWireValue(@Nullable String value) {
        if ("start".equals(value)) {
            return START;
        }
        if ("update".equals(value)) {
            return UPDATE;
        }
        if ("end".equals(value)) {
            return END;
        }
        return null;
    }
}
