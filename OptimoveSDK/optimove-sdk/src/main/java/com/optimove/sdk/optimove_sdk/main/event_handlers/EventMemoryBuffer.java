package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        if (next == null) {
            processEventInternally(optimoveEvents);
        } else {
            reportEventNext(optimoveEvents);
        }
    }


    private void processEventInternally(List<OptimoveEvent> optimoveEvents) {
        if (optimoveEventsBuffer.size() < maximumSize) {

            optimoveEventsBuffer.addAll(optimoveEvents);
        }
    }


    public void processQueue() {
        reportEventNext(optimoveEventsBuffer);
//        while (!optimoveEventsBuffer.isEmpty()) {
//            reportEventNext(new ArrayList<OptimoveEvent>(optimoveEventsBuffer.re()));
//        }
    }


}
