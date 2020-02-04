package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;

public final class OptipushManager extends OptipushHandler {

//    @NonNull
//    private OptimoveFirebaseInteractor firebaseInteractor;
    @NonNull
    private OptipushUserRegistrar optipushUserRegistrar;
    @NonNull
    private Context context;


    public OptipushManager(@NonNull OptipushUserRegistrar optipushUserRegistrar,
                           @NonNull Context context) {
        this.optipushUserRegistrar = optipushUserRegistrar;
        this.context = context;
    }

    public void tokenWasChanged(){
        optipushUserRegistrar.userTokenChanged();
    }


    public void addRegisteredUserOnDevice(String initialVisitorId, String userId) {
        optipushUserRegistrar.userIdChanged(initialVisitorId, userId);
    }


    @Override
    public void optipushMessageCommand(RemoteMessage remoteMessage, int executionTimeLimitInMs) {
        NotificationCreator notificationCreator = new NotificationCreator(context);
        new OptipushMessageCommand(context, Optimove.getInstance()
                .getEventHandlerProvider()
                .getEventHandler(),
                new RequirementProvider(context), notificationCreator)
                .processRemoteMessage(executionTimeLimitInMs, remoteMessage, JsonUtils.parseJsonMap(remoteMessage.getData(),
                        NotificationData.class));

    }
}

