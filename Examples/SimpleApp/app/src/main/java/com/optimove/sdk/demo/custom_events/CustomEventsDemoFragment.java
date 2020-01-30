package com.optimove.sdk.demo.custom_events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.optimove.sdk.demo.R;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CustomEventsDemoFragment extends Fragment {

  private TextInputEditText eventNameInput;
  private TextInputEditText eventParamInput;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_custom_events_demo, container, false);
    view.findViewById(R.id.reportEventButton).setOnClickListener(v -> reportEvent());
    eventNameInput = view.findViewById(R.id.eventDemoNameInput);
    eventParamInput = view.findViewById(R.id.eventDemoParamInput);
    return view;
  }

  public void reportEvent() {
    String eventName = eventNameInput.getText().toString();
    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("param_key", this.eventParamInput.getText().toString());

    // Report Simple Events
    Optimove.getInstance().reportEvent(eventName, eventParams);
    // Report Complex Events
    Optimove.getInstance().reportEvent(new ComplexCustomEvent("some value"));
  }


  private static class ComplexCustomEvent implements OptimoveEvent {

    private String someValue;

    public ComplexCustomEvent(String someValue) {
      this.someValue = someValue;
    }

    @Override
    public String getName() {
      return "complex_event_example";
    }

    @Override
    public Map<String, Object> getParameters() {
      Map<String, Object> eventParams = new HashMap<>();
      eventParams.put("remote_param_key", this.someValue);
      return eventParams;
    }
  }
}
