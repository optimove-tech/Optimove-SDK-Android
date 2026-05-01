package com.optimove.android;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
}
