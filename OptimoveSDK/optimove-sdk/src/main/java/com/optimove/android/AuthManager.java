package com.optimove.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class AuthManager {

    public static final long DEFAULT_TOKEN_FETCH_TIMEOUT_MS = 10_000L;

    private static final ScheduledExecutorService TIMEOUT_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "optimove-auth-timeout");
                t.setDaemon(true);
                return t;
            });

    private final @NonNull AuthTokenProvider provider;
    private final long tokenFetchTimeoutMs;

    public AuthManager(@NonNull AuthTokenProvider provider) {
        this(provider, DEFAULT_TOKEN_FETCH_TIMEOUT_MS);
    }

    public AuthManager(@NonNull AuthTokenProvider provider, long tokenFetchTimeoutMs) {
        this.provider = provider;
        this.tokenFetchTimeoutMs = tokenFetchTimeoutMs;
    }

    /**
     * Requests a JWT for {@code userId}. {@code completion} is invoked exactly once, on an unspecified thread:
     * the provider's thread, the timeout thread, or synchronously on the caller's thread. A timeout, a thrown
     * provider exception, a {@code null} or an empty token are all delivered as errors.
     */
    public void getToken(@Nullable String userId, @NonNull AuthTokenProvider.Callback completion) {
        if (userId == null || userId.trim().isEmpty()) {
            completion.onComplete(null, new AuthTokenException(AuthTokenException.Kind.NO_USER_ID));
            return;
        }
        final String id = userId.trim();

        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<ScheduledFuture<?>> timeoutRef = new AtomicReference<>();
        final AtomicReference<RuntimeException> completionFailure = new AtomicReference<>();

        final AuthTokenProvider.Callback once = (token, error) -> {
            if (!completed.compareAndSet(false, true)) {
                OptiLoggerStreamsContainer.warn("Auth token provider completed late or more than once; ignoring");
                return;
            }
            ScheduledFuture<?> timeout = timeoutRef.getAndSet(null);
            if (timeout != null) {
                timeout.cancel(false);
            }
            try {
                if (token != null && !token.isEmpty()) {
                    completion.onComplete(token, null);
                } else {
                    completion.onComplete(null, error != null ? error
                            : new AuthTokenException(AuthTokenException.Kind.TOKEN_FETCH_FAILED));
                }
            } catch (RuntimeException e) {
                completionFailure.set(e);
                throw e;
            }
        };

        if (tokenFetchTimeoutMs > 0) {
            try {
                timeoutRef.set(TIMEOUT_SCHEDULER.schedule(() -> {
                    OptiLoggerStreamsContainer.warn("Auth token provider did not complete within %d ms",
                            tokenFetchTimeoutMs);
                    once.onComplete(null, new AuthTokenException(AuthTokenException.Kind.TOKEN_FETCH_TIMED_OUT));
                }, tokenFetchTimeoutMs, TimeUnit.MILLISECONDS));
            } catch (RuntimeException e) {
                OptiLoggerStreamsContainer.warn("Could not schedule auth token timeout - %s", e.getMessage());
            }
        }

        try {
            provider.getToken(id, once);
        } catch (Exception e) {
            if (e == completionFailure.get()) {
                // thrown by the SDK's own completion running synchronously inside the provider, not by the provider
                throw (RuntimeException) e;
            }
            OptiLoggerStreamsContainer.error("Auth token provider threw - %s", e.getMessage());
            once.onComplete(null, e);
        }
    }
}
