package com.optimove.sdk.optimove_sdk.main.constants;

/**
 * Constants that represent all the {@code Permissions} that the Optimove SDK requires from the Device.<br>
 */
public enum SdkRequiredPermission {

  /**
   * Indicates that the app is not allowed to display a "drop down" notification banner when a new notification arrives.
   */
  DRAW_NOTIFICATION_OVERLAY(0),

  /**
   * Indicates that the user has opted-out from the app's notification
   */
  NOTIFICATIONS(1),

  /**
   * Indicates that the Google Play Services app on the user's device is either missing or outdated
   */
  GOOGLE_PLAY_SERVICES(2),

  /**
   * Indicates that the user has opted-out from allowing apps from accessing his/her Advertising ID
   */
  ADVERTISING_ID(3);

  private int value;

  SdkRequiredPermission(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
