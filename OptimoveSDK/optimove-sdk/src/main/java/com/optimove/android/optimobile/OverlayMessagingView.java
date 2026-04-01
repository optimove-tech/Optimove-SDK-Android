package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

class OverlayMessagingView extends BaseMessageView {

    private static final String SDK_ACTION_OPEN_DEEP_LINK = "OPEN_DEEP_LINK";

    private static class RendererCommand {
        final boolean close;
        @Nullable final List<OverlayMessagingRendererEvent> events;
        @Nullable final JSONArray sdkActions;

        private RendererCommand(boolean close, @Nullable List<OverlayMessagingRendererEvent> events, @Nullable JSONArray sdkActions) {
            this.close = close;
            this.events = events;
            this.sdkActions = sdkActions;
        }

        @Nullable
        static RendererCommand parse(@Nullable JSONObject data) {
            if (data == null) return null;
            List<OverlayMessagingRendererEvent> events = OverlayMessagingRendererEvent.parseAll(data.optJSONArray("events"));
            return new RendererCommand(
                data.optBoolean("close", false),
                events.isEmpty() ? null : events,
                data.optJSONArray("executeSdkActions")
            );
        }
    }

    interface Listener {
        @UiThread void onMessageClosed(OverlayMessagingMessage message);
        @UiThread void onEvents(OverlayMessagingMessage message, List<OverlayMessagingRendererEvent> events);
        @UiThread void onDismissed(OverlayMessagingMessage message);
        @UiThread void onViewError(OverlayMessagingMessage message);
    }

    @NonNull
    private OverlayMessagingMessage currentMessage;
    @NonNull
    private final Listener listener;

    @UiThread
    OverlayMessagingView(@NonNull OverlayMessagingMessage message,
                         @NonNull Activity currentActivity,
                         @NonNull String iarUrl,
                         @NonNull Listener listener) {
        super(currentActivity);

        this.currentMessage = message;
        this.listener = listener;

        showWebView(currentActivity, iarUrl);
    }

    @UiThread
    void showMessage(@NonNull OverlayMessagingMessage message) {
        if (currentMessage.getId() == message.getId()) {
            return;
        }
        currentMessage = message;
        sendCurrentMessageToClient();
    }

    private void openUrl(Activity currentActivity, String uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (browserIntent.resolveActivity(currentActivity.getPackageManager()) != null) {
            currentActivity.startActivity(browserIntent);
        }
    }


    // - Implementations for abstracts

    @Override
    protected JSONObject getCurrentMessageContent() {
        return currentMessage.getContent();
    }

    @Override
    protected void onViewError() {
        listener.onViewError(currentMessage);
    }

    @Override
    protected void onMessageClosedByClient() {
        listener.onMessageClosed(currentMessage);
    }

    @Override
    protected void onMessageCloseRequested(MessageCloseSource source) {
        switch (source) {
            case CLIENT:
                // events already tracked when COMMAND handled
                break;
            case HARDWARE:
                listener.onDismissed(currentMessage);
                break;
        }
    }

    @Override
    protected void onMessageOpened() {
        // noop: no event needed yet
    }

    @Override
    protected void onExecuteActions(JSONObject data) {
        // noop: OM uses COMMAND, not EXECUTE_ACTIONS
    }

    @Override
    protected void onCommand(JSONObject data) {
        RendererCommand command = RendererCommand.parse(data);
        if (command == null) {
            return;
        }

        currentActivity.runOnUiThread(() -> {
            if (command.events != null) {
                listener.onEvents(currentMessage, command.events);
            }

            if (command.close) {
                closeCurrentMessage(MessageCloseSource.CLIENT);
            }

            if (command.sdkActions == null) {
                return;
            }
            for (int i = 0; i < command.sdkActions.length(); i++) {
                JSONObject action = command.sdkActions.optJSONObject(i);
                if (action == null) continue;
                String type = action.optString("type");
                JSONObject actionData = action.optJSONObject("data");
                switch (type) {
                    case SDK_ACTION_OPEN_DEEP_LINK:
                        if (actionData != null) {
                            openUrl(currentActivity, actionData.optString("url"));
                        }
                        break;
                }
            }
        });
    }
}
