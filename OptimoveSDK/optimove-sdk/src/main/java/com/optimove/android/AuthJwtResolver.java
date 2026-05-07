package com.optimove.android;

import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class AuthJwtResolver {

    private AuthJwtResolver() {
    }

    /**
     * When federated auth is configured and {@code userId} is non-empty, a JWT is required before sending
     * user-identified requests. Returns true if {@code jwt} is missing after {@link #blockingJwt} (failure,
     * timeout, or empty token).
     */
    public static boolean isMissingRequiredJwt(
            @Nullable AuthManager authManager,
            @Nullable String userId,
            @Nullable String jwt) {
        if (authManager == null) {
            return false;
        }
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return jwt == null || jwt.isEmpty();
    }

    @Nullable
    public static String blockingJwt(@Nullable AuthManager authManager, @Nullable String userId, long timeoutMs) {
        if (authManager == null || userId == null || userId.isEmpty()) {
            return null;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> tokenRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        authManager.getToken(userId, (token, error) -> {
            tokenRef.set(token);
            errorRef.set(error);
            latch.countDown();
        });
        try {
            if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                OptiLoggerStreamsContainer.warn("JWT fetch timed out for user '%s' after %d ms", userId, timeoutMs);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            OptiLoggerStreamsContainer.warn("JWT fetch interrupted for user '%s'", userId);
            return null;
        }
        Exception error = errorRef.get();
        if (error != null) {
            OptiLoggerStreamsContainer.warn("JWT fetch failed for user '%s': %s", userId, error.getMessage());
            return null;
        }
        return tokenRef.get();
    }
}
