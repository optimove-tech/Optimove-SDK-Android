package com.optimove.sdk.optimove_sdk;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.common.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.common.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.notification_events.ScheduledNotificationDeliveredEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamDbHelper;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamPersistanceAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.main.common.OptistreamEventBuilder.Constants.CATEGORY_OPTIPUSH;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
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
        MockitoAnnotations.openMocks(this);

        when(httpClient.postJsonArray(any(), any())).thenReturn(builder);
        when(builder.errorListener(any())).thenReturn(builder);
        when(builder.destination(any(), any())).thenReturn(builder);
        when(builder.successListener(any())).thenReturn(builder);
    }

    @Test
    public void nonRealtimeEventsShouldBeStoredInOrder() throws Exception {

        int numOfEvents = 100;
        OptistreamPersistanceAdapter optistreamPersistanceAdapter = new MockedOptistreamPersistency();

        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver,
                optistreamPersistanceAdapter, optitrackConfigs);

        for (int i = 0; i < numOfEvents; i++) {
            optistreamHandler.reportEvents(Collections.singletonList(getRegularEvent(false, "some_name_" + i)));
        }

        Thread.sleep(1000);

        OptistreamPersistanceAdapter.EventsBulk eventsBulk = optistreamPersistanceAdapter.getFirstEvents(numOfEvents);

        List<String> dbEventJsons = eventsBulk.getEventJsons();
        Assert.assertEquals(dbEventJsons.size(), numOfEvents);

    }

    @Test
    public void eventsShouldBeDispatchedOnceAndInOrderInSpiteOfAThreadChaos() throws Exception {

        long maxResponseTime = 80;
        int numOfEvents = 40;
        OptistreamPersistanceAdapter optistreamPersistanceAdapter = new MockedOptistreamPersistency();

        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver,
                optistreamPersistanceAdapter, optitrackConfigs);
        applyHttpRandomDelaySuccessInvocation(maxResponseTime);

        //generating numOfEvents events with random realtime
        for (int i = 0; i < numOfEvents; i++) {
            optistreamHandler.reportEvents(Collections.singletonList(getRegularEvent(Math.random() > 0.5,
                    "some_name_" + i)));
        }

        //add more chaos by simulating the app going on background
        new Thread(() -> {
            try {
                Thread.sleep((long) (Math.random() * maxResponseTime));
                optistreamHandler.activityStopped();
            } catch (InterruptedException i) {

            }
        }).run();

        //The dispatch should be serial, it cant take than the maximum response time numOfEvents times, because those
        // are background threads, lets give it twice
        Thread.sleep(maxResponseTime * numOfEvents * 2);

        //verifying that all of the events exist and all of them are ordered
        verify(httpClient, new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                int i = 0;
                for (Invocation invocation : data.getAllInvocations()) {
                    for (int jsonObjectIndex = 0; jsonObjectIndex < ((JSONArray)invocation.getRawArguments()[1]).length(); jsonObjectIndex++) {
                        try {
                            Assert.assertEquals(((JSONArray) invocation.getRawArguments()[1]).getJSONObject(jsonObjectIndex)
                                    .getString("event"), "some_name_" + i);
                        } catch (Exception e) {
                            fail(e.getMessage());
                        }
                        i++;
                    }
                }
            }

            @Override
            public VerificationMode description(String description) {
                return null;
            }
        }).postJsonArray(any(), any());
    }

    @Test
    public void eventShouldBePersisted() {
        OptistreamEvent regularEvent = getRegularEvent(true, "some_name");
        String regularEventJson = new Gson().toJson(regularEvent);
        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver, optistreamDbHelper
                , optitrackConfigs);
        optistreamHandler.reportEvents(Collections.singletonList(regularEvent));
        verify(optistreamDbHelper, timeout(1000)).insertEvent(regularEventJson);
    }

    @Test
    public void eventsShouldBeRemovedWhenDispatchSucceed() {
        Gson gson = new Gson();
        String lastId = "some_id";
        OptistreamDbHelper.EventsBulk eventBulk = new OptistreamDbHelper.EventsBulk(lastId,
                Collections.singletonList(gson.toJson(getRegularEvent(false, "some_name"))));
        applyHttpSuccessInvocation();
        when(optistreamDbHelper.getFirstEvents(OptistreamHandler.Constants.EVENT_BATCH_LIMIT)).thenReturn(eventBulk,
                new OptistreamDbHelper.EventsBulk(null, new ArrayList<>()));

        OptistreamHandler optistreamHandler = new OptistreamHandler(httpClient, lifecycleObserver, optistreamDbHelper
                , optitrackConfigs);
        optistreamHandler.reportEvents(Collections.singletonList(getRegularEvent(true, "some_name")));

        verify(optistreamDbHelper, timeout(1000)).removeEvents(lastId);
    }

    @Test
    public void realtimeEventShouldBeDispatchedImmediately() throws Exception {
        OptistreamEvent regularEvent = getRegularEvent(true, "some_name");
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
    public void optipushCategoryEventShouldBeDispatchedImmediately() throws Exception {
        OptistreamEvent notificationEvent = getOptipushCategoryEvent();
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
        OptistreamEvent regularEvent = getRegularEvent(false, "some_name");
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

    private OptistreamEvent getRegularEvent(boolean isRealtime, String name) {
        OptistreamEvent.Metadata optistreamMetadata = mock(OptistreamEvent.Metadata.class);

        when(optistreamMetadata.isRealtime()).thenReturn(isRealtime);
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName(name)
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(optistreamMetadata)
                .build();
    }

    private OptistreamEvent getOptipushCategoryEvent() {
        OptistreamEvent.Metadata optistreamMetadata = mock(OptistreamEvent.Metadata.class);

        when(optistreamMetadata.isRealtime()).thenReturn(false);
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory(CATEGORY_OPTIPUSH)
                .withName(ScheduledNotificationDeliveredEvent.NAME)
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(optistreamMetadata)
                .build();
    }

    private void applyHttpSuccessInvocation() {
        doAnswer(invocation -> {
            new Thread(() -> {
                Response.Listener<JSONObject> successListener =
                        (Response.Listener<JSONObject>) invocation.getArguments()[0];
                JSONObject jsonObject = new JSONObject();

                successListener.onResponse(jsonObject);
            }).start();

            return builder;
        }).when(builder)
                .successListener(any());
    }

    private void applyHttpRandomDelaySuccessInvocation(long maximumDelay) {
        doAnswer(invocation -> {
            new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * maximumDelay));
                } catch (InterruptedException in) {

                }
                Response.Listener<JSONObject> successListener =
                        (Response.Listener<JSONObject>) invocation.getArguments()[0];
                JSONObject jsonObject = new JSONObject();

                successListener.onResponse(jsonObject);
            }).start();

            return builder;
        }).when(builder)
                .successListener(any());
    }

    private class MockedOptistreamPersistency implements OptistreamPersistanceAdapter {

        List<OptistreamEventEntry> optistreamEventsEntries = new ArrayList<>();
        int currentLastId = 1;

        @Override
        public boolean insertEvent(String eventJson) {
            optistreamEventsEntries.add(new OptistreamEventEntry(currentLastId, eventJson));
            currentLastId++;
            return true;
        }

        @Override
        public void removeEvents(String lastId) {
            // lastId is an index in this case
            for (int i = 0; i < Integer.valueOf(lastId); i++) {
                optistreamEventsEntries.remove(0);
            }
        }

        @Override
        public OptistreamPersistanceAdapter.EventsBulk getFirstEvents(int numberOfEvents) {
            List<String> optistreamEvents = new ArrayList<>();
            String lastId = "";
            for (int i = 0; i < numberOfEvents; i++) {
                if (i < optistreamEventsEntries.size()) {
                    optistreamEvents.add(optistreamEventsEntries.get(i)
                            .getOptistreamEventData());
                    lastId = String.valueOf(optistreamEventsEntries.get(i).id);
                }
            }
            return new OptistreamPersistanceAdapter.EventsBulk(lastId, optistreamEvents);
        }
    }

    private class OptistreamEventEntry {
        private int id;
        private String optistreamEventData;

        public OptistreamEventEntry(int id, String optistreamEventData) {
            this.id = id;
            this.optistreamEventData = optistreamEventData;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOptistreamEventData() {
            return optistreamEventData;
        }

        public void setOptistreamEventData(String optistreamEventData) {
            this.optistreamEventData = optistreamEventData;
        }
    }
}