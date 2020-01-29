package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventHandler;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;
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
        MockitoAnnotations.initMocks(this);
        eventSynchronizer = new EventSynchronizer(Executors.newSingleThreadExecutor());
        eventSynchronizer.setNext(nextEventHandler);
    }

    @Test
    public void eventShouldBeSubmitedToExecutor() {
        EventContext eventContext = mock(EventContext.class);
        eventSynchronizer.reportEvent(eventContext);
        verify(nextEventHandler,timeout(1000)).reportEvent(eventContext);
    }
}
