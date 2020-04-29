package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventMemoryBufferTests {

    private int maximumSize = 100;

    private EventMemoryBuffer eventMemoryBuffer;
    @Mock
    private EventHandler nextEventHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        eventMemoryBuffer = new EventMemoryBuffer(maximumSize);
    }

    @Test
    public void eventsShouldBePassedToNextIfNextSetAndNumberOfEventsIsLowerThanMaximum() {
        int numberOfEvents = 50;
        for (int i= 0; i< numberOfEvents ; i++){
            OptimoveEvent optimoveEvent = new SimpleCustomEvent("some_name", new HashMap<>());
            EventContext eventContext = new EventContext(optimoveEvent);
            eventMemoryBuffer.reportEvent(eventContext);
        }
        eventMemoryBuffer.setNext(nextEventHandler);

        verify(nextEventHandler, times(numberOfEvents)).reportEvent(any());
    }
    @Test
    public void maximumSizeNumberOfEventsShouldBePassedToNextIfBiggerThanMaximumSizeReportedAndNextSet() {
        int numberOfEvents = 150;
        for (int i= 0; i< numberOfEvents ; i++){
            OptimoveEvent optimoveEvent = new SimpleCustomEvent("some_name", new HashMap<>());
            EventContext eventContext = new EventContext(optimoveEvent);
            eventMemoryBuffer.reportEvent(eventContext);
        }
        eventMemoryBuffer.setNext(nextEventHandler);

        verify(nextEventHandler, times(maximumSize)).reportEvent(any());
    }
}
