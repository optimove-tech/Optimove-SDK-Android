package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackManager;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.Map;

public class ComponentPool extends EventHandler {

    private static int realtimeExpirationTimeInMillis = 1000;
    private Optist optitrackManager;
    private Map<String, EventConfigs> eventConfigsMap;

    public ComponentPool(Map<String, EventConfigs> eventConfigsMap, OptitrackManager optitrackManager,
                         RealtimeManager realtimeManager) {
        this.eventConfigsMap = eventConfigsMap;
        this.optitrackManager = optitrackManager;
        this.realtimeManager = realtimeManager;
    }

    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        EventConfigs eventConfigs = eventConfigsMap.get(optimoveEvent.getName());
        long currentTimeMillis = System.currentTimeMillis();

        if (eventConfigs.isSupportedOnOptitrack()) {
            optitrackManager.reportEvent(optimoveEvent, eventConfigs);
            if (eventContext.getExecutionTimeout() > 0) {
                optitrackManager.setTimeout(eventContext.getExecutionTimeout());
                optitrackManager.sendAllEventsNow();
            }
        }
    }

    private boolean isImportantEvent(OptimoveEvent optimoveEvent) {
        return (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME) ||
                optimoveEvent.getName()
                        .equals(SetEmailEvent.EVENT_NAME));
    }
}
