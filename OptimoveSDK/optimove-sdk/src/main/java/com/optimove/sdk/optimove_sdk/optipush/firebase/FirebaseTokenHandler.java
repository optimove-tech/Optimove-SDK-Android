package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.io.IOException;

public class FirebaseTokenHandler {

    private Context context;
    private static String APP_CONTROLLER_PROJECT_NAME = "optimove.sdk.app_controller";

    public interface TokenListener {
        void sendToken(String token);

        void tokenRefreshFailed(String reason);
    }

    public FirebaseTokenHandler(@NonNull Context context) {
        this.context = context;
    }

    public boolean setup(@NonNull OptipushConfigs.FirebaseConfigs firebaseConfigs) {
        FirebaseKeys firebaseKeys = new FirebaseKeys.Builder()
                .setApiKey(firebaseConfigs.getWebApiKey())
                .setApplicationId(firebaseConfigs.getAppId())
                .setDatabaseUrl(firebaseConfigs.getDbUrl())
                .setGcmSenderId(firebaseConfigs.getSenderId())
                .setProjectId(firebaseConfigs.getProjectId())
                .setStorageBucket(firebaseConfigs.getStorageBucket())
                .build();

        try {
            if (FirebaseApp.getApps(context).size() > 0) {
                FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), APP_CONTROLLER_PROJECT_NAME);
            } else {
                FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions());
            }
        } catch (Exception e) {
            OptiLoggerStreamsContainer.error("Failed to init Firebase project %s due to: %s", firebaseConfigs.getProjectId(), e.getMessage());
            return false;
        }
        return true;
    }

    public void getOptimoveFCMToken(@NonNull TokenListener tokenListener) {
        String gcmSenderId;
        FirebaseApp firebaseApp;
        try {
            firebaseApp = FirebaseApp.getApps(context).size() > 1 ?
                    FirebaseApp.getInstance(APP_CONTROLLER_PROJECT_NAME) : FirebaseApp.getInstance();
            gcmSenderId = firebaseApp.getOptions()
                    .getGcmSenderId();
        } catch (Throwable throwable) {
            tokenListener.tokenRefreshFailed(String.format("Failed to get the senderId due to: %s",
                    throwable.getMessage()));
            return;
        }
        if (gcmSenderId == null) {
            tokenListener.tokenRefreshFailed("Failed to get the senderId");
            return;
        }
        new Thread(() -> {
            try {
                String token = FirebaseInstanceId.getInstance(firebaseApp)
                        .getToken(gcmSenderId, "FCM");
                new Handler(Looper.getMainLooper()).post(() -> tokenListener.sendToken(token));
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> tokenListener.tokenRefreshFailed(String.format("Was not able to get the token due to: %s", e.getMessage())));

            }
        }).start();
    }

}

