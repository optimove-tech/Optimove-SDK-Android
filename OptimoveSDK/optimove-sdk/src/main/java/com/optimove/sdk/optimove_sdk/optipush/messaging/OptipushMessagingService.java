package com.optimove.sdk.optimove_sdk.optipush.messaging;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;

public class OptipushMessagingService extends FirebaseMessagingService {

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    new OptipushMessagingHandler(this).onMessageReceived(remoteMessage);
  }

  @Override
  public void onNewToken(@NonNull String newToken) {
    super.onNewToken(newToken);
    Optimove.getInstance().fcmTokenRefreshed();
  }
}
