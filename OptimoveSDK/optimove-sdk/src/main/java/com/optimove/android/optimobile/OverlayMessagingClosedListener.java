package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

/**
 * Observes overlay messages leaving the screen — e.g. to keep the host app's own popup queue in
 * step with overlays. Purely an observer: unlike {@link OverlayMessagingActionHandler}, it cannot
 * alter or block what the SDK does.
 * <p>
 * {@link #onClosed} fires once for every presented message, including when presentation fails
 * ({@link OverlayMessagingCloseReason#ERROR}). It does not fire on {@code hide()}, for messages
 * the interceptor defers or discards, or if the process is killed while a message is presented.
 * It runs on the main thread after the SDK has updated its queue; exceptions are caught and
 * logged.
 */
public interface OverlayMessagingClosedListener {

    @UiThread
    void onClosed(@NonNull OverlayMessagingMessage message, @NonNull OverlayMessagingClosedInfo info);
}
