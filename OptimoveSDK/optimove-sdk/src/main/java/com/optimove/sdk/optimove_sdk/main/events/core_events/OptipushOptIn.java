package com.optimove.sdk.optimove_sdk.main.events.core_events;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;

import java.util.HashMap;
import java.util.Map;

public final class OptipushOptIn implements OptimoveEvent, OptimoveCoreEvent {

  public static final String EVENT_NAME = "optipush_opt_in";
  public static final String APP_NS_PARAM_KEY = "app_ns";
  public static final String TIMESTAMP_PARAM_KEY = "timestamp";
  public static final String DEVICE_ID_PARAM_KEY = "device_id";



  private String fullPackageName;
  private String encryptedDeviceId;
  private long timeInSeconds;

  public OptipushOptIn(String fullPackageName, String encryptedDeviceId, long timeInSeconds) {
    this.fullPackageName = fullPackageName;
    this.encryptedDeviceId = encryptedDeviceId;
    this.timeInSeconds = timeInSeconds;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put(TIMESTAMP_PARAM_KEY, timeInSeconds);
    params.put(APP_NS_PARAM_KEY, fullPackageName);
    params.put(DEVICE_ID_PARAM_KEY, encryptedDeviceId);
    return params;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }
}
