package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Receives Live Update events. The host owns rendering.
 * <p>
 * Called on the FCM service thread for pushes and on the SDK's background executor for notification dismissals,
 * the startup expiry sweep, {@link OptimoveLiveUpdates#end(String)} and {@link OptimoveLiveUpdates#clearAll()}.
 * Never on the main thread.
 */
public interface LiveUpdateHandler {

    void onUpdate(@NonNull Context context, @NonNull LiveUpdateEvent event, @NonNull LiveUpdate liveUpdate);
}
