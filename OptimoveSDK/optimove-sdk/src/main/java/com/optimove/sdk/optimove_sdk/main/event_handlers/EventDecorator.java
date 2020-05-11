package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventDecorator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;

    public EventDecorator(Map<String, EventConfigs> eventConfigsMap) {
        this.eventConfigsMap = eventConfigsMap;
    }

    @Override
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        List<OptimoveEvent> optimoveEventsDecorated = new ArrayList<>();

        for (OptimoveEvent optimoveEvent: optimoveEvents) {
            optimoveEventsDecorated.add(new OptimoveEventDecorator(optimoveEvent,
                    eventConfigsMap.get(optimoveEvent.getName())));
        }
        reportEventNext(optimoveEventsDecorated);
    }

}
