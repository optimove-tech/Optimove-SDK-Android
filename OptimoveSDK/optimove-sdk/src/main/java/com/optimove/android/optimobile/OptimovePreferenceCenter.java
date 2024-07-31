package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class OptimovePreferenceCenter {
    private static OptimovePreferenceCenter shared;

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
    }

    //==============================================================================================
    //-- Public APIs

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
        // check for customer id

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
        // check for customer id

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
            Preferences preferences = null;

            // fetch preferences and map to attributes like below

            // example preferences attributes
            OptimovePreferenceCenter.Channel[] configuredChannels = new OptimovePreferenceCenter.Channel[] {Channel.MOBILE_PUSH, Channel.WEB_PUSH, Channel.SMS};
            Topic[] customerPreferences = new Topic[] {
                    new Topic("topic-id-1", "topic-1", "topic-1-desc", new Channel[] {Channel.MOBILE_PUSH}),
                    new Topic("topic-id-2", "topic-2", "topic-2-desc", new Channel[] {Channel.WEB_PUSH}),
                    new Topic("topic-id-3", "topic-3", "topic-3-desc", new Channel[] {Channel.SMS})
            };

            preferences = new Preferences(configuredChannels, customerPreferences);

            this.fireCallback(preferences);
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
            Boolean result = false;

            // set customer preferences and return result

            this.fireCallback(result);
        }

        private void fireCallback(Boolean result) {
            Optimobile.handler.post(() -> SetPreferencesRunnable.this.callback.run(result));
        }
    }
}



