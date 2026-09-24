package com.optimove.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Supplies JWTs for federated authentication. Register with
 * {@link OptimoveConfig.Builder#enableAuth(AuthTokenProvider)}.
 * <p>
 * Contract:
 * <ul>
 *     <li>Invoke {@link Callback#onComplete(String, Exception)} exactly once per call: {@code (token, null)} on
 *     success or {@code (null, error)} on failure. Call it on failure too, e.g. when there is no session.</li>
 *     <li>Do not throw from {@link #getToken(String, Callback)}; report failures through the callback.</li>
 * </ul>
 * The SDK applies a timeout ({@link AuthManager#DEFAULT_TOKEN_FETCH_TIMEOUT_MS}) and ignores late or repeated
 * completions. A timeout, a thrown exception or an empty token is treated as a failure, and the user-identified
 * request is not sent.
 */
public interface AuthTokenProvider {

    void getToken(@NonNull String userId, @NonNull Callback callback);

    @FunctionalInterface
    interface Callback {
        void onComplete(@Nullable String token, @Nullable Exception error);
    }
}
