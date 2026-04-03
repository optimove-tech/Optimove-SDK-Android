package com.optimove.android;

import org.junit.Assert;
import org.junit.Test;

public class AuthJwtResolverTest {

    private final AuthTokenProvider dummyProvider = (userId, callback) -> { };

    @Test
    public void isMissingRequiredJwt_noProvider_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(null, "u1", null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(null, "u1", "jwt"));
    }

    @Test
    public void isMissingRequiredJwt_emptyUserId_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, null, null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, "", null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, "  ", null));
    }

    @Test
    public void isMissingRequiredJwt_providerAndUser_missingJwt_true() {
        Assert.assertTrue(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, "u1", null));
        Assert.assertTrue(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, "u1", ""));
    }

    @Test
    public void isMissingRequiredJwt_hasToken_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyProvider, "u1", "jwt"));
    }
}
