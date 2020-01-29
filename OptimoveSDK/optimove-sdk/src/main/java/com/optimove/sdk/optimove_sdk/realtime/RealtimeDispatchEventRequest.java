package com.optimove.sdk.optimove_sdk.realtime;

import com.optimove.sdk.optimove_sdk.main.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.*;

public final class RealtimeDispatchEventRequest {

  private String realtimeToken;
  private UserInfo userInfo;
  private RealtimeEvent realtimeEvent;

  public RealtimeDispatchEventRequest(String realtimeToken, UserInfo userInfo, RealtimeEvent event) {
    this.realtimeToken = realtimeToken;
    this.userInfo = userInfo;
    this.realtimeEvent = event;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject result = new JSONObject();
    result.put(EVENT_REQUEST_TID_KEY, realtimeToken);
    if (userInfo.getUserId() != null)
      result.put(EVENT_REQUEST_CID_KEY, userInfo.getUserId());
    else
      result.put(EVENT_REQUEST_VID_KEY, userInfo.getVisitorId());
    result.put(EVENT_REQUEST_EID_KEY, realtimeEvent.getId());
    result.put(EVENT_REQUEST_FIRST_VISITOR_DATE_KEY, realtimeEvent.getFirstVisitorDate());
    result.put(EVENT_REQUEST_CONTEXT_KEY, new JSONObject(realtimeEvent.getContext()));
    return result;
  }
}
