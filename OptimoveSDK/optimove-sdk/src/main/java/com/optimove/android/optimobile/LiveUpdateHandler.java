package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Receives Live Update events. The host owns rendering.
 * <p>
 * Called on the FCM service thread for pushes, on the SDK's background executor for notification dismissals and
 * the startup expiry sweep, and on the calling thread for {@link OptimoveLiveUpdates#end(String)} and
 * {@link OptimoveLiveUpdates#clearAll()}. Never on the main thread unless the host calls those two from it.
 */
public interface LiveUpdateHandler {

    void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate);
}
