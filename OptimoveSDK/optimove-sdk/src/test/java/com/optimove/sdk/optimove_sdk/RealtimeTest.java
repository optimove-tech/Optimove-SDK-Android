package com.optimove.sdk.optimove_sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetEmailEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;

import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_RESPONSE_DATA_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.EVENT_RESPONSE_SUCCESS_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.FAILED_SET_EMAIL_EVENT_KEY;
import static com.optimove.sdk.optimove_sdk.realtime.RealtimeConstants.FAILED_SET_USER_EVENT_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RealtimeTest {


    @Mock
    Context context;
    @Mock
    UserInfo userInfo;
    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    HttpClient httpClient;
    @Mock
    HttpClient.RequestBuilder<JSONObject> builder;
    @Mock
    RealtimeConfigs realtimeConfigs;

    private RealtimeManager realtimeManager;
    private String realtimeGetawayUrl = "some_url";
    private String realtimeToken = "some_token";

    private String userId = "some_user_id_from_info";
    private String userEmail = "some_user_email_frm_info";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);

        when(realtimeConfigs.getRealtimeGateway()).thenReturn(realtimeGetawayUrl);
        when(realtimeConfigs.getRealtimeToken()).thenReturn(realtimeToken);

        when(httpClient.postJsonArray(anyString(), any())).thenReturn(builder);
        when(builder.errorListener(any())).thenReturn(builder);
        when(builder.destination(any(), any())).thenReturn(builder);
        when(builder.successListener(any())).thenReturn(builder);

        when(userInfo.getUserId()).thenReturn(userId);
        when(userInfo.getEmail()).thenReturn(userEmail);
        realtimeManager = new RealtimeManager(httpClient, realtimeConfigs, context);
    }


    @Test
    public void singleEventShouldBeDispatched() throws JSONException {
        when(builder.successListener(any())).thenReturn(builder);

        realtimeManager.reportEvents(Collections.singletonList(getRegularEvent()));

        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(eq(realtimeGetawayUrl), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), "some_name");
    }


    @Test
    public void setUserIdShouldBeAttachedBeforeTheReportedEventIfPreviouslyFailed() throws JSONException {
        String optistreamSetUserIdEventAsStoredInSharedPrefs = new Gson().toJson(getSetUserIdEvent());
        when(sharedPreferences.getString(eq(FAILED_SET_USER_EVENT_KEY), any())).thenReturn(optistreamSetUserIdEventAsStoredInSharedPrefs);

        realtimeManager.reportEvents(Collections.singletonList(getRegularEvent()));

        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(eq(realtimeGetawayUrl), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), SetUserIdEvent.EVENT_NAME);
        Assert.assertEquals(jsonArray.getJSONObject(1)
                .getString("event"), "some_name");

    }
    @Test
    public void setEmailShouldBeAttachedBeforeTheReportedEventIfPreviouslyFailed() throws JSONException {
        String optistreamSetEmailEventAsStoredInSharedPrefs = new Gson().toJson(getSetEmailEvent());
        when(sharedPreferences.getString(eq(FAILED_SET_EMAIL_EVENT_KEY), any())).thenReturn(optistreamSetEmailEventAsStoredInSharedPrefs);

        realtimeManager.reportEvents(Collections.singletonList(getRegularEvent()));

        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(eq(realtimeGetawayUrl), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), SetEmailEvent.EVENT_NAME);
        Assert.assertEquals(jsonArray.getJSONObject(1)
                .getString("event"), "some_name");

    }

    @Test
    public void setEmailShouldntBeAttachedBeforeTheReportedSetEmailEventIfPreviouslyFailed() throws JSONException {
        String optistreamSetEmailEventAsStoredInSharedPrefs = new Gson().toJson(getSetEmailEvent());
        when(sharedPreferences.getString(eq(FAILED_SET_EMAIL_EVENT_KEY), any())).thenReturn(optistreamSetEmailEventAsStoredInSharedPrefs);

        realtimeManager.reportEvents(Collections.singletonList(getSetEmailEvent()));

        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(eq(realtimeGetawayUrl), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), SetEmailEvent.EVENT_NAME);
        Assert.assertEquals(jsonArray.length(), 1);

    }
    @Test
    public void setUserShouldntBeAttachedBeforeTheReportedSetUserEventIfPreviouslyFailed() throws JSONException {
        String optistreamSetUserEventAsStoredInSharedPrefs = new Gson().toJson(getSetUserIdEvent());
        when(sharedPreferences.getString(eq(FAILED_SET_USER_EVENT_KEY), any())).thenReturn(optistreamSetUserEventAsStoredInSharedPrefs);

        realtimeManager.reportEvents(Collections.singletonList(getSetUserIdEvent()));

        ArgumentCaptor<JSONArray> httpSentJsonArray = ArgumentCaptor.forClass(JSONArray.class);

        verify(httpClient, timeout(1000)).postJsonArray(eq(realtimeGetawayUrl), httpSentJsonArray.capture());
        JSONArray jsonArray = httpSentJsonArray.getValue();
        Assert.assertEquals(jsonArray.getJSONObject(0)
                .getString("event"), SetUserIdEvent.EVENT_NAME);
        Assert.assertEquals(jsonArray.length(), 1);
    }

    @Test
    public void failedEventsShouldBeRemovedIfDispatchedSuccessfully() {
        applyHttpSuccessInvocation();
        realtimeManager.reportEvents(Collections.singletonList(getRegularEvent()));

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor, timeout(2000))
                .remove(FAILED_SET_USER_EVENT_KEY);
        inOrder.verify(editor, timeout(2000))
                .remove(FAILED_SET_EMAIL_EVENT_KEY);
        inOrder.verify(editor)
                .apply();
    }

    @Test
    public void failedSetUserIdShouldEndUpInSharedPrefs() {
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return builder;
        }).when(builder)
                .errorListener(any());
        OptistreamEvent optistreamEvent = getSetUserIdEvent();
        realtimeManager.reportEvents(Collections.singletonList(optistreamEvent));

        String serializedSetUserEvent = new Gson().toJson(optistreamEvent);

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor, timeout(500))
                .putString(FAILED_SET_USER_EVENT_KEY, serializedSetUserEvent);
        inOrder.verify(editor, timeout(500))
                .apply();
    }
    @Test
    public void failedSetEmailShouldEndUpInSharedPrefs() {
        doAnswer(invocation -> {
            Response.ErrorListener errorListener =
                    (Response.ErrorListener) invocation.getArguments()[0];
            errorListener.onErrorResponse(mock(ParseError.class));
            return builder;
        }).when(builder)
                .errorListener(any());
        OptistreamEvent optistreamEvent = getSetEmailEvent();
        realtimeManager.reportEvents(Collections.singletonList(optistreamEvent));

        String serializedSetEmailEvent = new Gson().toJson(optistreamEvent);

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor, timeout(500))
                .putString(FAILED_SET_EMAIL_EVENT_KEY, serializedSetEmailEvent);
        inOrder.verify(editor, timeout(500))
                .apply();
    }
    private OptistreamEvent getRegularEvent() {
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName("some_name")
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(mock(OptistreamEvent.Metadata.class))
                .build();
    }

    private OptistreamEvent getSetUserIdEvent() {
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName(SetUserIdEvent.EVENT_NAME)
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(mock(OptistreamEvent.Metadata.class))
                .build();
    }
    private OptistreamEvent getSetEmailEvent() {
        return OptistreamEvent.builder()
                .withTenantId(33333)
                .withCategory("some_category")
                .withName(SetEmailEvent.EVENT_NAME)
                .withOrigin("some_origin")
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp("timestamp")
                .withContext(mock(Map.class))
                .withMetadata(mock(OptistreamEvent.Metadata.class))
                .build();
    }
    private void applyHttpSuccessInvocation() {
        doAnswer(invocation -> {
            new Thread(() -> {
                try {
                    Response.Listener<JSONObject> successListener =
                            (Response.Listener<JSONObject>) invocation.getArguments()[0];
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(EVENT_RESPONSE_DATA_KEY, true);
                    jsonObject.put(EVENT_RESPONSE_SUCCESS_KEY, true);
                    successListener.onResponse(jsonObject);
                } catch (JSONException j) {

                }
            }).start();

            return builder;
        }).when(builder)
                .successListener(any());
    }
}
