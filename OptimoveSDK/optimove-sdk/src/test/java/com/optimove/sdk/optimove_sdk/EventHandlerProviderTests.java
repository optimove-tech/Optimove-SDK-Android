package com.optimove.sdk.optimove_sdk;

import com.optimove.sdk.optimove_sdk.main.common.EventHandlerFactory;
import com.optimove.sdk.optimove_sdk.main.common.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.event_handlers.DestinationDecider;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHandlerProviderTests {

    private EventHandlerProvider eventHandlerProvider;

    @Mock
    private EventHandlerFactory eventHandlerFactory;
    @Mock
    private DestinationDecider destinationDecider;
    @Mock
    private EventDecorator eventDecorator;
    @Mock
    private EventNormalizer eventNormalizer;
    @Mock
    private EventMemoryBuffer eventMemoryBuffer;
    @Mock
    private EventSynchronizer eventSynchronizer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        eventHandlerProvider = new EventHandlerProvider(eventHandlerFactory);
        when(eventHandlerFactory.getDestinationDecider(any(), any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(destinationDecider);
        when(eventHandlerFactory.getEventDecorator(any(), anyInt())).thenReturn(eventDecorator);
        when(eventHandlerFactory.getEventNormalizer(anyInt())).thenReturn(eventNormalizer);
        when(eventHandlerFactory.getEventBuffer()).thenReturn(eventMemoryBuffer);
        when(eventHandlerFactory.getEventSynchronizer(any())).thenReturn(eventSynchronizer);
    }

    @Test
    public void getEventHandlerShouldBuildBasicChainOfSynchronizerAndBuffer() {
        EventHandler eventHandler = eventHandlerProvider.getEventHandler();
        Assert.assertTrue(eventHandler instanceof EventSynchronizer);
        verify(eventSynchronizer).setNext(eventMemoryBuffer);
        verify(eventMemoryBuffer, times(0)).setNext(any());
    }


    @Test
    public void setConfigsShouldBuildAFullChain() {
        Configs configs = mock(Configs.class);
        when(configs.getOptitrackConfigs()).thenReturn(mock(OptitrackConfigs.class));
        eventHandlerProvider.processConfigs(configs);
        verify(eventSynchronizer, timeout(1000)).setNext(eventMemoryBuffer);
        verify(eventMemoryBuffer, timeout(1000)).setNext(eventNormalizer);
        verify(eventNormalizer, timeout(1000)).setNext(eventDecorator);
        verify(eventDecorator, timeout(1000)).setNext(destinationDecider);
        Assert.assertTrue(eventHandlerProvider.getEventHandler() instanceof EventSynchronizer);


    }
}
