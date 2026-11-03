package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

/**
 * Observes overlay messages entering and leaving the screen. Unlike
 * {@link OverlayMessagingActionHandler}, this is purely an observer: it cannot alter or block
 * what the SDK does.
 * <p>
 * Contract:
 * <ul>
 *     <li>{@link #onShown} fires once when the SDK actually presents a message. It does not fire
 *     for messages the interceptor defers or discards, nor while overlays are hidden — a message
 *     held by {@code hide()} fires when it is presented after {@code show()}.</li>
 *     <li>{@link #onClosed} fires exactly once for every message {@link #onShown} fired for,
 *     including when presentation fails ({@link OverlayMessagingCloseReason#ERROR}).</li>
 *     <li>{@code hide()} is not a close: a hidden overlay is still pending.</li>
 *     <li>A message's {@link #onClosed} fires before the next message's {@link #onShown}.</li>
 *     <li>Both run on the main thread, after the SDK has updated its own queue. Exceptions are
 *     caught and logged.</li>
 * </ul>
 * Nothing fires if the process is killed while a message is on screen.
 */
public interface OverlayMessagingLifecycleListener {

    @UiThread
    default void onShown(@NonNull OverlayMessagingMessage message) {
    }

    @UiThread
    default void onClosed(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingClosedInfo info) {
    }
}
