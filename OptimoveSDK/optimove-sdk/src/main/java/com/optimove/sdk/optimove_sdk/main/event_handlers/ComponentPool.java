package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.Map;

public class ComponentPool extends EventHandler {

    private static int realtimeExpirationTimeInMillis = 1000;
    private OptistreamHandler optistreamHandler;
    private RealtimeManager realtimeManager;
    private Map<String, EventConfigs> eventConfigsMap;
    private boolean realtimeEnabled;
    private boolean realtimeEnabledThroughOptistream;


    public ComponentPool(Map<String, EventConfigs> eventConfigsMap, OptistreamHandler optistreamHandler,
                         RealtimeManager realtimeManager, boolean realtimeEnabled, boolean realtimeEnabledThroughOptistream) {
        this.eventConfigsMap = eventConfigsMap;
        this.optistreamHandler = optistreamHandler;
        this.realtimeManager = realtimeManager;
        this.realtimeEnabled = realtimeEnabled;
        this.realtimeEnabledThroughOptistream = realtimeEnabledThroughOptistream;
    }

    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        EventConfigs eventConfigs = eventConfigsMap.get(optimoveEvent.getName());

        long currentTimeMillis = System.currentTimeMillis();
        boolean expiredForRealtime =
                (currentTimeMillis - optimoveEvent.getTimestamp()) > realtimeExpirationTimeInMillis;

        if (eventConfigs.isSupportedOnRealtime() && (!expiredForRealtime || isImportantEvent(optimoveEvent))) {
            realtimeManager.reportEvent(optimoveEvent);
        }
//        if (eventConfigs.isSupportedOnOptitrack()) {
//            optitrackManager.reportEvent(optimoveEvent, eventConfigs);
//            if (eventContext.getExecutionTimeout() > 0) {
//                optitrackManager.setTimeout(eventContext.getExecutionTimeout());
//                optitrackManager.sendAllEventsNow();
//            }
//        }
    }

    private boolean isImportantEvent(OptimoveEvent optimoveEvent) {
        return (optimoveEvent.getName()
                .equals(SetUserIdEvent.EVENT_NAME) ||
                optimoveEvent.getName()
                        .equals(SetEmailEvent.EVENT_NAME));
    }
}
