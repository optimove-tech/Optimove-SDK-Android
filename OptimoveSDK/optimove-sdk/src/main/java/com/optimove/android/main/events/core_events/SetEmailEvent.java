package com.optimove.android.main.events.core_events;

import com.optimove.android.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class SetEmailEvent extends OptimoveEvent implements OptimoveCoreEvent {

  public static final String EVENT_NAME = "set_email_event";

  public static final String EMAIL_PARAM_KEY = "email";

  private String email;

  public SetEmailEvent(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }


  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put(EMAIL_PARAM_KEY, email);
    return params;
  }
}
