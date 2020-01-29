package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * When passed a {@code RemoteMessage}, this object validates it is indeed an {@code Optipush} Message
 * and extracts the campaign's data if it is.
 */
public class OptipushMessagingHandler {

    private Context context;
    private static int executionTimeInMilliseconds = (int) TimeUnit.SECONDS.toMillis(5);

    public OptipushMessagingHandler(Context context) {
        this.context = context;
    }

    /**
     * Call with a {@code RemoteMessage} to try to start a new {@code Optipush} message flow.<br>
     * If the message was not intended for the <i>Optimove SDK</i> this method returns {@code false}.
     *
     * @param remoteMessage the new <i>Push Message</i> to validate and execute.
     * @return {@code true} if the message was handled by the <i>Optimove SDK</i>, {@code false} otherwise.
     */
    public boolean onMessageReceived(RemoteMessage remoteMessage) {
        OptiLogger.optipushReceivedNewPushMessage(new JSONObject(remoteMessage.getData()).toString());

        Optimove.configureUrgently(context);

        if (remoteMessage.getData()
                .containsKey(OptipushConstants.PushSchemaKeys.IS_OPTIPUSH)) {
            Optimove.getInstance()
                    .getOptipushHandlerProvider()
                    .getOptipushHandler()
                    .optipushMessageCommand(remoteMessage
                            , executionTimeInMilliseconds);
            return true;
        }

        // If reached here, the message was not handled by Optimove
        return false;
    }

}