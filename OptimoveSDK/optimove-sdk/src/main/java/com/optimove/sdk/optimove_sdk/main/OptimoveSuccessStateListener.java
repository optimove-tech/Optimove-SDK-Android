package com.optimove.sdk.optimove_sdk.main;

import com.optimove.sdk.optimove_sdk.main.constants.SdkRequiredPermission;

/**
 * Callback called when the SDK has finished initializing <b>successfully</b>.
 */
public interface OptimoveSuccessStateListener {

  /**
   * Called when the SDK has finished initialization successfully. <b>DO NOT</b> depend any UX flow on this callback.
   * <p>
   * <b>Discussion</b>:
   * There might be some {@code permission}s that the SDK requires in order to perform <b>better</b>.<br>
   * Those {@code permission}s are not mandatory for the SDK to run, however they improve the execution of the <i>services</i> that the SDK provides.
   *
   * @param missingPermissions Additional {@code permission}s that the SDK needs
   */
  void onConfigurationSucceed(SdkRequiredPermission... missingPermissions);
}
