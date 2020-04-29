package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.LinkedList;

public class EventMemoryBuffer extends EventHandler {


    private LinkedList<OptimoveEvent> optimoveEventsBuffer;
    private int maximumSize;



    public EventMemoryBuffer(int maximumSize) {
        this.optimoveEventsBuffer = new LinkedList<>();
        this.maximumSize = maximumSize;
    }

    @Override
    public void setNext(EventHandler next) {
        this.next = next;
        processQueue();
    }

    @Override
    public void reportEvent(OptimoveEvent optimoveEvent) {
        if (next == null) {
            processEventInternally(optimoveEvent);
        } else {
            reportEventNext(optimoveEvent);
        }
    }


    private void processEventInternally(OptimoveEvent optimoveEvent) {
        if (optimoveEventsBuffer.size() < maximumSize) {
            optimoveEventsBuffer.push(optimoveEvent);
        }
    }


    public void processQueue() {
        while (!optimoveEventsBuffer.isEmpty()) {
            reportEventNext(optimoveEventsBuffer.remove());
        }
    }


}
