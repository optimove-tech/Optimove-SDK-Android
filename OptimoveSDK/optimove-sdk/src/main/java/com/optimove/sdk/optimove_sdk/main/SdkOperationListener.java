package com.optimove.sdk.optimove_sdk.main;

/**
 * Used by the <i>Optimove SDK</i> to report back to a caller of an {@code operation} if the operation was <b>successful</b>.<br>
 */
public interface SdkOperationListener {

  /**
   * Called once the called {@code operation} is done with the operation's success flag.
   *
   * @param success the {@code operation}'s success flag.
   */
  void onResult(boolean success);
}
