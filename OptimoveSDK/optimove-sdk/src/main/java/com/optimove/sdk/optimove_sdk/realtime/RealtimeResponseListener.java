package com.optimove.sdk.optimove_sdk.realtime;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

public interface RealtimeResponseListener {
  void onResponse(OptimoveEvent event, RealtimeEventDispatchResponse response);
}
