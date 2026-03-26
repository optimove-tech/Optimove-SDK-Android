package com.optimove.android;

import androidx.annotation.NonNull;

public final class AuthTokenException extends Exception {

    public enum Kind {
        TOKEN_FETCH_FAILED("Failed to fetch auth token from provider."),
        NO_USER_ID("No userId available for auth token request.");

        private final String message;

        Kind(String message) {
            this.message = message;
        }
    }

    private final @NonNull Kind kind;

    public AuthTokenException(@NonNull Kind kind) {
        super(kind.message);
        this.kind = kind;
    }

    public @NonNull Kind getKind() {
        return kind;
    }
}
