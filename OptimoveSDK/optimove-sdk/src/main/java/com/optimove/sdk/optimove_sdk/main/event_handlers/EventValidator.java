package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.EventValidationResult;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEventValidator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import java.util.Map;

public class EventValidator extends EventHandler {

    private Map<String, EventConfigs> eventConfigsMap;

    public EventValidator(Map<String, EventConfigs> eventConfigsMap) {
        this.eventConfigsMap = eventConfigsMap;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void reportEvent(OptimoveEvent optimoveEvent) {
        String eventName = optimoveEvent.getName();
        if (!eventConfigsMap.containsKey(eventName)) {
            OptiLogger.eventDoesntAppearInConfigs(eventName);
            return;
        }
        EventValidationResult validationResult =
                new OptimoveEventValidator(optimoveEvent, eventConfigsMap.get(eventName)).validateEvent();
        if (validationResult != EventValidationResult.VALID) {
            OptiLogger.eventIsInvalid(eventName, validationResult);
            return;
        }
        reportEventNext(optimoveEvent);
    }

}
