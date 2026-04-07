package com.optimove.android.optimobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class InAppMessageView extends BaseMessageView {

    private static final String TAG = InAppMessageView.class.getName();

    private static final String BUTTON_ACTION_CLOSE_MESSAGE = "closeMessage";
    private static final String BUTTON_ACTION_TRACK_CONVERSION_EVENT = "trackConversionEvent";
    private static final String BUTTON_ACTION_OPEN_URL = "openUrl";
    private static final String BUTTON_ACTION_DEEP_LINK = "deepLink";
    private static final String BUTTON_ACTION_REQUEST_APP_STORE_RATING = "requestAppStoreRating";
    private static final String BUTTON_ACTION_PUSH_REGISTER = "promptPushPermission";

    @NonNull
    private final InAppMessagePresenter presenter;
    @NonNull
    private InAppMessage currentMessage;
    @Nullable
    private final String region;

    @UiThread
    InAppMessageView(@NonNull InAppMessagePresenter presenter,
                     @NonNull InAppMessage message,
                     @NonNull Activity currentActivity,
                     @NonNull String iarUrl,
                     @Nullable String region) {
        super(currentActivity, true);

        this.presenter = presenter;
        this.currentMessage = message;
        this.region = region;

        showWebView(currentActivity, iarUrl);
    }

    @UiThread
    void showMessage(@NonNull InAppMessage message) {
        if (currentMessage.getInAppId() == message.getInAppId()) {
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
                    closeCurrentMessage(MessageCloseSource.CLIENT);
                    break;
                case BUTTON_ACTION_TRACK_CONVERSION_EVENT:
                    Optimobile.trackEventImmediately(currentActivity, action.getEventType(), action.getConversionEventData());
                    break;
            }
        }

        // Handle 'terminating' actions
        for (ExecutableAction action : actions) {
            switch (action.getType()) {
                case BUTTON_ACTION_OPEN_URL:
                    presenter.cancelCurrentPresentationQueue();

                    this.openUrl(currentActivity, action.getUrl());
                    return;
                case BUTTON_ACTION_DEEP_LINK:
                    if (null != OptimoveInApp.getInstance().inAppDeepLinkHandler) {
                        presenter.cancelCurrentPresentationQueue();

                        OptimoveInApp.getInstance().inAppDeepLinkHandler.handle(currentActivity.getApplicationContext(),
                                new InAppDeepLinkHandlerInterface.InAppButtonPress(
                                        action.getDeepLink(),
                                        currentMessage.getInAppId(),
                                        currentMessage.getData()
                                )
                        );
                    }
                    return;
                case BUTTON_ACTION_REQUEST_APP_STORE_RATING:
                    presenter.cancelCurrentPresentationQueue();

                    this.openPlayStore(currentActivity);
                    return;
                case BUTTON_ACTION_PUSH_REGISTER:
                    presenter.cancelCurrentPresentationQueue();

                    Optimobile.pushRequestDeviceToken(currentActivity);
                    return;
            }
        }
    }

    private void openPlayStore(Activity currentActivity) {
        String packageName = currentActivity.getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        if (intent.resolveActivity(currentActivity.getPackageManager()) != null) {
            currentActivity.startActivity(intent);
            return;
        }

        intent.setData(Uri.parse("https://play.google.com/store/apps/details?" + packageName));
        currentActivity.startActivity(intent);
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
                case BUTTON_ACTION_TRACK_CONVERSION_EVENT:
                    if (null == rawActionData) {
                        continue;
                    }
                    String eventType = rawActionData.optString("eventType");
                    JSONObject eventData = rawActionData.optJSONObject("data");
                    action.setEventType(eventType);
                    action.setConversionEventData(eventData);
                    break;
                case BUTTON_ACTION_OPEN_URL:
                    if (null == rawActionData) {
                        continue;
                    }
                    String url = rawActionData.optString("url");
                    action.setUrl(url);
                    break;
                case BUTTON_ACTION_DEEP_LINK:
                    if (null == rawActionData) {
                        continue;
                    }
                    JSONObject deepLink = rawActionData.optJSONObject("deepLink");
                    action.setDeepLink(deepLink);
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
        String channelUuid;
        String eventType;
        JSONObject deepLink;
        JSONObject conversionEventData;

        void setType(String type) {
            this.type = type;
        }

        void setChannelUuid(String channelUuid) {
            this.channelUuid = channelUuid;
        }

        void setEventType(String eventType) {
            this.eventType = eventType;
        }

        void setConversionEventData(JSONObject data) {
            conversionEventData = data;
        }

        void setUrl(String url) {
            this.url = url;
        }

        void setDeepLink(JSONObject deepLink) {
            this.deepLink = deepLink;
        }

        String getType() {
            return type;
        }

        String getUrl() {
            return url;
        }

        String getChannelUuid() {
            return channelUuid;
        }

        String getEventType() {
            return eventType;
        }

        JSONObject getConversionEventData() {
            return conversionEventData;
        }

        JSONObject getDeepLink() {
            return deepLink;
        }
    }


    // - Implementations for abstracts

    @Override
    protected JSONObject getCurrentMessageContent() {
        JSONObject content = currentMessage.getContent();
        if (region != null) {
            try {
                content.put("region", region);
            } catch (JSONException e) {
                Log.w(TAG, "Could not pass region to In-App renderer");
            }
        }

        return content;
    }

    @Override
    protected void onMessageClosedByClient() {
        // this happens when IAR client closed message
        presenter.messageClosed();
    }

    @Override
    protected void onViewError() {
        presenter.onViewError();
    }

    @Override
    protected void onCommand(JSONObject data) {
        // noop: in-app uses EXECUTE_ACTIONS, not COMMAND
    }

    @Override
    protected void onMessageCloseRequested(MessageCloseSource source) {
        // this happens when we told IAR to close message
        // TODO: the split keeps existing behaviour, but simpler would be to run this when message closed by client as well (or was there a reason?)
        InAppMessageService.handleMessageClosed(currentActivity, currentMessage);
    }

    @Override
    protected void onMessageOpened() {
        InAppMessageService.handleMessageOpened(currentActivity, currentMessage);
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
