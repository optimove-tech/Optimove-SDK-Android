package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

/**
 * Callback interface for in-app message display filter results.
 * 
 * This callback is used to return the result of message filtering logic
 * to the SDK. The callback can be invoked from any thread.
 */
public interface InAppMessageFilterCallback {
    
    /**
     * Called to provide the filter result for a message.
     * 
     * <p>This method can be called from any thread. The SDK will handle
     * switching to the appropriate thread for message display.</p>
     * 
     * @param result The filter result indicating whether to show or suppress the message
     */
    void onFilterResult(@NonNull InAppMessageDisplayFilter.FilterResult result);
}
