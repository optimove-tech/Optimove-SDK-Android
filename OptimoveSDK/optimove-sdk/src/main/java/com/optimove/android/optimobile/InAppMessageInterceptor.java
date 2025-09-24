package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public interface InAppMessageInterceptor {
    
    enum InterceptResult {
        SHOW,
        SUPPRESS
    }
    
    void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback);
}
