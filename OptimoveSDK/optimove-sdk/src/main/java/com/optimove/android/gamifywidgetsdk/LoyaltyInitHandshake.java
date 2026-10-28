package com.optimove.android.gamifywidgetsdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

/**
 * Pairs the loyalty widget's READY signal with the auth token that goes into INIT.
 *
 * <p>The token request starts as soon as the dialog opens so it overlaps page load; INIT is sent once READY has
 * arrived and the token has resolved, in whichever order they land. A failed token fetch ends the handshake — the
 * widget is never handed an INIT without the JWT auth requires.
 *
 * <p>Not thread-safe: every method must be called on the main thread.
 */
@UiThread
final class LoyaltyInitHandshake {

    interface Listener {
        void sendInit(@Nullable String token);

        void onTokenFailed(@NonNull Exception error);
    }

    private final Listener listener;

    private boolean readyPending;
    private boolean tokenResolved;
    private boolean finished;
    @Nullable private String token;

    LoyaltyInitHandshake(@NonNull Listener listener) {
        this.listener = listener;
    }

    void onReady() {
        if (finished) {
            return;
        }
        readyPending = true;
        maybeSendInit();
    }

    void onTokenResult(@Nullable String token, @Nullable Exception error) {
        if (finished || tokenResolved) {
            return;
        }
        if (error != null) {
            finished = true;
            listener.onTokenFailed(error);
            return;
        }
        this.token = token;
        tokenResolved = true;
        maybeSendInit();
    }

    void cancel() {
        finished = true;
    }

    private void maybeSendInit() {
        if (!readyPending || !tokenResolved) {
            return;
        }
        // Every READY is answered: the widget re-sends it if it reloads and needs INIT again.
        readyPending = false;
        listener.sendInit(token);
    }
}
