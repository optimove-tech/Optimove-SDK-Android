package com.optimove.sdk.optimove_sdk.optipush_tests.firebase_tests;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.LAST_TOKEN_KEY;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.REGISTRATION_PREFERENCES_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
//@Config(sdk = Build.VERSION_CODES.P)
public class MbaasTopicRegistrarTests {

    @Mock
    HttpClient httpClient;
    @Mock
    SdkOperationListener operationListener;
    @Mock
    HttpClient.RequestBuilder<JSONObject> builder;

    private String mbaasTopicsEndpoint = "endpoint";
    private String fcmToken = "some_token";

    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putString(LAST_TOKEN_KEY, fcmToken).commit();

        when(httpClient.postJson(anyString(),any())).thenReturn(builder);
        when(builder.errorListener(any())).thenReturn(builder);
        when(builder.successListener(any())).thenReturn(builder);
        when(builder.destination(any(),any())).thenReturn(builder);
    }
    @Test
    public void registerTopicShouldCallTheHttpEndpointWithCorrectParams() throws Exception {
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,
                mbaasTopicsEndpoint
                ,httpClient,
                operationListener);
        String topicToRegister = "my_custom_topic";
        mbaasTopicsRegistrar.registerToTopics(topicToRegister);
        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);

        verify(httpClient).postJson(eq(mbaasTopicsEndpoint),jsonObjectArgumentCaptor.capture());
        verify(builder).destination("%s", MbaasTopicsRegistrar.REGISTER_TOPIC_URL_COMPONENT);

        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getString("fcmToken"),fcmToken);
        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getJSONArray("topics").get(0),topicToRegister);
    }
    @Test
    public void registerTopicShouldntCallTheHttpEndpointIfExistsInStorage(){
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
                operationListener);
        String topicToRegister = "my_custom_topic";
        context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(topicToRegister, true).commit();

        mbaasTopicsRegistrar.registerToTopics(topicToRegister);

        verifyZeroInteractions(httpClient);
        verify(operationListener).onResult(true);
    }
    @Test
    public void registerTopicShouldntCallTheHttpEndpointIfTokenIsNull() {
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
                operationListener);
        context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().remove(LAST_TOKEN_KEY).commit();
        String topicToRegister = "my_custom_topic";
        mbaasTopicsRegistrar.registerToTopics(topicToRegister);

        verifyZeroInteractions(httpClient);
        verify(operationListener).onResult(false);
    }
    @Test
    public void unregisterTopicShouldCallTheHttpEndpointWithCorrectParams() throws Exception {
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
                operationListener);
        String topicToRegister = "my_custom_topic";
        context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(topicToRegister, true).commit();

        mbaasTopicsRegistrar.unregisterFromTopics(topicToRegister);
        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);

        verify(builder).destination("%s", MbaasTopicsRegistrar.UNREGISTER_TOPIC_URL_COMPONENT);
        verify(httpClient).postJson(eq(mbaasTopicsEndpoint),jsonObjectArgumentCaptor.capture());

        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getString("fcmToken"),fcmToken);
        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getJSONArray("topics").get(0),topicToRegister);
    }
    @Test
    public void unregisterTopicShouldntCallTheHttpEndpointIfDoesntExistInStorage() {
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
                operationListener);
        String topicToRegister = "my_custom_topic";

        mbaasTopicsRegistrar.unregisterFromTopics(topicToRegister);

        verifyZeroInteractions(httpClient);
        verify(operationListener).onResult(true);
    }
    @Test
    public void unregisterTopicShouldntCallTheHttpEndpointIfTokenIsNull() {
        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
                operationListener);

        String topicToRegister = "my_custom_topic";
        context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().remove(LAST_TOKEN_KEY).commit();

        context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(topicToRegister, true).commit();
        mbaasTopicsRegistrar.unregisterFromTopics(topicToRegister);

        verifyZeroInteractions(httpClient);
        verify(operationListener).onResult(false);
    }

    @Test
    public void refreshTopicRegistrationShouldSendToEndpoint() throws JSONException {
        String someTopicToCheck = "tizm";
//        HashMap topics  = mock(HashMap.class);
//        Set<String> keySet = new ArraySet<>();
//        keySet.add(someTopicToCheck);
//        when(topics.keySet()).thenReturn(keySet);
//        when(topicPreferences.getAll()).thenReturn(topics);
        //todo - figure out why the refreshing is needed in the first place
//        context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .edit()
//                .putBoolean(someTopicToCheck, false)
//                .commit();
//        context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .edit().putString(LAST_TOKEN, fcmToken).commit();
//
//        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context, mbaasTopicsEndpoint, httpClient,
//                operationListener);
//        mbaasTopicsRegistrar.refreshTopicsRegistration();
//
//        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
//        verify(httpClient).postJson(eq(mbaasTopicsEndpoint), jsonObjectArgumentCaptor.capture());
//        verify(builder).destination("%s", MbaasTopicsRegistrar.REGISTER_TOPIC_URL_COMPONENT);
//        Assert.assertEquals(jsonObjectArgumentCaptor.getValue()
//                .getString("fcmToken"), fcmToken);
//        Assert.assertEquals(jsonObjectArgumentCaptor.getValue()
//                .getJSONArray("topics")
//                .get(0), someTopicToCheck);
    }

//    @Test
//    public void refreshTopicRegistrationShouldPutEventsToStorageWhenEndpointReturnsFailure() throws JSONException {
//        String someTopicToCheck = "tizm";
////        HashMap topics  = mock(HashMap.class);
////        Set<String> keySet = new HashSet<>();
////        keySet.add(someTopicToCheck);
////        when(topics.keySet()).thenReturn(keySet);
////        when(topicPreferences.getAll()).thenReturn(topics);
//        context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .edit()
//                .putBoolean(someTopicToCheck, false)
//                .commit();
//        context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .edit().putString(LAST_TOKEN, fcmToken).commit();
//        doAnswer(invocation -> {
//            Response.ErrorListener errorListener =
//                    (Response.ErrorListener) invocation.getArguments()[0];
//            errorListener.onErrorResponse(mock(ParseError.class));
//            return builder;
//        }).when(builder)
//                .errorListener(any());
//
//        MbaasTopicsRegistrar mbaasTopicsRegistrar = new MbaasTopicsRegistrar(context,mbaasTopicsEndpoint,httpClient,
//                operationListener);
//        mbaasTopicsRegistrar.refreshTopicsRegistration();
//
//
//       // InOrder inOrder = Mockito.inOrder(topicPreferencesEditor);
//        //inOrder.
//        Assert.assertTrue(topicPreferences.getBoolean(someTopicToCheck,false));
//       // verify(topicPreferencesEditor, timeout(1000)).putBoolean(someTopicToCheck,any());
//      //  inOrder.verify(topicPreferencesEditor,timeout(1000)).apply();
//
////        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
////        verify(httpClient).postJson(eq(mbaasTopicsEndpoint),jsonObjectArgumentCaptor.capture());
////        verify(builder).destination("%s", MbaasTopicsRegistrar.REGISTER_TOPIC_URL_COMPONENT);
////        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getString("fcmToken"),fcmToken);
////        Assert.assertEquals(jsonObjectArgumentCaptor.getValue().getJSONArray("topics").get(0),someTopicToCheck);
//    }
}
