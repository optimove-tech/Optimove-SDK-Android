package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.EventContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventSynchronizer extends EventHandler {

    private ExecutorService singleThreadExecutor;

    public EventSynchronizer(ExecutorService executorService) {
        this.singleThreadExecutor = executorService;
    }

    @Override
    public void reportEvent(EventContext eventContext) {
        singleThreadExecutor.submit(() ->
            this.reportEventNext(eventContext));
    }

}
