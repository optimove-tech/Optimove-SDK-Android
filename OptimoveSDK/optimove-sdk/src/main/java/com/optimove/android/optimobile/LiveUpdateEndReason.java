package com.optimove.android.optimobile;

import java.util.Locale;

public enum LiveUpdateEndReason {
    SERVER,
    LOCAL,
    EXPIRED,
    CLEARED;

    String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
