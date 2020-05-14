package com.optimove.sdk.optimove_sdk;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamDbHelper;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class OptitrackTests {

    @Mock
    HttpClient.RequestBuilder<JSONObject> builder;
    @Mock
    UserInfo userInfo;
    @Mock
    LifecycleObserver lifecycleObserver;
    @Mock
    OptitrackConfigs optitrackConfigs;
    @Mock
    HttpClient httpClient;
    @Mock
    OptistreamDbHelper optistreamDbHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(httpClient.postJsonArray(anyString(), any())).thenReturn(builder);
        when(builder.errorListener(any())).thenReturn(builder);
        when(builder.destination(any(), any())).thenReturn(builder);
        when(builder.successListener(any())).thenReturn(builder);
    }

    @Test
    public void realtimeEventShouldBeDispatchedImmediately() throws Exception {
        OptistreamEvent regularEvent = getRegularEvent(true);
        String regularEventJson = new Gson().toJson(regularEvent);
        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver, optistreamDbHelper
                , optitrackConfigs);
        OptistreamDbHelper.EventsBulk eventBulk = new OptistreamDbHelper.EventsBulk("1",
                Collections.singletonList(regularEventJson));
        when(optistreamDbHelper.getFirstEvents(anyInt())).thenReturn(eventBulk);

        optistreamHandler.reportEvents(Collections.singletonList(regularEvent));


        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(any(), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), "some_name");
    }
    @Test
    public void notificationEventShouldBeDispatchedImmediately() throws Exception {
        OptistreamEvent notificationEvent = getNotificationEvent();
        String notificationEventJson = new Gson().toJson(notificationEvent);
        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver, optistreamDbHelper
                , optitrackConfigs);
        OptistreamDbHelper.EventsBulk eventBulk = new OptistreamDbHelper.EventsBulk("1",
                Collections.singletonList(notificationEventJson));
        when(optistreamDbHelper.getFirstEvents(anyInt())).thenReturn(eventBulk);

        optistreamHandler.reportEvents(Collections.singletonList(notificationEvent));


        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(any(), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), ScheduledNotificationDeliveredEvent.NAME);
    }
    @Test
    public void nonRealtimeEventShouldntBeDispatchedImmediately() throws Exception {
        OptistreamEvent regularEvent = getRegularEvent(false);
        String regularEventJson = new Gson().toJson(regularEvent);
        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver, optistreamDbHelper
                , optitrackConfigs);
        OptistreamDbHelper.EventsBulk eventBulk = new OptistreamDbHelper.EventsBulk("1",
                Collections.singletonList(regularEventJson));
        when(optistreamDbHelper.getFirstEvents(anyInt())).thenReturn(eventBulk);

        optistreamHandler.reportEvents(Collections.singletonList(regularEvent));

        Thread.sleep(100);
        verifyZeroInteractions(httpClient);
    }
    private OptistreamEvent getRegularEvent(boolean isRealtime) {
        OptistreamEvent.Metadata optistreamMetadata = mock(OptistreamEvent.Metadata.class);

        when(optistreamMetadata.isRealtime()).thenReturn(isRealtime);
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName("some_name")
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(optistreamMetadata)
                .build();
    }
    private OptistreamEvent getNotificationEvent() {
        OptistreamEvent.Metadata optistreamMetadata = mock(OptistreamEvent.Metadata.class);

        when(optistreamMetadata.isRealtime()).thenReturn(false);
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName(ScheduledNotificationDeliveredEvent.NAME)
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(optistreamMetadata)
                .build();
    }

}