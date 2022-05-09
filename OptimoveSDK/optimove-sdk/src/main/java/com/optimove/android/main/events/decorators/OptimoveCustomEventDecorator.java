package com.optimove.android.main.events.decorators;

import com.optimove.android.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorator class to enforce BL on the incoming {@link OptimoveEvent}s:
 * <ul>
 * <li>Add additional attributes in accordance to the event's configs</li>
 * <li>Apply normalization rules on event and param names</li>
 * </ul>
 */
public class OptimoveCustomEventDecorator extends OptimoveEventDecorator {

  private String formattedEventName;

  public OptimoveCustomEventDecorator(OptimoveEvent optimoveEvent,  int maxNumberOfParamsToAdd) {
    super(optimoveEvent, maxNumberOfParamsToAdd);
    this.formattedEventName = optimoveEvent.getName().trim().toLowerCase().replace(" ", "_");

    Map<String, Object> newModifiedParams = new HashMap<>(modifiedEventParams.size());
    // TODO: 2019-09-17 are we decorating keys only?? 
    for (String paramKey : this.modifiedEventParams.keySet()) {
      newModifiedParams.put(paramKey.trim().toLowerCase().replace(" ", "_"), this.modifiedEventParams.get(paramKey));
    }
    this.modifiedEventParams = newModifiedParams;
  }

  @Override
  public String getName() {
    return formattedEventName;
  }
}