package com.optimove.android.preferencecenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.tools.networking.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimovePreferenceCenter {
    private static final String TAG = "OptimovePC";
    private static OptimovePreferenceCenter shared;

    static ExecutorService executorService;

    static final Handler handler = new Handler(Looper.getMainLooper());

    public interface PreferencesGetHandler {
        void run(ResultType result, @Nullable Preferences preferences);
    }

    public interface PreferencesSetHandler {
        void run(ResultType result);
    }

    public enum ResultType {
        SUCCESS,
        ERROR_CREDENTIALS_NOT_SET,
        ERROR_USER_NOT_SET,
        ERROR
    }

    public static OptimovePreferenceCenter getInstance() {
        if (shared == null) {
            throw new IllegalStateException("OptimovePreferenceCenter is not initialized");
        }
        return shared;
    }

    /**
     * Initializes an instance of OptimovePreferenceCenter
     * <p>
     * This API is intended for internal SDK use. Do not call this API or depend on it in your app.
     */
    public static void initialize() {
        shared = new OptimovePreferenceCenter();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Asynchronously runs preferences get handler on UI thread. Handler receives a single argument Preferences
     *
     * @param preferencesGetHandler handler
     */
    public void getPreferencesAsync(@NonNull PreferencesGetHandler preferencesGetHandler) {
        Config config = Optimove.getConfig().getPreferenceCenterConfig();
        if (config == null) {
            Log.e(TAG, "Preference center credentials are not set");
            handler.post(() -> preferencesGetHandler.run(ResultType.ERROR_CREDENTIALS_NOT_SET, null));
            return;
        }

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId();

        if (userId == null || Objects.equals(userId, userInfo.getVisitorId())) {
            Log.w(TAG, "Customer ID is not set");
            handler.post(() -> preferencesGetHandler.run(ResultType.ERROR_USER_NOT_SET, null));
            return;
        }

        Runnable task = new GetPreferencesRunnable(config, userId, preferencesGetHandler);
        executorService.submit(task);
    }

    /**
     * Asynchronously runs preferences set handler on UI thread. Handler receives a single Boolean result argument
     *
     * @param preferencesSetHandler handler
     * @param updates               list of preference updates to set
     */
    public void setCustomerPreferencesAsync(@NonNull PreferencesSetHandler preferencesSetHandler, List<PreferenceUpdate> updates) {
        Config config = Optimove.getConfig().getPreferenceCenterConfig();
        if (config == null) {
            Log.e(TAG, "Preference center credentials are not set");
            handler.post(() -> preferencesSetHandler.run(ResultType.ERROR_CREDENTIALS_NOT_SET));
            return;
        }

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId();

        if (userId == null || Objects.equals(userId, userInfo.getVisitorId())) {
            Log.w(TAG, "Customer ID is not set");
            handler.post(() -> preferencesSetHandler.run(ResultType.ERROR_USER_NOT_SET));
            return;
        }

        Runnable task = new SetPreferencesRunnable(config, userId, preferencesSetHandler, updates);
        executorService.submit(task);
    }

    static class GetPreferencesRunnable implements Runnable {
        private final Config config;
        private final String customerId;
        private final PreferencesGetHandler callback;

        GetPreferencesRunnable(Config config, String customerId, PreferencesGetHandler callback) {
            this.config = config;
            this.customerId = customerId;
            this.callback = callback;
        }

        @Override
        public void run() {
            Preferences preferences = null;
            ResultType resultType = ResultType.ERROR;

            String region = config.getRegion();
            HttpClient httpClient = HttpClient.getInstance();

            try {
                String encodedCustomerId = URLEncoder.encode(this.customerId, "UTF-8");
                String url = "https://preference-center-" + region + ".optimove.net/api/v1/preferences?customerId=" + encodedCustomerId + "&brandGroupId=" + config.getBrandGroupId();

                try (Response response = httpClient.getSync(url, config.getTenantId())) {
                    if (!response.isSuccessful()) {
                        logFailedResponse(response);
                    } else {
                        preferences = mapResponseToPreferences(response);
                        resultType = ResultType.SUCCESS;
                    }
                }
            } catch (NullPointerException | IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(resultType, preferences);
        }

        private void fireCallback(ResultType result, @Nullable Preferences preferences) {
            handler.post(() -> GetPreferencesRunnable.this.callback.run(result, preferences));
        }
    }

    static class SetPreferencesRunnable implements Runnable {
        private final Config config;
        private final String customerId;
        private final PreferencesSetHandler callback;
        private final List<PreferenceUpdate> updates;

        SetPreferencesRunnable(Config config, String customerId, PreferencesSetHandler callback, List<PreferenceUpdate> updates) {
            this.config = config;
            this.customerId = customerId;
            this.callback = callback;
            this.updates = updates;
        }

        @Override
        public void run() {
            ResultType result = ResultType.ERROR;

            String region = config.getRegion();
            HttpClient httpClient = HttpClient.getInstance();

            try {
                String encodedCustomerId = URLEncoder.encode(this.customerId, "UTF-8");
                String url = "https://preference-center-" + region + ".optimove.net/api/v1/preferences?customerId=" + encodedCustomerId + "&brandGroupId=" + config.getBrandGroupId();
                JSONArray data = mapPreferenceUpdatesToArray(updates);

                try (Response response = httpClient.putSync(url, data, config.getTenantId())) {
                    if (!response.isSuccessful()) {
                        logFailedResponse(response);
                    } else {
                        result = ResultType.SUCCESS;
                    }
                }
            } catch (JSONException | IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result);
        }

        private void fireCallback(ResultType result) {
            handler.post(() -> SetPreferencesRunnable.this.callback.run(result));
        }
    }

    private static JSONArray mapPreferenceUpdatesToArray(List<PreferenceUpdate> updates) throws JSONException {
        JSONArray updatesArray = new JSONArray();

        for (int i = 0; i < updates.size(); i++) {
            String topicId = updates.get(i).getTopicId();
            List<Channel> channels = updates.get(i).getSubscribedChannels();
            JSONArray subscribedChannels = new JSONArray();
            for (int j = 0; j < channels.size(); j++) {
                subscribedChannels.put(channels.get(j).getValue());
            }

            JSONObject mappedUpdate = new JSONObject();
            mappedUpdate.put("topicId", topicId);
            mappedUpdate.put("channelSubscription", subscribedChannels);

            updatesArray.put(mappedUpdate);
        }

        return updatesArray;
    }

    private static Preferences mapResponseToPreferences(Response response) {
        try {
            JSONObject data = new JSONObject(response.body().string());

            JSONArray channels = data.getJSONArray("channels");
            List<Channel> configuredChannels = new ArrayList<>();

            int len = channels.length();
            for (int i = 0; i < len; i++) {
                configuredChannels.add(Channel.getChannelByValue(channels.getInt(i)));
            }

            JSONArray topics = data.getJSONArray("topics");
            List<Topic> customerPreferences = new ArrayList<>();

            int topicLength = topics.length();
            for (int i = 0; i < topicLength; i++) {
                JSONObject topicObj = topics.getJSONObject(i);

                List<Channel> subscribedChannels = new ArrayList<>();
                JSONArray channelSubscriptionArray = topicObj.getJSONArray("channelSubscription");
                for (int j = 0; j < channelSubscriptionArray.length(); j++) {
                    subscribedChannels.add(Channel.getChannelByValue(channelSubscriptionArray.getInt(j)));
                }

                Topic topic = new Topic(topicObj.getString("topicId"), topicObj.getString("topicName"), topicObj.getString("topicDescription"), subscribedChannels);

                customerPreferences.add(topic);
            }

            return new Preferences(configuredChannels, customerPreferences);
        } catch (NullPointerException | JSONException | IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private static void logFailedResponse(Response response) {
        int code = response.code();
        String msg = "Request failed with code " + code + ". ";
        String responseBodyAsStr = "";
        try {
            ResponseBody body = response.body();
            if (body != null) {
                responseBodyAsStr = body.string();
            }
        } catch (IOException e) {/**/}


        switch (code) {
            case 400: {
                Log.e(TAG, msg + "Check preference center configuration: " + responseBodyAsStr);
                break;
            }
            default:
                Log.e(TAG, msg + responseBodyAsStr);
                break;
        }
    }
}
