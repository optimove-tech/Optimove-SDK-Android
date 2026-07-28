package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public final class LinkActionPayload {
    @NonNull public final String url;

    LinkActionPayload(@NonNull String url) {
        this.url = url;
    }
}
