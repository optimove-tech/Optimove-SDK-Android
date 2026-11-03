package com.optimove.android.optimobile;

/**
 * Why an overlay message closed. Deliberately a set of {@code String} constants rather than an
 * enum: new reasons may be added without breaking callers, who must treat unknown values as
 * "closed".
 */
public final class OverlayMessagingCloseReason {

    /** The user took a link (the client's link action handler, or the SDK default, ran). */
    public static final String ACTIONED = "actioned";

    /** The user closed the message without taking a link (close button, backdrop tap, back). */
    public static final String DISMISSED = "dismissed";

    /** The message failed to present or the renderer errored while it was on screen. */
    public static final String ERROR = "error";

    private OverlayMessagingCloseReason() {
    }
}
