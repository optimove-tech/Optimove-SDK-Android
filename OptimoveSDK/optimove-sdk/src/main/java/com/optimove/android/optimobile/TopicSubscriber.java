package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

interface TopicSubscriber {

    void subscribe(@NonNull String topic);

    void unsubscribe(@NonNull String topic);
}
