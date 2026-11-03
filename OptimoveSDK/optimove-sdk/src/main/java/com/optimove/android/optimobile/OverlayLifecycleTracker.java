package com.optimove.android.optimobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

/**
 * Enforces the {@link OverlayMessagingLifecycleListener} contract: one onShown per presented
 * message, exactly one matching onClosed, and a sticky "actioned" flag per message.
 */
class OverlayLifecycleTracker {

    private static final String TAG = "OverlayLifecycle";

    @Nullable
    private OverlayMessagingLifecycleListener listener;
    @Nullable
    private OverlayMessagingMessage shown;
    private boolean actioned;

    @UiThread
    void setListener(@Nullable OverlayMessagingLifecycleListener listener) {
        this.listener = listener;
    }

    /** Idempotent: re-presenting the same message (e.g. after an activity change) does not re-fire. */
    @UiThread
    void onPresented(@NonNull OverlayMessagingMessage message) {
        if (shown != null && shown.getId() == message.getId()) {
            return;
        }
        if (shown != null) {
            // Should not happen: the previous message must have closed first. Keep the pairing intact.
            onClosed(shown, OverlayMessagingCloseReason.ERROR);
        }
        shown = message;
        actioned = false;

        OverlayMessagingLifecycleListener l = listener;
        if (l == null) return;
        try {
            l.onShown(message);
        } catch (Exception e) {
            Log.e(TAG, "Lifecycle listener onShown threw", e);
        }
    }

    @UiThread
    void onLinkActionDispatched(@NonNull OverlayMessagingMessage message) {
        if (shown != null && shown.getId() == message.getId()) {
            actioned = true;
        }
    }

    /**
     * @param reasonIfNotActioned the reason to report unless a link action was already dispatched
     *                            for this message, in which case it closes as
     *                            {@link OverlayMessagingCloseReason#ACTIONED} (sticky).
     */
    @UiThread
    void onClosed(@NonNull OverlayMessagingMessage message, @NonNull String reasonIfNotActioned) {
        if (shown == null || shown.getId() != message.getId()) {
            return;
        }
        String reason = actioned ? OverlayMessagingCloseReason.ACTIONED : reasonIfNotActioned;
        OverlayMessagingMessage closed = shown;
        shown = null;
        actioned = false;

        OverlayMessagingLifecycleListener l = listener;
        if (l == null) return;
        try {
            l.onClosed(closed, new OverlayMessagingClosedInfo(reason));
        } catch (Exception e) {
            Log.e(TAG, "Lifecycle listener onClosed threw", e);
        }
    }
}
