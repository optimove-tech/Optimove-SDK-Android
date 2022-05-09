package com.optimove.android.main.events.core_events;

import androidx.annotation.Nullable;

import com.optimove.android.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class AppOpenEvent extends OptimoveEvent implements OptimoveCoreEvent {


  public static final String EVENT_NAME = "app_open";
  public static final String APP_NS_PARAM_KEY = "app_ns";
  public static final String DEVICE_ID_PARAM_KEY = "device_id";
  public static final String USER_ID_PARAM_KEY_ = "user_id";
  public static final String VISITOR_ID_PARAM_KEY = "visitor_id";


  @Nullable
  private String userId;
  @Nullable
  private String visitorId;
  private String fullPackageName;
  private String encryptedDeviceId;

  public AppOpenEvent(@Nullable String userId, @Nullable String visitorId, String fullPackageName, String encryptedDeviceId) {
    this.userId = userId;
    this.visitorId = visitorId;
    this.fullPackageName = fullPackageName;
    this.encryptedDeviceId = encryptedDeviceId;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>(4);
    params.put(APP_NS_PARAM_KEY, fullPackageName);
    params.put(DEVICE_ID_PARAM_KEY, encryptedDeviceId);
    if (userId != null)
      params.put(USER_ID_PARAM_KEY_, userId);
    else if (visitorId != null)
      params.put(VISITOR_ID_PARAM_KEY, visitorId);
    return params;
  }


}
