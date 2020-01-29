package com.optimove.sdk.optimove_sdk.optipush.messaging;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushFcmTokenHandler;

public class OptipushMessagingService extends FirebaseMessagingService {

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    new OptipushMessagingHandler(this).onMessageReceived(remoteMessage);
  }

  @Override
  public void onNewToken(String newToken) {
    super.onNewToken(newToken);
    new OptipushFcmTokenHandler(this).onTokenRefresh();
  }
}
