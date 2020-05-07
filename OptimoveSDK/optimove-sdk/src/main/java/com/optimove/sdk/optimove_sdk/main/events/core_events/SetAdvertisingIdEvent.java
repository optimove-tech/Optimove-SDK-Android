package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public final class SetAdvertisingIdEvent extends OptimoveEvent implements OptimoveCoreEvent {

  public static final String EVENT_NAME = "set_advertising_id";

  public static final String APP_NS_PARAM_KEY = "app_ns";
  public static final String DEVICE_ID_PARAM_KEY = "device_id";
  public static final String ADVERTISING_ID_PARAM_KEY = "advertising_id";

  private String advertisingId;
  private String encryptedDeviceId;
  private String fullPackageName;

  public SetAdvertisingIdEvent(String advertisingId,String fullPackageName, String encryptedDeviceId) {
    this.advertisingId = advertisingId;
    this.fullPackageName = fullPackageName;
    this.encryptedDeviceId = encryptedDeviceId;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put(ADVERTISING_ID_PARAM_KEY, advertisingId);
    params.put(DEVICE_ID_PARAM_KEY, encryptedDeviceId);
    params.put(APP_NS_PARAM_KEY, fullPackageName);
    return params;
  }
}
