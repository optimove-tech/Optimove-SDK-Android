package com.optimove.sdk.optimove_sdk.main.events.core_events;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import org.matomo.sdk.tools.BuildInfo;
import org.matomo.sdk.tools.DeviceHelper;
import org.matomo.sdk.tools.PropertySource;

import java.util.HashMap;
import java.util.Map;

public final class UserAgentHeaderEvent implements OptimoveEvent, OptimoveCoreEvent {


  public static final String EVENT_NAME = "user_agent_header_event";

  public static final String USER_AGENT_HEADER1_PARAM_KEY = "user_agent_header1";
  public static final String USER_AGENT_HEADER2_PARAM_KEY = "user_agent_header2";


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
    if (userAgent.length() <= OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH) {
      params.put(USER_AGENT_HEADER1_PARAM_KEY, userAgent);
      return params;
    }
    params.put(USER_AGENT_HEADER1_PARAM_KEY, userAgent.substring(0, OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH));
    params.put(USER_AGENT_HEADER2_PARAM_KEY, userAgent.substring(OptitrackConstants.PARAMETER_VALUE_MAX_LENGTH));
    return params;
  }
}
