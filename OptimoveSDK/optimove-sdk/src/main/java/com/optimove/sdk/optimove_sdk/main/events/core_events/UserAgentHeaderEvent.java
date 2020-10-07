package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public final class UserAgentHeaderEvent extends OptimoveEvent implements OptimoveCoreEvent {


  public static final String EVENT_NAME = "user_agent_header_event";

  public static final String USER_AGENT_HEADER1_PARAM_KEY = "user_agent_header1";
  public static final String USER_AGENT_HEADER2_PARAM_KEY = "user_agent_header2";

  public static final int  USER_AGENT_VALUE_MAX_LENGTH = 255;



  private String userAgent;
  public UserAgentHeaderEvent(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    if (userAgent.length() <= USER_AGENT_VALUE_MAX_LENGTH) {
      params.put(USER_AGENT_HEADER1_PARAM_KEY, userAgent);
      return params;
    }
    params.put(USER_AGENT_HEADER1_PARAM_KEY, userAgent.substring(0, USER_AGENT_VALUE_MAX_LENGTH));
    params.put(USER_AGENT_HEADER2_PARAM_KEY, userAgent.substring(USER_AGENT_VALUE_MAX_LENGTH));
    return params;
  }
}
