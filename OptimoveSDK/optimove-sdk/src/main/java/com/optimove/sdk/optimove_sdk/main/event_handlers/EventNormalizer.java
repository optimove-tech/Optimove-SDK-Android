package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveCustomEventDecorator;
import com.optimove.sdk.optimove_sdk.main.EventContext;

public class EventNormalizer extends EventHandler {


    @Override
    public void reportEvent(EventContext eventContext) {
        if (!(eventContext.getOptimoveEvent() instanceof OptimoveCoreEvent)) {
            eventContext.setOptimoveEvent(new OptimoveCustomEventDecorator(eventContext.getOptimoveEvent()));
            reportEventNext(eventContext);
        }  else {
            reportEventNext(eventContext);
        }
    }
}
