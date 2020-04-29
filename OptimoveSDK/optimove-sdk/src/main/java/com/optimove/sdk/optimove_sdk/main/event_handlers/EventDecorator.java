package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.Map;

public class EventDecorator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;

    public EventDecorator(Map<String, EventConfigs> eventConfigsMap) {
        this.eventConfigsMap = eventConfigsMap;
    }

    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        reportEventNext(new OptimoveEventDecorator(optimoveEvent,
                eventConfigsMap.get(optimoveEvent.getName())));
    }

}
