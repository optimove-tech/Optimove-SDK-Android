package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.EventContext;

import java.util.LinkedList;

public class EventMemoryBuffer extends EventHandler {


    private LinkedList<EventContext> optimoveEventsBuffer;
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
    public void reportEvent(EventContext eventContext) {
        if (next == null) {
            processEventInternally(eventContext);
        } else {
            reportEventNext(eventContext);
        }
    }


    private void processEventInternally(EventContext eventContext) {
        if (optimoveEventsBuffer.size() < maximumSize) {
            optimoveEventsBuffer.push(eventContext);
        }
    }


    public void processQueue() {
        while (!optimoveEventsBuffer.isEmpty()) {
            reportEventNext(optimoveEventsBuffer.remove());
        }
    }


}
