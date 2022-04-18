package com.optimove.sdk.optimove_sdk.optistream;

import android.os.Build;

import java.util.concurrent.TimeUnit;

public interface OptitrackConstants {

  /* *********************************
   * Configurations
   * *********************************/

  long SESSION_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(30);


  /* *********************************
   * Event's Parameters
   * *********************************/

  int USER_ID_MAX_LENGTH = 200;

  String EVENT_PLATFORM_PARAM_KEY = "event_platform";
  String EVENT_DEVICE_TYPE_PARAM_KEY = "event_device_type";
  String EVENT_OS_PARAM_KEY = "event_os";
  String EVENT_NATIVE_MOBILE_PARAM_KEY = "event_native_mobile";

  String EVENT_PLATFORM_DEFAULT_VALUE = "Android";
  String EVENT_DEVICE_TYPE_DEFAULT_VALUE = "Mobile";
  String EVENT_OS_DEFAULT_VALUE = String.format("Android %s", Build.VERSION.RELEASE);
  boolean EVENT_NATIVE_MOBILE_DEFAULT_VALUE = true;

  /* *********************************
   * Shared Preferences
   * *********************************/

  String OPTITRACK_SP_NAME = "com.optimove.sdk.optitrack_shared_pref";

  int OPTITRACK_BUFFER_SIZE = 100;

}
