package com.optimove.sdk.optimove_sdk.optipush;

import android.support.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;

public abstract class OptipushHandler {
    protected OptipushHandler next;

    public void setNext(OptipushHandler next) {
        this.next = next;
    }

    public abstract void addRegisteredUserOnDevice(String visitorId, String userId);

    public abstract void startTestMode(@Nullable SdkOperationListener operationListener);

    public abstract void stopTestMode(@Nullable SdkOperationListener operationListener);

    //    public abstract void tokenWasChanged(String lastToken, String newToken);
    public abstract void tokenWasChanged();

   // public abstract void registerUserForPush(String token);

    //messaging
    public abstract void optipushMessageCommand(RemoteMessage remoteMessage, int executionTimeLimitInMs);

}
