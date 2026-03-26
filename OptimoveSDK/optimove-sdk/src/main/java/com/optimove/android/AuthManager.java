package com.optimove.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AuthManager {

    private final @NonNull AuthTokenProvider provider;

    public AuthManager(@NonNull AuthTokenProvider provider) {
        this.provider = provider;
    }

    public void getToken(@Nullable String userId, @NonNull AuthTokenProvider.Callback completion) {
        if (userId == null) {
            completion.onComplete(null, new AuthTokenException(AuthTokenException.Kind.NO_USER_ID));
            return;
        }
        String id = userId.trim();
        if (id.isEmpty()) {
            completion.onComplete(null, new AuthTokenException(AuthTokenException.Kind.NO_USER_ID));
            return;
        }
        provider.getToken(id, (token, error) -> {
            if (token != null) {
                completion.onComplete(token, null);
            } else {
                completion.onComplete(null, error != null ? error : new AuthTokenException(AuthTokenException.Kind.TOKEN_FETCH_FAILED));
            }
        });
    }
}
