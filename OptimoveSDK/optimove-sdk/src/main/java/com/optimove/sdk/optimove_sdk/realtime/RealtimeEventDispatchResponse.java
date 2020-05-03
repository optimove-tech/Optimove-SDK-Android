package com.optimove.sdk.optimove_sdk.realtime;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.*;

public final class RealtimeEventDispatchResponse {

  private boolean success;
  private boolean hasData;

  /**
   * Defaults the response to an error response
   */
  public RealtimeEventDispatchResponse() {
    this(false, false);
  }

  public RealtimeEventDispatchResponse(boolean success, boolean hasData) {
    this.success = success;
    this.hasData = hasData;
  }

  public static RealtimeEventDispatchResponse fromJson(JSONObject responseJson) throws JSONException {
    RealtimeEventDispatchResponse result = new RealtimeEventDispatchResponse();
    result.success = responseJson.getBoolean(EVENT_RESPONSE_SUCCESS_KEY);
    result.hasData = responseJson.getBoolean(EVENT_RESPONSE_DATA_KEY); // TEMP until Mobile Popup campaigns are supported
    return result;
  }

  public boolean isSuccess() {
    return success;
  }

  public boolean hasData() {
    return hasData;
  }
}
