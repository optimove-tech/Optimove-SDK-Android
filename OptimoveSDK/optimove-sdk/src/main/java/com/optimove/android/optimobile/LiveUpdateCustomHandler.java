package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Receives Live Update events for hosts that render them themselves, e.g. a home-screen widget or in-app UI.
 * The SDK does not post a notification for types handled here.
 * <p>
 * Invoked on the thread that delivered the update (a background thread for push).
 *
 * @see OptimoveLiveUpdates#registerCustomHandler(String, LiveUpdateCustomHandler)
 */
public interface LiveUpdateCustomHandler {

    void onLiveUpdateEvent(@NonNull Context context,
                           @NonNull LiveUpdateEvent event,
                           @NonNull LiveUpdate liveUpdate);
}
