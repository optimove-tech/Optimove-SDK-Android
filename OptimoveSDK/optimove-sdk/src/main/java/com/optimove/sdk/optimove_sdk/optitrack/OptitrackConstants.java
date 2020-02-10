package com.optimove.sdk.optimove_sdk.optitrack;

import android.os.Build;

import java.util.concurrent.TimeUnit;

public interface OptitrackConstants {

  /* *********************************
   * Optitrack Dispatcher
   * *********************************/

  int DEFAULT_DISPATCH_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(12);


  /* *********************************
   * Configurations
   * *********************************/

  long SESSION_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(30);


  /* *********************************
   * Event's Parameters
   * *********************************/

  String PARAMETER_STRING_TYPE = "String";
  String PARAMETER_NUMBER_TYPE = "Number";
  String PARAMETER_BOOLEAN_TYPE = "Boolean";
  int PARAMETER_VALUE_MAX_LENGTH = 255;

  String EVENT_PLATFORM_PARAM_KEY = "event_platform";
  String EVENT_DEVICE_TYPE_PARAM_KEY = "event_device_type";
  String EVENT_OS_PARAM_KEY = "event_os";
  String EVENT_NATIVE_MOBILE_PARAM_KEY = "event_native_mobile";

  String EVENT_PLATFORM_DEFAULT_VALUE = "Android";
  String EVENT_DEVICE_TYPE_DEFAULT_VALUE = "Mobile";
  String EVENT_OS_DEFAULT_VALUE = String.format("Android %s", Build.VERSION.RELEASE);
  boolean EVENT_NATIVE_MOBILE_DEFAULT_VALUE = true;

  // Used in finer tuning the Matomo Visitor Fingerprint
  String[] MATOMO_PLUGIN_FLAGS = {"fla", "java", "dir", "qt", "realp", "pdf", "wma", "gears"};

  /* *********************************
   * Shared Preferences
   * *********************************/

  String OPTITRACK_SP_NAME = "com.optimove.sdk.optitrack_shared_pref";
  String OPTITRACK_USER_ID_KEY = "optitrack_user_id";
  String LAST_OPT_REPORTED_KEY = "last_opt_reported";
  int LAST_REPORTED_OPT_IN = 1;
  int LAST_REPORTED_OPT_OUT = 2;


  int OPTITRACK_BUFFER_SIZE = 100;

}
