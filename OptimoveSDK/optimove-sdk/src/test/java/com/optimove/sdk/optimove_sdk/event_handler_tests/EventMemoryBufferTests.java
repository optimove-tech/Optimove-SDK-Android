package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventMemoryBufferTests {

    private int bufferMaximumSize = 100;

    private EventMemoryBuffer eventMemoryBuffer;
    @Mock
    private EventHandler nextEventHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        eventMemoryBuffer = new EventMemoryBuffer(bufferMaximumSize);
    }

    @Test
    public void eventsShouldBePassedToNextIfNextSetAndNumberOfEventsIsLowerThanMaximum() {
        int numberOfEvents = 50;
        for (int i = 0; i< numberOfEvents ; i++){
            OptimoveEvent optimoveEvent = new SimpleCustomEvent("some_name", new HashMap<>());
            eventMemoryBuffer.reportEvent(Collections.singletonList(optimoveEvent));
        }
        eventMemoryBuffer.setNext(nextEventHandler);

        verify(nextEventHandler, times(1)).reportEvent(any());
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.size()
               , numberOfEvents)));
    }
    @Test
    public void maximumSizeNumberOfEventsShouldBePassedToNextIfBiggerThanMaximumSizeReportedAndNextSet() {
        int numberOfEvents = 150;
        for (int i= 0; i< numberOfEvents ; i++){
            OptimoveEvent optimoveEvent = new SimpleCustomEvent("some_name", new HashMap<>());
            eventMemoryBuffer.reportEvent(Collections.singletonList(optimoveEvent));
        }
        eventMemoryBuffer.setNext(nextEventHandler);

        verify(nextEventHandler, times(1)).reportEvent(any());
        verify(nextEventHandler).reportEvent(assertArg(arg -> Assert.assertEquals(arg.size()
                , bufferMaximumSize)));
    }
}
