package com.optimove.sdk.optimove_sdk.optipush;

import com.google.firebase.messaging.RemoteMessage;

public abstract class OptipushHandler {
    protected OptipushHandler next;

    public void setNext(OptipushHandler next) {
        this.next = next;
    }

    public abstract void userIdChanged();

    public abstract void tokenWasChanged();

    //messaging
    public abstract void optipushMessageCommand(RemoteMessage remoteMessage, int executionTimeLimitInMs);

}
