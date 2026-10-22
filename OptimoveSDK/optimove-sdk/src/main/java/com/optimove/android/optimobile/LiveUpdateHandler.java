package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Receives Live Update events. Called on a background thread; the host owns rendering.
 */
public interface LiveUpdateHandler {

    void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate);
}
