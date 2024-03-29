package com.optimove.android.main.event_handlers;

import com.optimove.android.main.events.OptimoveEvent;

import java.util.List;


public abstract class EventHandler {

    protected EventHandler next;

    public void setNext(EventHandler next){
        this.next = next;
    }

    public abstract void reportEvent(List<OptimoveEvent> optimoveEvents);


    protected void reportEventNext(List<OptimoveEvent> optimoveEvents) {
        if (next == null) {
            return;
        }
        next.reportEvent(optimoveEvents);
    }

}
