package com.optimove.sdk.optimove_sdk.main;

import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.event_handlers.DestinationDecider;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandlerProvider {

    private final Object lockObj = new Object();

    private EventHandlerFactory eventHandlerFactory;
    private ExecutorService singleThreadExecutor;

    private EventSynchronizer eventSynchronizer;
    private EventMemoryBuffer eventMemoryBuffer;


    public EventHandlerProvider(EventHandlerFactory eventHandlerFactory) {
        this.eventHandlerFactory = eventHandlerFactory;
        this.singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public EventHandler getEventHandler() {
        ensureHandlersInitialization();
        return eventSynchronizer;
    }

    private void ensureHandlersInitialization() {
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
            RealtimeManager realtimeManager = eventHandlerFactory.getRealtimeMananger(configs.getRealtimeConfigs());
            OptistreamHandler optistreamHandler =
                    eventHandlerFactory.getOptistreamHandler(configs.getOptitrackConfigs());
            OptistreamEventBuilder optistreamEventBuilder =
                    eventHandlerFactory.getOptistreamEventBuilder(configs.getTenantId(), configs.isAirship() ?
                            getAirshipMetadata() : null);
            DestinationDecider destinationDecider =
                    eventHandlerFactory.getDestinationDecider(configs.getEventsConfigs(), optistreamHandler,
                            realtimeManager, optistreamEventBuilder, configs.isEnableRealtime(), configs.isEnableRealtimeThroughOptistream());

            eventNormalizer.setNext(eventValidator);
            eventValidator.setNext(eventDecorator);
            //optistream
            eventDecorator.setNext(destinationDecider);

            eventMemoryBuffer.setNext(eventNormalizer);
        });

    }

    private @Nullable OptistreamEvent.AirshipMetadata getAirshipMetadata(){
        try {
            Class<?> airshipClass = Class.forName("com.urbanairship.UAirship");

            Method sharedMethod = airshipClass.getMethod("shared");
            Object uAirshipClassInstance = sharedMethod.invoke(null);

            Method getChannelMethod = airshipClass.getMethod("getChannel");
            Object airShipChannelInstance = getChannelMethod.invoke(uAirshipClassInstance);

            Class<?> airshipChannelClass = Class.forName("com.urbanairship.channel.AirshipChannel");
            Method getIdMethod = airshipChannelClass.getMethod("getId");
            Object channelId = getIdMethod.invoke(airShipChannelInstance);

            Method getAirshipConfigOptionsMethod = airshipClass.getMethod("getAirshipConfigOptions");
            Object airshipUrlConfigInstance = getAirshipConfigOptionsMethod.invoke(uAirshipClassInstance);


            Class<?> airshipConfigOptionsClass = Class.forName("com.urbanairship.AirshipConfigOptions");

            Field field = airshipConfigOptionsClass.getDeclaredField("appKey");

            Object appKey = field.get(airshipUrlConfigInstance);

            String appKeyString = String.valueOf(appKey);
            String channelIdString = String.valueOf(channelId);

            if (appKeyString.equals("null") || (channelIdString.equals("null")) ){
                OptiLoggerStreamsContainer.error("Airship not available - either appKey or channelId were not found");
                return null;
            } else {
                return new OptistreamEvent.AirshipMetadata(channelIdString, appKeyString);
            }

        } catch (Exception e) {
            OptiLoggerStreamsContainer.error("Airship not available - %s", e.getMessage());
            return null;
        }

    }


}
