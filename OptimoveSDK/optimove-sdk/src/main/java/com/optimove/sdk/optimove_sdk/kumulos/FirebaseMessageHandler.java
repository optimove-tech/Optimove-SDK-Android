package com.optimove.sdk.optimove_sdk.kumulos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * FirebaseMessageHandler provides helpers for handling FirebaseMessagingService events
 *
 * This can allow interoperating Optimobile push with your own FCM service
 */
public class FirebaseMessageHandler {

    private static final String TAG = FirebaseMessagingService.class.getName();

    /**
     * Handles the received notification from FCM, creating a PushMessage model and broadcasting
     * the appropriate com.optimobile.push Intent
     *
     * @param context
     * @param remoteMessage
     */
    public static void onMessageReceived(@NonNull Context context, @Nullable RemoteMessage remoteMessage) {
        if (null == remoteMessage) {
            return;
        }

        Optimobile.log(TAG, "Received a push message");

        Map<String, String> bundle = remoteMessage.getData();

        String customStr = bundle.get("custom");

        if (null == customStr) {
            return;
        }

        // Extract bundle
        int id;
        JSONObject data;
        JSONObject custom;
        Uri uri;
        String pictureUrl = bundle.get("bicon");
        JSONArray buttons;
        String sound = bundle.get("sound");

        try {
            custom = new JSONObject(customStr);
            uri = (!custom.isNull("u")) ? Uri.parse(custom.getString("u")) : null;
            data = custom.getJSONObject("a");
            id = data.getJSONObject("k.message").getJSONObject("data").getInt("id");
            buttons = data.optJSONArray("k.buttons");

        } catch (JSONException e) {
            Optimobile.log(TAG, "Push received had no ID/data/uri or was incorrectly formatted, ignoring...");
            return;
        }

        String bgn = bundle.get("bgn");
        boolean runBackgroundHandler = (null != bgn && bgn.equals("1"));

        PushMessage pushMessage = new PushMessage(
                id,
                bundle.get("title"),
                bundle.get("alert"),
                data,
                remoteMessage.getSentTime(),
                uri,
                runBackgroundHandler,
                pictureUrl,
                buttons,
                sound,
                remoteMessage.getCollapseKey()
        );

        Intent intent = new Intent(PushBroadcastReceiver.ACTION_PUSH_RECEIVED);
        intent.setPackage(context.getPackageName());
        intent.putExtra(PushMessage.EXTRAS_KEY, pushMessage);

        context.sendBroadcast(intent);
    }
}
