package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

public class FirebaseTopicsRegistrar extends TopicsRegistrar {

  protected FirebaseTopicsRegistrar(Context context, @Nullable SdkOperationListener operationListener) {
    super(context, operationListener);
  }

  @Override
  public void registerToTopics(String... topics) {
    for (String topic : topics) {
      FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
    OptiLogger.optipushFinishedTopicsRegistrationViaFirebaseSuccessfully(topics);
    notifyOperationListener(true);
  }

  @Override
  public void unregisterFromTopics(String... topics) {
    for (String topic : topics) {
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }
    OptiLogger.optipushFinishedTopicsUnregistrationViaFirebaseSuccessfully(topics);
    notifyOperationListener(true);
  }

}
