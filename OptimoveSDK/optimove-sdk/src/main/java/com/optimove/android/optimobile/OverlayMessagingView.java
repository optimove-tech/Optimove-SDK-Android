package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class OverlayMessagingView extends BaseMessageView {

    private static final String TAG = OverlayMessagingView.class.getName();

    private static final String BUTTON_ACTION_CLOSE_MESSAGE = "closeMessage";
    private static final String BUTTON_ACTION_OPEN_URL = "openUrl";

    interface Listener {
        @UiThread void onMessageClosed(OverlayMessagingMessage message);
        @UiThread void onClicked(OverlayMessagingMessage message, JSONObject props);
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

    @UiThread
    private void executeActions(Activity currentActivity, List<ExecutableAction> actions) {
        // Handle 'secondary' actions
        for (ExecutableAction action : actions) {
            switch (action.getType()) {
                case BUTTON_ACTION_CLOSE_MESSAGE:
                    fireClickedEvent(true);
                    closeCurrentMessage(MessageCloseSource.CLICK);
                    break;
            }
        }

        // Handle 'terminating' actions
        for (ExecutableAction action : actions) {
            switch (action.getType()) {
                case BUTTON_ACTION_OPEN_URL:
                    // TODO: this should close current message?
                    fireClickedEvent(false);
                    this.openUrl(currentActivity, action.getUrl());
                    return;
            }
        }
    }

    private void fireClickedEvent(boolean closing) {
        try {
            JSONObject props = new JSONObject();
            props.put("closing", closing);
            listener.onClicked(currentMessage, props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void openUrl(Activity currentActivity, String uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (browserIntent.resolveActivity(currentActivity.getPackageManager()) != null) {
            currentActivity.startActivity(browserIntent);
        }
    }

    private List<ExecutableAction> parseButtonActionData(@NonNull JSONObject data) {
        List<ExecutableAction> actions = new ArrayList<>();
        JSONArray rawActions = data.optJSONArray("actions");

        if (null == rawActions) {
            return actions;
        }

        for (int i = 0; i < rawActions.length(); i++) {
            JSONObject rawAction = rawActions.optJSONObject(i);

            String actionType = rawAction.optString("type");
            JSONObject rawActionData = rawAction.optJSONObject("data");

            ExecutableAction action = new ExecutableAction();
            action.setType(actionType);

            switch (actionType) {
                case BUTTON_ACTION_OPEN_URL:
                    if (null == rawActionData) {
                        continue;
                    }
                    String url = rawActionData.optString("url");
                    action.setUrl(url);
                    break;
                default:
                    break;
            }
            actions.add(action);
        }

        return actions;
    }

    private static class ExecutableAction {
        String type;
        String url;

        void setType(String type) { this.type = type; }
        void setUrl(String url) { this.url = url; }

        String getType() { return type; }
        String getUrl() { return url; }
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
            case CLICK:
                // event already tracked when closing click action executed
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
        if (null == data) {
            return;
        }

        List<ExecutableAction> actions = this.parseButtonActionData(data);
        currentActivity.runOnUiThread(() -> this.executeActions(currentActivity, actions));
    }


}
