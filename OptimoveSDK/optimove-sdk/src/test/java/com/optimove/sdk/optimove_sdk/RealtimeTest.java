//package com.optimove.sdk.optimove_sdk;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//import com.android.volley.ParseError;
//import com.android.volley.Response;
//import com.optimove.sdk.optimove_sdk.main.UserInfo;
//import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
//import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
//import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
//import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
//import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
//import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
//import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InOrder;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_DEVICE_TYPE_PARAM_KEY;
//import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_NATIVE_MOBILE_PARAM_KEY;
//import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_OS_PARAM_KEY;
//import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_PLATFORM_PARAM_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.DID_FAIL_SET_EMAIL_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.DID_FAIL_SET_USER_ID_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_CID_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_CONTEXT_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_EID_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_FIRST_VISITOR_DATE_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_TID_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_REQUEST_VID_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_RESPONSE_DATA_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_RESPONSE_SUCCESS_KEY;
//import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.FIRST_VISIT_TIMESTAMP_KEY;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.booleanThat;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.inOrder;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.timeout;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class RealtimeTest {
//
//
//    @Mock
//    Context context;
//    @Mock
//    UserInfo userInfo;
//    @Mock
//    SharedPreferences sharedPreferences;
//    @Mock
//    SharedPreferences.Editor editor;
//    @Mock
//    HttpClient httpClient;
//    @Mock
//    HttpClient.RequestBuilder<JSONObject> builder;
//    @Mock
//    EventConfigs setUserIdEventConfig;
//    @Mock
//    EventConfigs setEmailEventConfig;
//    @Mock
//    EventConfigs generalOptimoveEventConfig;
//    @Mock
//    OptimoveEvent generalOptimoveEvent;
//    @Mock
//    Map<String, EventConfigs> eventConfigsMap;
//    @Mock
//    RealtimeConfigs realtimeConfigs;
//
//    private RealtimeManager realtimeManager;
//    private int setUserEventId = 300;
//    private int setEmailEventId = 301;
//    private int generalOptimoveEventId = 303;
//    private String realtimeGetawayUrl = "some_url";
//    private String realtimeToken = "some_token";
//    private String generalEventName = "some_name";
//
//    private String userId = "some_user_id_from_info";
//    private String userEmail = "some_user_email_frm_info";
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
//        when(sharedPreferences.edit()).thenReturn(editor);
//        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
//        when(sharedPreferences.getBoolean(DID_FAIL_SET_EMAIL_KEY, false)).thenReturn(false);
//        when(sharedPreferences.getBoolean(DID_FAIL_SET_USER_ID_KEY, false)).thenReturn(false);
//        when(sharedPreferences.contains(FIRST_VISIT_TIMESTAMP_KEY)).thenReturn(true);
//
//
//        when(eventConfigsMap.get(SetUserIdEvent.EVENT_NAME)).thenReturn(setUserIdEventConfig);
//        when(eventConfigsMap.get(SetEmailEvent.EVENT_NAME)).thenReturn(setEmailEventConfig);
//        when(eventConfigsMap.get(generalEventName)).thenReturn(generalOptimoveEventConfig);
//
//        when(generalOptimoveEvent.getName()).thenReturn(generalEventName);
//        when(generalOptimoveEventConfig.getId()).thenReturn(generalOptimoveEventId);
//        when(setUserIdEventConfig.getId()).thenReturn(setUserEventId);
//        when(setEmailEventConfig.getId()).thenReturn(setEmailEventId);
//
//        when(realtimeConfigs.getRealtimeGateway()).thenReturn(realtimeGetawayUrl);
//        when(realtimeConfigs.getRealtimeToken()).thenReturn(realtimeToken);
//
//        when(httpClient.postJson(anyString(), any())).thenReturn(builder);
//        when(builder.errorListener(any())).thenReturn(builder);
//        when(builder.destination(any(), any())).thenReturn(builder);
//        when(builder.successListener(any())).thenReturn(builder);
//
//        when(userInfo.getUserId()).thenReturn(userId);
//        when(userInfo.getEmail()).thenReturn(userEmail);
//        realtimeManager = new RealtimeManager(httpClient,
//                realtimeConfigs, eventConfigsMap, userInfo,
//                context);
//
//    }
//
//    @Test
//    public void firstVisitTimestampKeyShouldBeUpdatedIfFirstTime() {
//        when(sharedPreferences.contains(FIRST_VISIT_TIMESTAMP_KEY)).thenReturn(false);
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        InOrder inOrder = inOrder(editor);
//        inOrder.verify(editor)
//                .putLong(eq(FIRST_VISIT_TIMESTAMP_KEY), anyLong());
//        inOrder.verify(editor)
//                .apply();
//    }
//
//
//    @Test
//    public void userIdEventShouldBeIndicatedAsFailedIfHttpError() {
//        doAnswer(invocation -> {
//            Response.ErrorListener errorListener =
//                    (Response.ErrorListener) invocation.getArguments()[0];
//            errorListener.onErrorResponse(mock(ParseError.class));
//            return builder;
//        }).when(builder)
//                .errorListener(any());
//
//        when(builder.successListener(any())).thenReturn(builder);
//
//        realtimeManager.reportEvent(new SetUserIdEvent("asdfdsf", "asdfgsfd", "sdfsfsdf"));
//
//        verify(editor, timeout(1000)).putBoolean(DID_FAIL_SET_USER_ID_KEY, true);
//    }
//
//    @Test
//    public void userEmailShouldBeIndicatedAsFailedIfHttpError() {
//        doAnswer(invocation -> {
//            Response.ErrorListener errorListener =
//                    (Response.ErrorListener) invocation.getArguments()[0];
//            errorListener.onErrorResponse(mock(ParseError.class));
//            return builder;
//        }).when(builder)
//                .errorListener(any());
//
//        when(builder.successListener(any())).thenReturn(builder);
//
//        realtimeManager.reportEvent(new SetEmailEvent("some_email"));
//
//        verify(editor, timeout(1000)).putBoolean(DID_FAIL_SET_EMAIL_KEY, true);
//    }
//
//    @Test
//    public void realTimeEventShouldContainOnlyUserIdAndNotVisitorId() throws JSONException {
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//
//
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//
//        Assert.assertEquals(jsonObject.getString(EVENT_REQUEST_CID_KEY), userId);
//        Assert.assertFalse(jsonObject.has(EVENT_REQUEST_VID_KEY));
//    }
//
//    @Test
//    public void realTimeEventShouldContainOnlyVisitorIdAndNotUserIdIfUserIdNull() throws JSONException {
//        String originalVisitorId = "some_visitor_id";
//
//        when(userInfo.getUserId()).thenReturn(null);
//        when(userInfo.getVisitorId()).thenReturn(originalVisitorId);
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//
//
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//        Assert.assertEquals(jsonObject.getString(EVENT_REQUEST_VID_KEY), originalVisitorId);
//        Assert.assertFalse(jsonObject.has(EVENT_REQUEST_CID_KEY));
//    }
//
//    @Test
//    public void reportedByNameEventShouldBeTransferredWithCorrectId() throws JSONException {
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//        Assert.assertEquals(jsonObject.getInt(EVENT_REQUEST_EID_KEY), generalOptimoveEventId);
//
//    }
//
//    @Test
//    public void eventsShouldBeSentWithCorrectToken() throws JSONException {
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//
//        Assert.assertEquals(jsonObject.get(EVENT_REQUEST_TID_KEY), realtimeToken);
//    }
//
//    @Test
//    public void allEventParametersShouldBeSentToHTTPCorrectly() throws JSONException {
//        Map<String, Object> params = new HashMap<>(2);
//        params.put("first_param", "first_param");
//        params.put("second_param", "second_param");
//
//        when(generalOptimoveEvent.getParameters()).thenReturn(params);
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//        JSONObject context = jsonObject.getJSONObject(EVENT_REQUEST_CONTEXT_KEY);
//
//        Assert.assertTrue(context.has("first_param"));
//        Assert.assertTrue(context.has("second_param"));
//    }
//
//
//    @Test
//    public void firstTimeVisitorDateShouldntBeChangedIfExistsInStorage() {
//        when(sharedPreferences.contains(FIRST_VISIT_TIMESTAMP_KEY)).thenReturn(true);
//        long firstVisitTimestamp = 123;
//        when(sharedPreferences.getLong(eq(FIRST_VISIT_TIMESTAMP_KEY), anyLong())).thenReturn(firstVisitTimestamp);
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(1000)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getValue();
//        try {
//            Assert.assertEquals(jsonObject.getLong(EVENT_REQUEST_FIRST_VISITOR_DATE_KEY), firstVisitTimestamp);
//        } catch (JSONException e) {
//            Assert.fail();
//        }
//
//    }
//
//    //
////    //endregion General tests
////
////    //region Regular events
////
//    @Test
//    public void shouldReportSetUserFirstIfARegularEventIsSentAndThereIsFailedUserIdEvent() throws JSONException {
//        when(sharedPreferences.getBoolean(eq(DID_FAIL_SET_USER_ID_KEY), anyBoolean())).thenReturn(true);
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJson = ArgumentCaptor.forClass(JSONObject.class);
//        applyHttpSuccessInvocation();
//
//        verify(httpClient, timeout(2000).times(2)).postJson(eq(realtimeGetawayUrl),
//                httpSentJson.capture());
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(0)
//                .getInt(EVENT_REQUEST_EID_KEY), setUserEventId);
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(0)
//                .getString(EVENT_REQUEST_CID_KEY), userId);
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(1)
//                .getInt(EVENT_REQUEST_EID_KEY), generalOptimoveEventId);
//    }
//
//    @Test(timeout = 2000)
//    public void shouldReportSetEmailFirstIfARegularEventIsSentAndThereIsFailedEmailEvent() throws JSONException {
//        when(sharedPreferences.getBoolean(eq(DID_FAIL_SET_EMAIL_KEY), anyBoolean())).thenReturn(true);
//        applyHttpSuccessInvocation();
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJson = ArgumentCaptor.forClass(JSONObject.class);
//
//        verify(httpClient, timeout(1000).times(2)).postJson(eq(realtimeGetawayUrl),
//                httpSentJson.capture());
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(0)
//                .getInt(EVENT_REQUEST_EID_KEY), setEmailEventId);
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(1)
//                .getInt(EVENT_REQUEST_EID_KEY), generalOptimoveEventId);
//
//    }
//
//
//    @Test
//    public void shouldReportSetEmailAndUserFirstIfARegularEventIsSentAndThereAreFailedEmailAndUsersEvents() throws JSONException {
//        when(sharedPreferences.getBoolean(eq(DID_FAIL_SET_EMAIL_KEY), anyBoolean())).thenReturn(true);
//        when(sharedPreferences.getBoolean(eq(DID_FAIL_SET_USER_ID_KEY), anyBoolean())).thenReturn(true);
//        applyHttpSuccessInvocation();
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJson = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(2000).times(3)).postJson(eq(realtimeGetawayUrl),
//                httpSentJson.capture());
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(0)
//                .getInt(EVENT_REQUEST_EID_KEY), setUserEventId);
//        Assert.assertEquals(httpSentJson.getAllValues()
//                .get(1)
//                .getInt(EVENT_REQUEST_EID_KEY), setEmailEventId);
//        Assert.assertEquals(httpSentJson.getAllValues()
//                        .get(2)
//                        .getInt(EVENT_REQUEST_EID_KEY),
//                generalOptimoveEventId);
//
//
//    }
//
//    @Test
//    public void eventsShouldBeSentByTheSameOrderTheyReported() throws JSONException {
//        Map<String, Object> firstParams = new HashMap<>(2);
//        firstParams.put("first_param", "first_param");
//        when(generalOptimoveEvent.getParameters()).thenReturn(firstParams);
//
//        OptimoveEvent optimoveEventSecond = mock(OptimoveEvent.class);
//        applyHttpSuccessInvocation();
//        Map<String, Object> secondParams = new HashMap<>(2);
//        secondParams.put("second_param", "second_param");
//        when(optimoveEventSecond.getName()).thenReturn(generalEventName);
//        when(optimoveEventSecond.getParameters()).thenReturn(secondParams);
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//        realtimeManager.reportEvent(optimoveEventSecond);
//
//
//        ArgumentCaptor<JSONObject> httpSentJson = ArgumentCaptor.forClass(JSONObject.class);
//
//        verify(httpClient, timeout(2000).times(2)).postJson(eq(realtimeGetawayUrl),
//                httpSentJson.capture());
//        JSONObject firstEventContext = httpSentJson.getAllValues()
//                .get(0)
//                .getJSONObject(EVENT_REQUEST_CONTEXT_KEY);
//        JSONObject secondEventContext = httpSentJson.getAllValues()
//                .get(1)
//                .getJSONObject(EVENT_REQUEST_CONTEXT_KEY);
//        Assert.assertTrue(firstEventContext.has("first_param"));
//        Assert.assertTrue(secondEventContext.has("second_param"));
//
//    }
//
//    //
////    //endregion
////
////
////    //region SetUser/SetEmail events
////
//    @Test
//    public void userIdFailedFlagShouldBeSetToFalseIfEventReportedSuccessfully() {
//        String originalVisitorId = "some_visitor_id1";
//        String newUserId = "some_user_id1";
//        applyHttpSuccessInvocation();
//        realtimeManager.reportEvent(new SetUserIdEvent(originalVisitorId, newUserId, "sdfsfsdf"));
//
//        InOrder inOrder = inOrder(editor);
//        inOrder.verify(editor, timeout(2000))
//                .remove(DID_FAIL_SET_USER_ID_KEY);
//        inOrder.verify(editor)
//                .apply();
//    }
//
//    @Test
//    public void emailFailedFlagShouldBeSetToFalseIfEventReportedSuccessfully() {
//        applyHttpSuccessInvocation();
//        realtimeManager.reportEvent(new SetEmailEvent("sdfsfsdf"));
//
//        InOrder inOrder = inOrder(editor);
//        inOrder.verify(editor, timeout(2000))
//                .remove(DID_FAIL_SET_EMAIL_KEY);
//        inOrder.verify(editor)
//                .apply();
//    }
//
//    @Test
//    public void setEmailShouldBeDecoratedIfRegularEventIsSentButSetEmailWasFailed() throws JSONException {
//        when(sharedPreferences.getBoolean(DID_FAIL_SET_EMAIL_KEY, false)).thenReturn(true);
//        Map<String, EventConfigs.ParameterConfig> parameterConfigs = new HashMap<>();
//        parameterConfigs.put(EVENT_PLATFORM_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_NATIVE_MOBILE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_DEVICE_TYPE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_OS_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        when(setEmailEventConfig.getParameterConfigs()).thenReturn(parameterConfigs);
//        applyHttpSuccessInvocation();
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(2000).times(2)).postJson(eq(realtimeGetawayUrl),
//                httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getAllValues()
//                .get(0);
//        JSONObject context = jsonObject.getJSONObject(EVENT_REQUEST_CONTEXT_KEY);
//        Assert.assertTrue(context.has(EVENT_PLATFORM_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_NATIVE_MOBILE_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_DEVICE_TYPE_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_OS_PARAM_KEY));
//    }
//
//    @Test
//    public void setUserIdShouldBeDecoratedIfRegularEventIsSentButRealtimeIsntSynced() throws JSONException {
//        when(sharedPreferences.getBoolean(DID_FAIL_SET_USER_ID_KEY, false)).thenReturn(true);
//        Map<String, EventConfigs.ParameterConfig> parameterConfigs = new HashMap<>();
//        parameterConfigs.put(EVENT_PLATFORM_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_NATIVE_MOBILE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_DEVICE_TYPE_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        parameterConfigs.put(EVENT_OS_PARAM_KEY, mock(EventConfigs.ParameterConfig.class));
//        when(setUserIdEventConfig.getParameterConfigs()).thenReturn(parameterConfigs);
//        applyHttpSuccessInvocation();
//        realtimeManager.reportEvent(generalOptimoveEvent);
//
//        ArgumentCaptor<JSONObject> httpSentJsonObject = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient, timeout(2000).times(2)).postJson(eq(realtimeGetawayUrl), httpSentJsonObject.capture());
//        JSONObject jsonObject = httpSentJsonObject.getAllValues()
//                .get(0);
//        JSONObject context = jsonObject.getJSONObject(EVENT_REQUEST_CONTEXT_KEY);
//        Assert.assertTrue(context.has(EVENT_PLATFORM_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_NATIVE_MOBILE_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_DEVICE_TYPE_PARAM_KEY));
//        Assert.assertTrue(context.has(EVENT_OS_PARAM_KEY));
//
//    }
//
//    @Test
//    public void secondRequestMustNotBeDispatchedIfFirstRespondDoesntArrive() throws InterruptedException {
//
//        doAnswer(invocation -> {
//            Response.Listener<JSONObject> successListener =
//                    (Response.Listener<JSONObject>) invocation.getArguments()[0];
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put(EVENT_RESPONSE_DATA_KEY, true);
//            jsonObject.put(EVENT_RESPONSE_SUCCESS_KEY, true);
//            new Thread(() -> {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//
//                }
//                successListener.onResponse(jsonObject);
//            }).start();
//            return builder;
//        }).when(builder)
//                .successListener(any());
//
//        realtimeManager.reportEvent(generalOptimoveEvent);
//        realtimeManager.reportEvent(generalOptimoveEvent);
//        Thread.sleep(1000);
//
//        verify(httpClient, times(1)).postJson(anyString(), any());
//    }
//
//    private void applyHttpSuccessInvocation() {
//        doAnswer(invocation -> {
//            new Thread(() -> {
//                try {
//                    Response.Listener<JSONObject> successListener =
//                            (Response.Listener<JSONObject>) invocation.getArguments()[0];
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put(EVENT_RESPONSE_DATA_KEY, true);
//                    jsonObject.put(EVENT_RESPONSE_SUCCESS_KEY, true);
//                    successListener.onResponse(jsonObject);
//                } catch (JSONException j) {
//
//                }
//            }).start();
//
//            return builder;
//        }).when(builder)
//                .successListener(any());
//    }
//}
