package com.optimove.android.optimobile;

/** Why an overlay message closed. */
public enum OverlayMessagingCloseReason {

    /**
     * The user interacted with the message (tapped an action button) before it closed. Whether a
     * link was opened is up to the action handler.
     */
    ACTIONED,

    /** The user closed the message without interacting with it (close button, backdrop tap, back). */
    DISMISSED,

    /** The message failed to present or the renderer errored while it was on screen. */
    ERROR
}
