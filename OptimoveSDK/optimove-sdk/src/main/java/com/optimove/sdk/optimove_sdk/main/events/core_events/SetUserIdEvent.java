package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public final class SetUserIdEvent implements OptimoveEvent, OptimoveCoreEvent {

  public static final String EVENT_NAME = "set_user_id_event";

  public static final String ORIGINAL_VISITOR_ID_PARAM_KEY = "originalVisitorId";
  public static final String USER_ID_PARAM_KEY = "userId";
  public static final String UPDATED_VISITOR_ID_PARAM_KEY = "updatedVisitorId";

  private String originalVisitorId;
  private String userId;
  private String updatedVisitorId;

  public SetUserIdEvent(String originalVisitorId, String userId, String updatedVisitorId) {
    this.originalVisitorId = originalVisitorId;
    this.userId = userId;
    this.updatedVisitorId = updatedVisitorId;
  }

  public String getOriginalVisitorId() {
    return originalVisitorId;
  }

  public String getUserId() {
    return userId;
  }

  public String getUpdatedVisitorId() {
    return updatedVisitorId;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put(ORIGINAL_VISITOR_ID_PARAM_KEY, originalVisitorId);
    params.put(USER_ID_PARAM_KEY, userId);
    params.put(UPDATED_VISITOR_ID_PARAM_KEY, updatedVisitorId);
    return params;
  }
}