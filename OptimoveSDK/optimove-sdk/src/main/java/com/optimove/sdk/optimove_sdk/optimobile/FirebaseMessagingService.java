package com.optimove.sdk.optimove_sdk.optimobile;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = FirebaseMessagingService.class.getName();

    @Override
    public void onNewToken(@NonNull String token) {
        if (TextUtils.isEmpty(token)) {
            return;
        }

        Optimobile.log(TAG, "Got a push token: " + token);
        Optimobile.pushTokenStore(this, PushTokenType.FCM, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        FirebaseMessageHandler.onMessageReceived(this, remoteMessage);
    }
}
