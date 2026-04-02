package com.optimove.android;

import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class AuthJwtResolver {

    private AuthJwtResolver() {
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
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return errorRef.get() != null ? null : tokenRef.get();
    }
}
