package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface InAppMessageInterceptor {
    void processMessage(@Nullable JSONObject messageData, @NonNull InAppMessageInterceptorCallback callback);
}
