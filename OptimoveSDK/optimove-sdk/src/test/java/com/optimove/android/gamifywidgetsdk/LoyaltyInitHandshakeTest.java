package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyInitHandshakeTest {

    private final List<String> inits = new ArrayList<>();
    private final List<Exception> failures = new ArrayList<>();

    private final LoyaltyInitHandshake handshake = new LoyaltyInitHandshake(new LoyaltyInitHandshake.Listener() {
        @Override
        public void sendInit(@Nullable String token) {
            inits.add(token);
        }

        @Override
        public void onTokenFailed(@NonNull Exception error) {
            failures.add(error);
        }
    });

    @Test
    public void readyBeforeToken_sendsInitOnceTokenResolves() {
        handshake.onReady();
        assertTrue(inits.isEmpty());

        handshake.onTokenResult("jwt", null);
        assertEquals(1, inits.size());
        assertEquals("jwt", inits.get(0));
    }

    @Test
    public void tokenBeforeReady_sendsInitOnReady() {
        handshake.onTokenResult("jwt", null);
        assertTrue(inits.isEmpty());

        handshake.onReady();
        assertEquals(1, inits.size());
        assertEquals("jwt", inits.get(0));
    }

    @Test
    public void nullTokenWithoutError_sendsInitWithNullToken() {
        handshake.onTokenResult(null, null);
        handshake.onReady();

        assertEquals(1, inits.size());
        assertNull(inits.get(0));
    }

    @Test
    public void everyReadyIsAnswered() {
        handshake.onTokenResult("jwt", null);
        handshake.onReady();
        handshake.onReady();

        assertEquals(2, inits.size());
    }

    @Test
    public void tokenFailure_reportsAndNeverSendsInit() {
        handshake.onReady();
        handshake.onTokenResult(null, new Exception("no session"));
        handshake.onReady();

        assertTrue(inits.isEmpty());
        assertEquals(1, failures.size());
    }

    @Test
    public void cancelledWhileTokenInFlight_doesNotSendInit() {
        handshake.onReady();
        handshake.cancel();
        handshake.onTokenResult("late-jwt", null);

        assertTrue(inits.isEmpty());
        assertTrue(failures.isEmpty());
    }
}
