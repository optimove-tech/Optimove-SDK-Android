package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;

final class FirebaseTopicSubscriber implements TopicSubscriber {

    private static final String TAG = FirebaseTopicSubscriber.class.getName();

    @Override
    public void subscribe(@NonNull String topic) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnFailureListener(e ->
                    Optimobile.log(TAG, "Live Updates: subscribing to " + topic + " failed: " + e));
        } catch (RuntimeException | LinkageError e) {
            Optimobile.log(TAG, "Live Updates: cannot subscribe to " + topic + ": " + e);
        }
    }

    @Override
    public void unsubscribe(@NonNull String topic) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnFailureListener(e ->
                    Optimobile.log(TAG, "Live Updates: unsubscribing from " + topic + " failed: " + e));
        } catch (RuntimeException | LinkageError e) {
            Optimobile.log(TAG, "Live Updates: cannot unsubscribe from " + topic + ": " + e);
        }
    }
}
