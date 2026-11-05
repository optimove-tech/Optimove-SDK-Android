package com.optimove.android.optimobile;

import androidx.annotation.Nullable;

public enum LiveUpdateEvent {
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
