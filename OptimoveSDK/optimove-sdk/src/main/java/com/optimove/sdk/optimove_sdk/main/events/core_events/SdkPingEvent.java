package com.optimove.sdk.optimove_sdk.main.events.core_events;

import android.content.Context;
import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;

import java.util.HashMap;
import java.util.Map;

public class SdkPingEvent extends OptimoveEvent implements OptimoveCoreEvent {

  public static final String EVENT_NAME = "notification_ping";

  public static final String APP_NS_PARAM_KEY = "app_ns";
  public static final String DEVICE_ID_PARAM_KEY = "device_id";
  public static final String USER_ID_PARAM_KEY = "user_id";
  public static final String VISITOR_ID_PARAM_KEY = "visitor_id";


  @Nullable
  private String userId;
  @Nullable
  private String visitorId;

  private String fullPackageName;
  private String encryptedDeviceId;

  public SdkPingEvent(@Nullable String userId, @Nullable String visitorId, String fullPackageName, String encryptedDeviceId) {
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
    Map<String, Object> params = new HashMap<>(3);
    params.put(APP_NS_PARAM_KEY, fullPackageName);
    params.put(DEVICE_ID_PARAM_KEY, encryptedDeviceId);
    if (userId != null)
      params.put(USER_ID_PARAM_KEY, userId);
    else if (visitorId != null)
      params.put(VISITOR_ID_PARAM_KEY, visitorId);
    return params;
  }
}
