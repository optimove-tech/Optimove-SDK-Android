package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.OptimoveConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OptimovePreferenceCenter {
    private static final String TAG = OptimovePreferenceCenter.class.getName();
    private static OptimovePreferenceCenter shared;
    private static OptimoveConfig config;
    private static @Nullable String customerId;

    public interface PreferencesGetHandler {
        void run(@Nullable Preferences preferences);
    }

    public interface PreferencesSetHandler {
        void run(Boolean result);
    }

    public enum Channel {
        MOBILE_PUSH(489),
        WEB_PUSH(490),
        SMS(493);
        private final int channel;

        Channel(int channel) {
            this.channel = channel;
        }

        @NonNull
        public int getValue() {
            return channel;
        }

        public static Channel getChannelByValue ( int value) {
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
     * Asynchronously runs preferences get handler on UI thread. Handler receives a single argument Preferences
     *
     * @param preferencesGetHandler handler
     */
    public void getPreferencesAsync(@NonNull PreferencesGetHandler preferencesGetHandler) {
        if (customerId == null) {
            Optimobile.log(TAG, "No customer ID has been set");
            return;
        }

        Runnable task = new GetPreferencesRunnable(preferencesGetHandler);
        Optimobile.executorService.submit(task);
    }

    /**
     *  Asynchronously runs preferences set handler on UI thread. Handler receives a single Boolean result argument
     *
     * @param preferencesSetHandler handler
     * @param updates list of preference updates to set
     */
    public void setCustomerPreferencesAsync(@NonNull PreferencesSetHandler preferencesSetHandler, List<PreferenceUpdate> updates) {
        if (customerId == null) {
            Optimobile.log(TAG, "No customer ID has been set");
            return;
        }

        Runnable task = new SetPreferencesRunnable(preferencesSetHandler, updates);
        Optimobile.executorService.submit(task);
    }

    static class GetPreferencesRunnable implements Runnable {
        private final PreferencesGetHandler callback;

        GetPreferencesRunnable(PreferencesGetHandler callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            String mappedRegion = shared.mapRegion(config.getRegion());
            String url = "https://preference-center-" + mappedRegion + ".optimove.net/api/v1/preferences?customerId=" + customerId + "&brandGroupId=" + config.getBrandGroupId();

            OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();

            try (Response response = httpClient.getSync(url)) {
                if (!response.isSuccessful()) {
                    logFailedResponse(response);
                } else {
                   Preferences preferences = mapResponseToPreferences(response);

                   if (preferences == null) {
                       // log?
                       return;
                   }

                    this.fireCallback(preferences);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Optimobile.PartialInitialisationException e) {
                // noop
            }
        }

        private void fireCallback(Preferences preferences) {
            Optimobile.handler.post(() -> GetPreferencesRunnable.this.callback.run(preferences));
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
            String url = "https://preference-center-" + mappedRegion + ".optimove.net/api/v1/preferences?customerId=" + customerId + "&brandGroupId=" + config.getBrandGroupId();

            OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();

            try {
                //TODO: Need to map Channels to int for updates
                JSONArray data = new JSONArray(updates.toArray());
                Response response = httpClient.putSync(url, data);

                if (!response.isSuccessful()) {
                    this.fireCallback(false);
                    return;
                } else {
                    this.fireCallback(true);
                    return;
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (Optimobile.PartialInitialisationException e) {
                // noop
            }

            this.fireCallback(false);
        }

        private void fireCallback(Boolean result) {
            Optimobile.handler.post(() -> SetPreferencesRunnable.this.callback.run(result));
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

    static void initialize(OptimoveConfig currentConfig, String newCustomerId) {
        shared = new OptimovePreferenceCenter();
        config = currentConfig;
        customerId = newCustomerId;
    }

    void setCustomerId(@Nullable String newCustomerId) {
        customerId =  newCustomerId;
    }

    public static Preferences mapResponseToPreferences(Response response) {
        try {
            JSONObject data = new JSONObject(response.body().string());

            JSONArray channels = data.getJSONArray("channels");
            List<Channel> configuredChannels = new ArrayList<>();

            int len = channels.length();
            for (int i = 0; i < len; i++) {
                configuredChannels.add(Channel.getChannelByValue(channels.getInt(i)));
            }

            JSONArray topics = data.getJSONArray("topics");
            List<PreferenceCenterTopic> customerPreferences = new ArrayList<>();

            int topicLength = topics.length();
            for (int i = 0; i < topicLength; i++) {
                JSONObject topicObj = topics.getJSONObject(i);

                List<Channel> subscribedChannels = new ArrayList<>();
                JSONArray channelSubscriptionArray = topicObj.getJSONArray("channelSubscription");
                for (int j = 0; j < channelSubscriptionArray.length(); j++) {
                    subscribedChannels.add(Channel.getChannelByValue(channelSubscriptionArray.getInt(j)));
                }

                PreferenceCenterTopic topic = new PreferenceCenterTopic(
                        topicObj.getString("topicId"),
                        topicObj.getString("topicName"),
                        topicObj.getString("topicDescription"),
                        subscribedChannels
                );

                customerPreferences.add(topic);
            }

            return new Preferences(configuredChannels, customerPreferences);
        } catch (NullPointerException | JSONException | IOException e) {
            Optimobile.log(TAG, e.getMessage());
            return null;
        }
    }

    private static void logFailedResponse(Response response) {
        switch (response.code()) {
            case 404:
                Optimobile.log(TAG, "User not found");
                break;
            case 422:
                try {
                    Optimobile.log(TAG, response.body().string());
                } catch (NullPointerException | IOException e) {
                    Optimobile.log(TAG, e.getMessage());
                }
                break;
            default:
                Optimobile.log(TAG, response.message());
                break;
        }
    }
}



