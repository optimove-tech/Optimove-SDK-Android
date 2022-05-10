package com.optimove.android.event_handler_tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;

import com.optimove.android.main.event_handlers.EventHandler;
import com.optimove.android.main.event_handlers.EventMemoryBuffer;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.main.events.SimpleCustomEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventMemoryBufferTests {

    private int bufferMaximumSize = 100;

    private EventMemoryBuffer eventMemoryBuffer;
    @Mock
    private EventHandler nextEventHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
    @Test
    public void reportEventcaseNextIsNull(){
        List<OptimoveEvent> events = Collections.singletonList(new SimpleCustomEvent("some_name", new HashMap<>()));
        eventMemoryBuffer.setNext(nextEventHandler);
        eventMemoryBuffer.reportEvent(events);
        verify(nextEventHandler).reportEvent(events);

    }
}
