package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Firebase.APP_CONTROLLER_PROJECT_NAME;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Firebase.SDK_CONTROLLER_PROJECT_NAME;


public class OptimoveFirebaseInteractor {

  public static final int NUMBER_OF_OPTIMOVE_FIREBASE_APPS = 2;

  private Context context;
  @Nullable
  private FirebaseApp sdkController;
  @Nullable
  private FirebaseApp appController;
  private boolean clientHasDefaultFirebaseApp;

  private String mbaasTopicsEndpoint;

  public OptimoveFirebaseInteractor(OptipushConfigs optipushConfigs) {
    context = Optimove.getInstance().getApplicationContext();
    this.mbaasTopicsEndpoint = optipushConfigs.getPushTopicsRegistrationEndpoint();
    this.clientHasDefaultFirebaseApp = false;
  }

  public boolean setup(OptipushConfigs optipushConfigs) {
    this.clientHasDefaultFirebaseApp = FirebaseApp.getApps(context).size() > 0;
    OptipushConfigs.FirebaseConfigs appControllerProjectMetadata = optipushConfigs.getAppControllerProjectConfigs();
    boolean didInitAppProject = initAppControllerProject(appControllerProjectMetadata);
    if (!didInitAppProject) {
      return false;
    }
    // Don't call the clientServiceProjectMetadata without having the AppController project initialized
    OptipushConfigs.FirebaseConfigs clientServiceProjectMetadata = optipushConfigs.getClientServiceProjectConfigs();
    return initClientsServiceControllerProject(clientServiceProjectMetadata);
  }

  /**
   * Performs {@code topic registration} at <i>FCM</i> for the provided topic.<br>
   * The topic is also validated against following pattern (defined by Firebase): ({@code [a-zA-Z0-9-_.~%]{1,900}})<br>
   * <p>
   * <b>Discussion</b>: <br>
   * The Firebase SDK (v16.0) allows topic registration only for the token of the "default" app.<br>
   * If the client already has a default {@code FirebaseApp} than the MBAAS won't be able to target that topic due to security restrictions of Firebase.<br>
   * That's why, for those client, the SDK sends the registration request to a dedicated endpoint and not the Firebase SDK.
   *
   * @param topic the topic to register to
   */
  public void registerToTopic(String topic, @Nullable SdkOperationListener operationListener) {
    if (appController == null) {
      OptiLogger.optipushSingleTopicRegistrationFailed_WhenAppControllerIsNull();
      if (operationListener != null)
        operationListener.onResult(false);
      return;
    }
    getTopicsRegistrar(operationListener).registerToTopics(topic);
  }

  /**
   * Performs {@code topic un-registration} at <i>FCM</i> for the provided topic.<br>
   * The topic is also validated against following pattern (defined by Firebase): ({@code [a-zA-Z0-9-_.~%]{1,900}})<br>
   * <p>
   * <b>Discussion</b>: <br>
   * The Firebase SDK (v16.0) allows topic registration only for the token of the "default" app.<br>
   * If the client already has a default {@code FirebaseApp} than the MBAAS won't be able to target that topic due to security restrictions of Firebase.<br>
   * That's why, for those client, the SDK sends the registration request to a dedicated endpoint and not the Firebase SDK.
   *
   * @param topic the topic to un-register from
   */
  public void unregisterFromTopic(String topic, @Nullable SdkOperationListener operationListener) {
    if (appController == null) {
      OptiLogger.optipushSingleTopicUnregistrationFailed_WhenAppControllerIsNull();
      if (operationListener != null)
        operationListener.onResult(false);
      return;
    }
    getTopicsRegistrar(operationListener).unregisterFromTopics(topic);
  }

  private boolean initAppControllerProject(@NonNull OptipushConfigs.FirebaseConfigs appControllerProjectConfigs) {
    FirebaseKeys firebaseKeys = new FirebaseKeys.Builder()
        .setApiKey(appControllerProjectConfigs.getWebApiKey())
        .setApplicationId(appControllerProjectConfigs.getAppId())
        .setDatabaseUrl(appControllerProjectConfigs.getDbUrl())
        .setGcmSenderId(appControllerProjectConfigs.getSenderId())
        .setProjectId(appControllerProjectConfigs.getProjectId())
        .setStorageBucket(appControllerProjectConfigs.getStorageBucket())
        .build();

    try {
      if (this.clientHasDefaultFirebaseApp) {
        if (FirebaseApp.getApps(context).size() == 0) {
          throw new IllegalStateException("Optimove can't connect to Firebase without a default app");
        }
        appController = FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), APP_CONTROLLER_PROJECT_NAME);
      } else {
        appController = FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions());
      }
    } catch (Exception e) {
      OptiLogger.optipushFirebaseProjectInitFailed(APP_CONTROLLER_PROJECT_NAME, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean initClientsServiceControllerProject(@NonNull OptipushConfigs.FirebaseConfigs clientsServiceControllerProjectMetadata) {
    FirebaseKeys firebaseKeys = new FirebaseKeys.Builder()
        .setApiKey(clientsServiceControllerProjectMetadata.getWebApiKey())
        .setApplicationId(clientsServiceControllerProjectMetadata.getAppId())
        .setDatabaseUrl(clientsServiceControllerProjectMetadata.getDbUrl())
        .setGcmSenderId(clientsServiceControllerProjectMetadata.getSenderId())
        .setProjectId(clientsServiceControllerProjectMetadata.getProjectId())
        .setStorageBucket(clientsServiceControllerProjectMetadata.getStorageBucket())
        .build();
    try {
      sdkController = FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), SDK_CONTROLLER_PROJECT_NAME);
    } catch (Exception e) {
      OptiLogger.optipushFirebaseProjectInitFailed(SDK_CONTROLLER_PROJECT_NAME, e.getMessage());
      return false;
    }
    return true;
  }

  private TopicsRegistrar getTopicsRegistrar(@Nullable SdkOperationListener operationListener) {
    if (this.clientHasDefaultFirebaseApp) {
      return new MbaasTopicsRegistrar(context, mbaasTopicsEndpoint, HttpClient.getInstance(context), operationListener);
    } else {
      return new FirebaseTopicsRegistrar(context, operationListener);
    }
  }
}

