package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveCustomEventDecorator;

public class EventNormalizer extends EventHandler {


    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        if (!(optimoveEvent instanceof OptimoveCoreEvent)) {
            reportEventNext(new OptimoveCustomEventDecorator(optimoveEvent));
        }  else {
            reportEventNext(optimoveEvent);
        }
    }
}
