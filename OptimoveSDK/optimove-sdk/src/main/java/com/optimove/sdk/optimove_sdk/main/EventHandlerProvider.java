package com.optimove.sdk.optimove_sdk.main;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandlerProvider {

    private final Object lockObj = new Object();

    private EventHandlerFactory eventHandlerFactory;
    private ExecutorService singleThreadExecutor;

    private EventSynchronizer eventSynchronizer;
    private EventMemoryBuffer eventMemoryBuffer;

    private LifecycleObserver lifecycleObserver;

    public EventHandlerProvider(EventHandlerFactory eventHandlerFactory, LifecycleObserver lifecycleObserver) {
        this.eventHandlerFactory = eventHandlerFactory;
        this.singleThreadExecutor = Executors.newSingleThreadExecutor();
        this.lifecycleObserver = lifecycleObserver;
    }

    public EventHandler getEventHandler() {
        ensureHandlersInitialization();
        return eventSynchronizer;
    }
    private void ensureHandlersInitialization(){
        synchronized (lockObj) {
            if (eventSynchronizer == null) {
                this.eventMemoryBuffer = eventHandlerFactory.getEventBuffer();
                this.eventSynchronizer = eventHandlerFactory.getEventSynchronizer(singleThreadExecutor);
                this.eventSynchronizer.setNext(eventMemoryBuffer);
            }
        }
    }

    public void processConfigs(Configs configs) {
        ensureHandlersInitialization();
        singleThreadExecutor.submit(() -> {
            EventNormalizer eventNormalizer = eventHandlerFactory.getEventNormalizer();
            EventValidator eventValidator = eventHandlerFactory.getEventValidator(configs.getEventsConfigs());
            EventDecorator eventDecorator = eventHandlerFactory.getEventDecorator(configs.getEventsConfigs());
            OptistreamHandler optistreamHandler = eventHandlerFactory.getOptistreamHandler(configs.getOptitrackConfigs(),
                    configs.getEventsConfigs(), lifecycleObserver);

            eventNormalizer.setNext(eventValidator);
            eventValidator.setNext(eventDecorator);
            //optistream
            eventDecorator.setNext(optistreamHandler);

            eventMemoryBuffer.setNext(eventNormalizer);
        });

    }


}
