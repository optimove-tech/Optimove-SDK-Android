package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.EventContext;


public abstract class EventHandler {



    protected EventHandler next;

    public void setNext(EventHandler next){
        this.next = next;
    }

    public abstract void reportEvent(EventContext eventContext);


    protected void reportEventNext(EventContext eventContext) {
        if (next == null) {
            return;
        }
        next.reportEvent(eventContext);
    }

}
