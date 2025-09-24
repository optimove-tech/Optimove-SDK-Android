package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

/**
 * Interface for intercepting in-app messages when display mode is set to INTERCEPTED.
 * Allows custom logic to determine whether to show or suppress messages.
 */
public interface InAppMessageInterceptor {
    
    enum InterceptResult {
        SHOW,      // Show the message
        SUPPRESS   // Suppress the message
    }
    
    /**
     * Called when an in-app message is about to be displayed.
     * This method runs on a background thread.
     * 
     * You MUST call the callback within 5 seconds or the message will be suppressed.
     * 
     * @param message Information about the in-app message
     * @param callback Callback to invoke with your decision (can be called from any thread)
     */
    void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback);
}
