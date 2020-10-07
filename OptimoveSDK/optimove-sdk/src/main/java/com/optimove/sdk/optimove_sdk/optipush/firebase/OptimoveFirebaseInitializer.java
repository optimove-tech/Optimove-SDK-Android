package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Firebase.APP_CONTROLLER_PROJECT_NAME;


public class OptimoveFirebaseInitializer {

  private Context context;
  private boolean clientHasDefaultFirebaseApp;

  public OptimoveFirebaseInitializer(@NonNull Context context) {
    this.context = context;
    this.clientHasDefaultFirebaseApp = false;
  }

  public boolean setup(OptipushConfigs optipushConfigs) {
    this.clientHasDefaultFirebaseApp = FirebaseApp.getApps(context).size() > 0;
    return initAppControllerProject(optipushConfigs.getAppControllerProjectConfigs());
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
        FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), APP_CONTROLLER_PROJECT_NAME);
      } else {
        FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions());
      }
    } catch (Exception e) {
      OptiLoggerStreamsContainer.error("Failed to init Firebase project %s due to: %s", APP_CONTROLLER_PROJECT_NAME, e.getMessage());
      return false;
    }
    return true;
  }

}

