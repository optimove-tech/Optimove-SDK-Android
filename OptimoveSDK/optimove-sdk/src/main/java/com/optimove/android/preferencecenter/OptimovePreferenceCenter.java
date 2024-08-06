package com.optimove.android.preferencecenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.tools.networking.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimovePreferenceCenter {
    private static final String TAG = OptimovePreferenceCenter.class.getName();
    private static OptimovePreferenceCenter shared;
    private static OptimoveConfig config;

    /**
     * package
     */
    static ExecutorService executorService;
    /**
     * package
     */
    static final Handler handler = new Handler(Looper.getMainLooper());

    public interface PreferencesGetHandler {
        void run(@Nullable Preferences preferences);
    }

    public interface PreferencesSetHandler {
        void run(Boolean result);
    }

    public enum Channel {
        MOBILE_PUSH(489), WEB_PUSH(490), SMS(493);
        private final int channel;

        Channel(int channel) {
            this.channel = channel;
        }

        @NonNull
        public int getValue() {
            return channel;
        }

        private static Channel getChannelByValue(int value) {
            switch (value) {
                case 489:
                    return MOBILE_PUSH;
                case 490:
                    return WEB_PUSH;
                case 493:
                    return SMS;
                default:
                    throw new IllegalArgumentException("Preference center does not support channel " + value);
            }
        }
    }

    public static OptimovePreferenceCenter getInstance() {
        if (shared == null) {
            throw new IllegalStateException("OptimovePreferenceCenter is not initialized");
        }
        return shared;
    }

    /**
     * Initializes an instance of OptimovePreferenceCenter
     *
     * @param currentConfig current config
     */
    public static void initialize(OptimoveConfig currentConfig) {
        shared = new OptimovePreferenceCenter();
        config = currentConfig;

        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Asynchronously runs preferences get handler on UI thread. Handler receives a single argument Preferences
     *
     * @param preferencesGetHandler handler
     */
    public void getPreferencesAsync(@NonNull PreferencesGetHandler preferencesGetHandler) {
        UserInfo userInfo = Optimove.getInstance().getUserInfo();

        if (userInfo.getUserId() == null || Objects.equals(userInfo.getUserId(), userInfo.getVisitorId())) {
            Log.d(TAG, "Customer ID is not set");
            return;
        }

        Runnable task = new GetPreferencesRunnable(preferencesGetHandler);
        executorService.submit(task);
    }

    /**
     * Asynchronously runs preferences set handler on UI thread. Handler receives a single Boolean result argument
     *
     * @param preferencesSetHandler handler
     * @param updates               list of preference updates to set
     */
    public void setCustomerPreferencesAsync(@NonNull PreferencesSetHandler preferencesSetHandler, List<PreferenceUpdate> updates) {
        UserInfo userInfo = Optimove.getInstance().getUserInfo();

        if (userInfo.getUserId() == null || Objects.equals(userInfo.getUserId(), userInfo.getVisitorId())) {
            Log.d(TAG, "Customer ID is not set");
            return;
        }

        Runnable task = new SetPreferencesRunnable(preferencesSetHandler, updates);
        executorService.submit(task);
    }

    static class GetPreferencesRunnable implements Runnable {
        private final PreferencesGetHandler callback;

        GetPreferencesRunnable(PreferencesGetHandler callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            String mappedRegion = shared.mapRegion(config.getRegion());
            HttpClient httpClient = HttpClient.getInstance();
            Preferences preferences = null;
            try {
                String encodedCustomerId = URLEncoder.encode(Optimove.getInstance().getUserInfo().getUserId(), "UTF-8");
                String url = "https://preference-center-" + mappedRegion + ".optimove.net/api/v1/preferences?customerId=" + encodedCustomerId + "&brandGroupId=" + config.getBrandGroupId();

                try (Response response = httpClient.getSync(url, Optimove.getInstance().getTenantInfo().getTenantId())) {
                    if (!response.isSuccessful()) {
                        logFailedResponse(response);
                    } else {
                        preferences = mapResponseToPreferences(response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.fireCallback(preferences);
        }

        private void fireCallback(@Nullable Preferences preferences) {
            handler.post(() -> GetPreferencesRunnable.this.callback.run(preferences));
        }
    }

    static class SetPreferencesRunnable implements Runnable {
        private final PreferencesSetHandler callback;
        private final List<PreferenceUpdate> updates;

        SetPreferencesRunnable(PreferencesSetHandler callback, List<PreferenceUpdate> updates) {
            this.callback = callback;
            this.updates = updates;
        }

        @Override
        public void run() {
            String mappedRegion = shared.mapRegion(config.getRegion());
            HttpClient httpClient = HttpClient.getInstance();
            boolean result = false;
            try {
                String encodedCustomerId = URLEncoder.encode(Optimove.getInstance().getUserInfo().getUserId(), "UTF-8");
                String url = "https://preference-center-" + mappedRegion + ".optimove.net/api/v1/preferences?customerId=" + encodedCustomerId + "&brandGroupId=" + config.getBrandGroupId();
                JSONArray data = mapPreferenceUpdatesToArray(updates);

                try (Response response = httpClient.putSync(url, data, Optimove.getInstance().getTenantInfo().getTenantId())) {
                    result = response.isSuccessful();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            this.fireCallback(result);
        }

        private void fireCallback(Boolean result) {
            handler.post(() -> SetPreferencesRunnable.this.callback.run(result));
        }
    }

    private String mapRegion(String region) {
        switch (region) {
            case "uk-1":
                return "dev-pb";
            case "eu-central-2":
                return "eu";
            case "us-east-1":
                return "us";
            default:
                throw new IllegalArgumentException("Region is not supported: " + region);
        }
    }

    private static JSONArray mapPreferenceUpdatesToArray(List<PreferenceUpdate> updates) throws JSONException {
        List<Object> mappedUpdates = new ArrayList<>();

        for (int i = 0; i < updates.size(); i++) {
            String updateId = updates.get(i).getTopicId();
            List<Channel> channels = updates.get(i).getSubscribedChannels();
            List<Integer> mappedChannels = new ArrayList<>();
            for (int j = 0; j < channels.size(); j++) {
                mappedChannels.add(channels.get(i).getValue());
            }

            Object mappedUpdate = new Object() {
                final String id = updateId;
                final List<Integer> subscribedChannels = mappedChannels;
            };
            mappedUpdates.add(mappedUpdate);
        }

        return new JSONArray(mappedUpdates.toArray());
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
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

    private static void logFailedResponse(Response response) {
        switch (response.code()) {
            case 404:
                Log.e(TAG, "User not found");
                break;
            case 422:
                try {
                    Log.d(TAG, response.body().string());
                } catch (NullPointerException | IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                Log.d(TAG, response.message());
                break;
        }
    }
}



