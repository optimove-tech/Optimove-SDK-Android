package com.optimove.sdk.optimove_sdk.main.events;

import java.util.Map;

public class SimpleCustomEvent implements OptimoveEvent {

  private String name;
  private Map<String, Object> parameters;

  public SimpleCustomEvent(String name, Map<String, Object> parameters) {
    this.name = name;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }
}
