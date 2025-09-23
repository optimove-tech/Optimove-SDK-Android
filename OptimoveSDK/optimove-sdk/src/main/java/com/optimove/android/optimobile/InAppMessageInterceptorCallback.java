package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

/**
 * Callback interface for in-app message interceptor results.
 * 
 * This callback is used to return the result of message interception logic
 * to the SDK. The callback can be invoked from any thread.
 */
public interface InAppMessageInterceptorCallback {
    
    /**
     * Called to provide the intercept result for a message.
     * 
     * <p>This method can be called from any thread. The SDK will handle
     * switching to the appropriate thread for message display.</p>
     * 
     * @param result The intercept result indicating whether to show or suppress the message
     */
    void onInterceptResult(@NonNull InAppMessageInterceptor.InterceptResult result);
}
