package com.optimove.android.event_handler_tests;

import com.optimove.android.main.event_handlers.EventHandler;
import com.optimove.android.main.event_handlers.EventSynchronizer;
import com.optimove.android.main.events.OptimoveEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class EventSynchronizerTests {

    @Mock
    private EventHandler nextEventHandler;

    private EventSynchronizer eventSynchronizer;
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        eventSynchronizer = new EventSynchronizer(Executors.newSingleThreadExecutor());
        eventSynchronizer.setNext(nextEventHandler);
    }

    @Test
    public void eventShouldBeReportedToNextHandler() {
        List<OptimoveEvent> optimoveEvents = mock(ArrayList.class);
        eventSynchronizer.reportEvent(optimoveEvents);
        verify(nextEventHandler,timeout(1000)).reportEvent(optimoveEvents);
    }
}
