package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.EventValidationResult;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEventValidator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventValidator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;

    public EventValidator(Map<String, EventConfigs> eventConfigsMap) {
        this.eventConfigsMap = eventConfigsMap;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        List<OptimoveEvent> optimoveEventsValidated = new ArrayList<>();

        for (OptimoveEvent optimoveEvent: optimoveEvents) {
            String eventName = optimoveEvent.getName();
            if (!eventConfigsMap.containsKey(eventName)) {
                OptiLogger.eventDoesntAppearInConfigs(eventName);
                continue;
            }
            EventValidationResult validationResult =
                    new OptimoveEventValidator(optimoveEvent, eventConfigsMap.get(eventName)).validateEvent();
            if (validationResult != EventValidationResult.VALID) {
                OptiLogger.eventIsInvalid(eventName, validationResult);
                continue;
            }
            optimoveEventsValidated.add(optimoveEvent);
        }

        if(!optimoveEventsValidated.isEmpty()) {
            reportEventNext(optimoveEventsValidated);
        }
    }

}
