package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

/**
 * Callback for returning interceptor decisions to the SDK.
 */
public interface InAppMessageInterceptorCallback {
    
    /**
     * Reports the intercept decision. Can be called from any thread.
     * 
     * @param result SHOW or SUPPRESS the message
     */
    void onInterceptResult(@NonNull InAppMessageInterceptor.InterceptResult result);
}
