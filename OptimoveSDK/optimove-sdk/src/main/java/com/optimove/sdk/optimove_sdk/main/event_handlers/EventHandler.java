package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;


public abstract class EventHandler {



    protected EventHandler next;

    public void setNext(EventHandler next){
        this.next = next;
    }

    public abstract void reportEvent(OptimoveEvent optimoveEvent);


    protected void reportEventNext(OptimoveEvent optimoveEvent) {
        if (next == null) {
            return;
        }
        next.reportEvent(optimoveEvent);
    }

}
