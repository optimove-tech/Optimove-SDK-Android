package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MbaasTopicsRegistrar extends TopicsRegistrar implements Response.Listener<JSONObject>, Response.ErrorListener {

  private String mbaasTopicsEndpoint;

  private boolean isRegister;
  private List<String> topics;
  private HttpClient httpClient;

  public static String REGISTER_TOPIC_URL_COMPONENT = "registerClientToTopics";
  public static String UNREGISTER_TOPIC_URL_COMPONENT = "unregisterClientFromTopics";

  public MbaasTopicsRegistrar(Context context, String mbaasTopicsEndpoint, HttpClient httpClient,
                               @Nullable SdkOperationListener operationListener) {
    super(context, operationListener);
    this.mbaasTopicsEndpoint = mbaasTopicsEndpoint;
    this.topics = new ArrayList<>();
    this.httpClient = httpClient;
  }

  @Override
  public void registerToTopics(String... topics) {
    isRegister = true;
    this.topics = new ArrayList<>(Arrays.asList(topics));
    sendToEndpoint();
  }

  @Override
  public void unregisterFromTopics(String... topics) {
    isRegister = false;
    this.topics = new ArrayList<>(Arrays.asList(topics));
    sendToEndpoint();
  }


  private void sendToEndpoint() {
    removeRedundantTopics();

    if (topics.isEmpty()) {
      OptiLoggerStreamsContainer.info("Skipped MBAAS topic operation (isRegister=%b) for topics %s as all topics are already not registered",
          isRegister, Arrays.deepToString(topics.toArray(new String[0])));
      notifyOperationListener(true);
      return;
    }

    OptiLoggerStreamsContainer.debug("Starting MBAAS topic operation (isRegister=%b) for topics: %s",
        isRegister, Arrays.deepToString(topics.toArray(new String[0])));
    JSONObject body = createTopicsRequestJsonBody(topics);
    if (body == null) {
      notifyOperationListener(false);
      return;
    }
    String apiName = isRegister ? REGISTER_TOPIC_URL_COMPONENT : UNREGISTER_TOPIC_URL_COMPONENT;
    httpClient
        .postJson(mbaasTopicsEndpoint, body)
        .destination("%s", apiName)
        .successListener(this)
        .errorListener(this)
        .send();
  }

  private void removeRedundantTopics() {
    for (String pendingTopic : new ArrayList<>(topics)) {
      boolean validForOperation = isTopicValidForOperation(pendingTopic);
      if (!validForOperation)
        topics.remove(pendingTopic);
    }
  }

  @SuppressWarnings("RedundantIfStatement")
  private boolean isTopicValidForOperation(String topic) {
    if (isRegister && topicsPreferences.contains(topic))
      return false; //Topic already exists, don't re-register
    if (!isRegister && !topicsPreferences.contains(topic))
      return false; //Topic doesn't exist, don't unregister
    return true;
  }

  @Nullable
  private JSONObject createTopicsRequestJsonBody(List<String> topics) {
    JSONObject result = new JSONObject();
    String token = new RegistrationDao(context).getLastToken();
    if (token == null) {
      OptiLogger.optipushFailedToCreateTopicsRegistrationRequest("last token was null");
      return null;
    }
    try {
      result.put("fcmToken", token);
      result.put("topics", new JSONArray(topics));
    } catch (JSONException e) {
      OptiLogger.optipushFailedToCreateTopicsRegistrationRequest(e.getMessage());
      return null;
    }
    return result;
  }

  @Override
  public void onResponse(JSONObject response) {
    boolean success;
    try {
      success = response.getBoolean("success");
    } catch (JSONException e) {
      OptiLogger.optipushTopicsRegistrationViaMbaasFailed(topics.toArray(new String[0]), e.getMessage());
      success = false;
    }

    notifyOperationListener(success);
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    if (error != null && error.getMessage() != null) {
      OptiLogger.optipushTopicsRegistrationViaMbaasFailed(topics.toArray(new String[0]), error.getMessage());
    } else {
      OptiLogger.optipushTopicsRegistrationViaMbaasFailed(topics.toArray(new String[0]), "Unknown error");
    }
    notifyOperationListener(false);
  }
}
