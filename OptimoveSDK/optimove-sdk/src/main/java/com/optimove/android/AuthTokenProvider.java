package com.optimove.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface AuthTokenProvider {

    void getToken(@NonNull String userId, @NonNull Callback callback);

    interface Callback {
        void onComplete(@Nullable String token, @Nullable Exception error);
    }
}
