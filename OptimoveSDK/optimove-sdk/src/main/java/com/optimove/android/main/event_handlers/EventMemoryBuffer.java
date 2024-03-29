package com.optimove.android.main.event_handlers;

import com.optimove.android.main.events.OptimoveEvent;

import java.util.ArrayList;
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
        reportEventNext(new ArrayList<>(optimoveEventsBuffer));
        optimoveEventsBuffer.clear();
    }


}
