package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;

public final class OptipushManager extends OptipushHandler {

    private static String testModeTopicPrefix = "test_android_";
    @NonNull
    private OptimoveFirebaseInteractor firebaseInteractor;
    @NonNull
    private OptipushUserRegistrar optipushUserRegistrar;
    @NonNull
    private Context context;


    public OptipushManager(@NonNull OptimoveFirebaseInteractor optimoveFirebaseInteractor,
                           @NonNull OptipushUserRegistrar optipushUserRegistrar,
                           @NonNull Context context) {
        this.firebaseInteractor = optimoveFirebaseInteractor;
        this.optipushUserRegistrar = optipushUserRegistrar;
        this.context = context;
    }

    public void tokenWasChanged(){
        optipushUserRegistrar.userTokenChanged();
    }


    public void addRegisteredUserOnDevice(String initialVisitorId, String userId) {
        optipushUserRegistrar.userIdChanged(initialVisitorId, userId);
    }


    public void startTestMode(@Nullable SdkOperationListener operationListener) {
        String packageName = context.getApplicationContext()
                .getPackageName();
        executeAnOptipushTopicOperation(testModeTopicPrefix + packageName, true, operationListener);
    }

    public void stopTestMode(@Nullable SdkOperationListener operationListener) {
        String packageName = context.getApplicationContext()
                .getPackageName();
        executeAnOptipushTopicOperation(testModeTopicPrefix + packageName, false, operationListener);
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
    private void executeAnOptipushTopicOperation(String topic, boolean isRegister,
                                                 @Nullable SdkOperationListener operationListener) {
        if (isRegister) {
            firebaseInteractor.registerToTopic(topic, operationListener);
        } else {
            firebaseInteractor.unregisterFromTopic(topic, operationListener);
        }
    }
}

