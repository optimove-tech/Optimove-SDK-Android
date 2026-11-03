package com.optimove.android.optimobile;

/**
 * Why an overlay message closed. Deliberately a set of {@code String} constants rather than an
 * enum: new reasons may be added without breaking callers, who must treat unknown values as
 * "closed".
 */
public final class OverlayMessagingCloseReason {

    /**
     * A link action was dispatched while the message was on screen. Best effort: the renderer
     * does not distinguish the close button from a close-only CTA.
     */
    public static final String ACTIONED = "actioned";

    /** The user closed the message without a link action (close button, close-only CTA, back). */
    public static final String DISMISSED = "dismissed";

    /** The message failed to present or the renderer errored while it was on screen. */
    public static final String ERROR = "error";

    private OverlayMessagingCloseReason() {
    }
}
