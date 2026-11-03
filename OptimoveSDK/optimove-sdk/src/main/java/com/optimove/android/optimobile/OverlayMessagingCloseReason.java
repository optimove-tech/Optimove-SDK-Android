package com.optimove.android.optimobile;

/** Why an overlay message closed. */
public enum OverlayMessagingCloseReason {

    /** The user took a link (the client's link action handler, or the SDK default, ran). */
    ACTIONED,

    /** The user closed the message without taking a link (close button, backdrop tap, back). */
    DISMISSED,

    /** The message failed to present or the renderer errored while it was on screen. */
    ERROR
}
