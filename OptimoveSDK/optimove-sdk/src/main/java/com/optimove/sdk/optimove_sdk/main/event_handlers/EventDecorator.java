package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventDecorator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;
    private int maxNumberOfParams;

    public EventDecorator(Map<String, EventConfigs> eventConfigsMap, int maxNumberOfParams) {
        this.eventConfigsMap = eventConfigsMap;
        this.maxNumberOfParams = maxNumberOfParams;
    }

    @Override
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        List<OptimoveEvent> optimoveEventsToSendToNext = new ArrayList<>();

        for (OptimoveEvent optimoveEvent : optimoveEvents) {
            if (eventConfigsMap.containsKey(optimoveEvent.getName())) {
                optimoveEventsToSendToNext.add(new OptimoveEventDecorator(optimoveEvent,
                        eventConfigsMap.get(optimoveEvent.getName()),
                        maxNumberOfParams - optimoveEvent.getParameters()
                                .size()));
            } else {
                optimoveEventsToSendToNext.add(optimoveEvent);
            }
        }
        reportEventNext(optimoveEventsToSendToNext);
    }

}
