package com.optimove.android;

import org.junit.Assert;
import org.junit.Test;

public class AuthJwtResolverTest {

    private final AuthManager dummyManager = new AuthManager((userId, callback) -> { });

    @Test
    public void isMissingRequiredJwt_noProvider_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt((AuthManager) null, "u1", null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt((AuthManager) null, "u1", "jwt"));
    }

    @Test
    public void isMissingRequiredJwt_emptyUserId_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyManager, null, null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyManager, "", null));
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyManager, "  ", null));
    }

    @Test
    public void isMissingRequiredJwt_providerAndUser_missingJwt_true() {
        Assert.assertTrue(AuthJwtResolver.isMissingRequiredJwt(dummyManager, "u1", null));
        Assert.assertTrue(AuthJwtResolver.isMissingRequiredJwt(dummyManager, "u1", ""));
    }

    @Test
    public void isMissingRequiredJwt_hasToken_false() {
        Assert.assertFalse(AuthJwtResolver.isMissingRequiredJwt(dummyManager, "u1", "jwt"));
    }
}
