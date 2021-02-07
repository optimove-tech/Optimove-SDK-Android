package com.optimove.sdk.demo.optipush;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessagingHandler;

// If you're using Firebase Messaging or another Push provider, you'd have this service catching all the Firebase Messaging SDK Callbacks, so you need to forward them to the Optimove SDK
public class MyMessagingService extends FirebaseMessagingService {

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    boolean wasOptipushMessage = new OptipushMessagingHandler(this).onMessageReceived(remoteMessage);
    if (wasOptipushMessage) {
      // You should not attempt to present a notification at this point.
      return;
    }
    // The notification was meant for the App, perform your push logic here
  }

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);
    // Forward the call to the Optimove SDK
    Optimove.getInstance().fcmTokenRefreshed();
    // Continue with the application logic
  }
}
