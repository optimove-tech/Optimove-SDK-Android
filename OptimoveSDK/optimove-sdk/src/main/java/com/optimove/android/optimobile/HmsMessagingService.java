package com.optimove.android.optimobile;

import android.text.TextUtils;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public final class HmsMessagingService extends HmsMessageService {

    private static final String TAG = HmsMessagingService.class.getName();

    @Override
    public void onNewToken(String token) {
        if (TextUtils.isEmpty(token)) {
            return;
        }

        Optimobile.log(TAG, "Got a push token: " + token);
        Optimobile.pushTokenStore(this, PushTokenType.HCM, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        HmsMessageHandler.onMessageReceived(this, remoteMessage);
    }
}
