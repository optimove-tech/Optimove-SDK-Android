package com.optimove.sdk.optimove_sdk.main.exceptions;

import android.os.Looper;

/**
 * Thrown by a function that is invoked on a <b>Worker Thread</b> instead of the <b>Main Thread</b>.
 *
 * @see Looper#getMainLooper()
 */
public final class MustRunOnMainThreadException extends RuntimeException {
}
