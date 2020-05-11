package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptistreamEventBuilder;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DestinationDecider extends EventHandler {

    private OptistreamHandler optistreamHandler;
    private RealtimeManager realtimeManager;
    private Map<String, EventConfigs> eventConfigsMap;
    private boolean realtimeEnabled;
    private boolean realtimeEnabledThroughOptistream;
    private OptistreamEventBuilder optistreamEventBuilder;


    public DestinationDecider(Map<String, EventConfigs> eventConfigsMap, OptistreamHandler optistreamHandler,
                              RealtimeManager realtimeManager, OptistreamEventBuilder optistreamEventBuilder, boolean realtimeEnabled,
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

        for (OptimoveEvent optimoveEvent: optimoveEvents) {
            EventConfigs eventConfigs = eventConfigsMap.get(optimoveEvent.getName());

            OptistreamEvent optistreamEvent;
            if (!realtimeEnabled || !eventConfigs.isSupportedOnRealtime()){
                // Only optistream
                optistreamEvent = optistreamEventBuilder.convertOptimoveToOptistreamEvent(optimoveEvent,
                        false);
                optistreamEvents.add(optistreamEvent);
            } else if (realtimeEnabledThroughOptistream) {
                // Only to optistream, with realtime enabled for Optistream
                optistreamEvent = optistreamEventBuilder.convertOptimoveToOptistreamEvent(optimoveEvent,
                        true);
                optistreamEvents.add(optistreamEvent);
            } else {
                // Both to optistream and to realtime, with realtime disabled for Optistream
                optistreamEvent = optistreamEventBuilder.convertOptimoveToOptistreamEvent(optimoveEvent,
                        false);
                optistreamEvents.add(optistreamEvent);
                optistreamRealtimeEvents.add(optistreamEvent);
            }
        }
        if (!optistreamRealtimeEvents.isEmpty()) {
            realtimeManager.reportEvents(optistreamRealtimeEvents);
        }
        //optistreamHandler.reportEvent(optistreamEvent);
    }

}
