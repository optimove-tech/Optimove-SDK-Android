package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

public interface InAppMessageInterceptor {
    void processMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback);
}
