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

            // fetch preferences

            // example preferences attributes
            int[] configuredChannels = new int[] {489, 490, 493};
            Topic[] customerPreferences = new Topic[] {
                    new Topic("topic-id-1", "topic-1", "topic-1-desc", new int[] {489}),
                    new Topic("topic-id-2", "topic-2", "topic-2-desc", new int[] {490}),
                    new Topic("topic-id-3", "topic-3", "topic-3-desc", new int[] {493})
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

            // set customer preferences

            this.fireCallback(result);
        }

        private void fireCallback(Boolean result) {
            Optimobile.handler.post(() -> SetPreferencesRunnable.this.callback.run(result));
        }
    }
}



