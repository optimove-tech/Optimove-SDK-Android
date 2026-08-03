package com.optimove.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface AuthTokenProvider {

    void getToken(@NonNull String userId, @NonNull Callback callback);

    @FunctionalInterface
    interface Callback {
        void onComplete(@Nullable String token, @Nullable Exception error);
    }
}