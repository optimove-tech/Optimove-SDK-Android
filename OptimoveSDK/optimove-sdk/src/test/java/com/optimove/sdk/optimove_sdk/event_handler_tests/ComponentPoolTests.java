package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ComponentPoolTests {

    @Mock
    private Map<String, EventConfigs> eventConfigsMap;
    @Mock
    private OptitrackManager optitrackManager;
    @Mock
    private RealtimeManager realtimeManager;

    private ComponentPool componentPool;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        componentPool = new ComponentPool(eventConfigsMap, optitrackManager, realtimeManager);
    }

    @Test
    public void eventShouldntBeReportedToRealtimeIfNotSupportedOnRealtime() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(false);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);
        componentPool.reportEvent(eventContext);
        verifyZeroInteractions(realtimeManager);

    }

    @Test
    public void eventShouldntBeReportedToRealtimeIfOld() throws InterruptedException {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);
        Thread.sleep(2000);
        componentPool.reportEvent(eventContext);
        verifyZeroInteractions(realtimeManager);
    }

    @Test
    public void userIdEventShouldBeReportedToRealtimeIfOld() throws InterruptedException {
        SetUserIdEvent setUserIdEvent = new SetUserIdEvent("some_original_visitor_id", "some_user_id",
                "some_updated_visitor_id");
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(SetUserIdEvent.EVENT_NAME)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);


        EventContext eventContext = new EventContext(setUserIdEvent);
        Thread.sleep(2000);
        componentPool.reportEvent(eventContext);

        verify(realtimeManager).reportEvent(setUserIdEvent);

    }

    @Test
    public void emailEventShouldBeReportedToRealtimeIfOld() throws InterruptedException {
        SetEmailEvent setEmailEvent = new SetEmailEvent("some_email");
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(SetEmailEvent.EVENT_NAME)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);


        EventContext eventContext = new EventContext(setEmailEvent);
        Thread.sleep(2000);
        componentPool.reportEvent(eventContext);

        verify(realtimeManager).reportEvent(setEmailEvent);
    }

    @Test
    public void eventShouldntBeReportedToOptitrackIfNotSupportedOnOptitrack() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnOptitrack()).thenReturn(false);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);
        componentPool.reportEvent(eventContext);
        verifyZeroInteractions(optitrackManager);
    }
    @Test
    public void eventShouldBeReportedToOptitrackIfSupportedOnOptitrack() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnOptitrack()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent);
        componentPool.reportEvent(eventContext);
        verify(optitrackManager).reportEvent(optimoveEvent,eventConfigs);
    }
    @Test
    public void eventShouldBeReportedToOptitrackAndDispatchedIfProcessingTimeout() {
        String eventName = "some_event_name";
        int timeout = 5;
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnOptitrack()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        EventContext eventContext = new EventContext(optimoveEvent, timeout);
        componentPool.reportEvent(eventContext);
        InOrder inOrder = inOrder(optitrackManager);
        inOrder.verify(optitrackManager).reportEvent(optimoveEvent,eventConfigs);
        inOrder.verify(optitrackManager).setTimeout(timeout);
        inOrder.verify(optitrackManager).sendAllEventsNow();
    }
}
