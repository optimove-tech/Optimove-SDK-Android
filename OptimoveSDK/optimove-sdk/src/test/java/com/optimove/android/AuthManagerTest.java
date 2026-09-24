package com.optimove.android;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AuthManagerTest {

    @Test
    public void getToken_forwardsTokenFromProvider() throws Exception {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete("the-jwt", null));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> token = new AtomicReference<>();
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken("user-1", (t, e) -> {
            token.set(t);
            err.set(e);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("the-jwt", token.get());
        Assert.assertNull(err.get());
    }

    @Test
    public void getToken_nullUserId_returnsNoUserIdError() throws Exception {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete("x", null));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken(null, (t, e) -> {
            err.set(e);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Assert.assertTrue(err.get() instanceof AuthTokenException);
        Assert.assertEquals(AuthTokenException.Kind.NO_USER_ID, ((AuthTokenException) err.get()).getKind());
    }

    @Test
    public void getToken_providerReturnsNullToken_usesTokenFetchFailed() throws Exception {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete(null, null));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            err.set(e);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Assert.assertTrue(err.get() instanceof AuthTokenException);
        Assert.assertEquals(AuthTokenException.Kind.TOKEN_FETCH_FAILED, ((AuthTokenException) err.get()).getKind());
    }

    @Test
    public void getToken_providerNeverCompletes_timesOutExactlyOnce() throws Exception {
        AuthManager manager = new AuthManager((userId, callback) -> { }, 50);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Exception> err = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            err.set(e);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Thread.sleep(150);
        Assert.assertEquals(1, calls.get());
        Assert.assertEquals(AuthTokenException.Kind.TOKEN_FETCH_TIMED_OUT, ((AuthTokenException) err.get()).getKind());
    }

    @Test
    public void getToken_lateCompletionAfterTimeout_isIgnored() throws Exception {
        AtomicReference<AuthTokenProvider.Callback> captured = new AtomicReference<>();
        AuthManager manager = new AuthManager((userId, callback) -> captured.set(callback), 50);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> token = new AtomicReference<>();
        AtomicReference<Exception> err = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            token.set(t);
            err.set(e);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        captured.get().onComplete("late-jwt", null);
        Assert.assertEquals(1, calls.get());
        Assert.assertNull(token.get());
        Assert.assertEquals(AuthTokenException.Kind.TOKEN_FETCH_TIMED_OUT, ((AuthTokenException) err.get()).getKind());
    }

    @Test
    public void getToken_providerCompletesTwice_deliversFirstOnly() {
        AuthManager manager = new AuthManager((userId, callback) -> {
            callback.onComplete("first", null);
            callback.onComplete("second", null);
        });
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> token = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            token.set(t);
        });
        Assert.assertEquals(1, calls.get());
        Assert.assertEquals("first", token.get());
    }

    @Test
    public void getToken_providerThrows_deliversErrorOnceWithoutPropagating() {
        IllegalStateException thrown = new IllegalStateException("boom");
        AuthManager manager = new AuthManager((userId, callback) -> {
            throw thrown;
        });
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            err.set(e);
        });
        Assert.assertEquals(1, calls.get());
        Assert.assertSame(thrown, err.get());
    }

    @Test
    public void getToken_providerThrowsAfterCompleting_keepsFirstResult() {
        AuthManager manager = new AuthManager((userId, callback) -> {
            callback.onComplete("jwt", null);
            throw new IllegalStateException("boom");
        });
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> token = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            token.set(t);
        });
        Assert.assertEquals(1, calls.get());
        Assert.assertEquals("jwt", token.get());
    }

    @Test
    public void getToken_completionThrows_propagatesToCaller() {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete("jwt", null));
        IllegalStateException fromCompletion = new IllegalStateException("from completion");
        try {
            manager.getToken("u", (t, e) -> {
                throw fromCompletion;
            });
            Assert.fail("expected exception from completion to propagate");
        } catch (IllegalStateException e) {
            Assert.assertSame(fromCompletion, e);
        }
    }

    @Test
    public void getToken_emptyToken_isTokenFetchFailed() {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete("", null));
        AtomicReference<String> token = new AtomicReference<>();
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            token.set(t);
            err.set(e);
        });
        Assert.assertNull(token.get());
        Assert.assertEquals(AuthTokenException.Kind.TOKEN_FETCH_FAILED, ((AuthTokenException) err.get()).getKind());
    }

    @Test
    public void getToken_successCancelsTimeout() throws Exception {
        AuthManager manager = new AuthManager((userId, callback) -> callback.onComplete("jwt", null), 50);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Exception> err = new AtomicReference<>();
        manager.getToken("u", (t, e) -> {
            calls.incrementAndGet();
            err.set(e);
        });
        Thread.sleep(150);
        Assert.assertEquals(1, calls.get());
        Assert.assertNull(err.get());
    }
}
