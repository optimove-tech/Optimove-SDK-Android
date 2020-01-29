package com.optimove.sdk.optimove_sdk.realtime;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Arrays;
import java.util.Map;

public final class RealtimeEvent {

  private int id;
  private long firstVisitorDate;
  private Map<String, Object> context;

  private RealtimeEvent(int id, long firstVisitorDate, Map<String, Object> context) {
    this.id = id;
    this.firstVisitorDate = firstVisitorDate;
    this.context = context;
  }

  public static RealtimeEvent newInstance(OptimoveEvent optimoveEvent, int eventId,
                                          Long firstVisitorDate){ //
    // Use a very bad value to detect anomalies during QA
    return new RealtimeEvent(eventId, firstVisitorDate, optimoveEvent. getParameters());
  }

  public int getId() {
    return id;
  }

  public long getFirstVisitorDate() {
    return firstVisitorDate;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  @Override
  public String toString() {
    return id + ": " + Arrays.deepToString(context.values().toArray());
  }
}
