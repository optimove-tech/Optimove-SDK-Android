package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.common.OptistreamEventBuilder;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DestinationDecider extends EventHandler {

    private final OptistreamHandler optistreamHandler;
    private final RealtimeManager realtimeManager;
    private final Map<String, EventConfigs> eventConfigsMap;
    private final boolean realtimeEnabled;
    private final boolean realtimeEnabledThroughOptistream;
    private final OptistreamEventBuilder optistreamEventBuilder;


    public DestinationDecider(Map<String, EventConfigs> eventConfigsMap, OptistreamHandler optistreamHandler,
                              RealtimeManager realtimeManager, OptistreamEventBuilder optistreamEventBuilder,
                              boolean realtimeEnabled,
                              boolean realtimeEnabledThroughOptistream) {
        this.eventConfigsMap = eventConfigsMap;
        this.optistreamHandler = optistreamHandler;
        this.realtimeManager = realtimeManager;
        this.optistreamEventBuilder = optistreamEventBuilder;
        this.realtimeEnabled = realtimeEnabled;
        this.realtimeEnabledThroughOptistream = realtimeEnabledThroughOptistream;
    }

    @Override
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        List<OptistreamEvent> optistreamRealtimeEvents = new ArrayList<>();
        List<OptistreamEvent> optistreamEvents = new ArrayList<>();

        for (OptimoveEvent optimoveEvent : optimoveEvents) {
            EventConfigs eventConfigs = eventConfigsMap.get(optimoveEvent.getName());

            OptistreamEvent optistreamEvent = optistreamEventBuilder.convertOptimoveToOptistreamEvent(optimoveEvent,
                    realtimeEnabledThroughOptistream);
            optistreamEvents.add(optistreamEvent);
            if (eventConfigs != null && realtimeEnabled && eventConfigs.isSupportedOnRealtime() && !realtimeEnabledThroughOptistream){
                optistreamRealtimeEvents.add(optistreamEvent);
            }
        }
        if (!optistreamRealtimeEvents.isEmpty()) {
            realtimeManager.reportEvents(optistreamRealtimeEvents);
        }
        if (!optistreamEvents.isEmpty()) {
            optistreamHandler.reportEvents(optistreamEvents);
        }
    }

}
