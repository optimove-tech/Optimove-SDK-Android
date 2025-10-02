package com.optimove.android.optimobile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OptimoveInApp {
    InAppDeepLinkHandlerInterface inAppDeepLinkHandler = null;
    static InAppMessagePresenter presenter;
    private static OptimoveInApp shared;
    @NonNull
    private final Application application;

    public enum InboxMessagePresentationResult {
        FAILED,
        FAILED_EXPIRED,
        PRESENTED,
        PAUSED
    }

    public interface InAppInboxUpdatedHandler extends Runnable {
        void run();
    }

    private InAppInboxUpdatedHandler inboxUpdatedHandler;

    public interface InAppInboxSummaryHandler {
        void run(@Nullable InAppInboxSummary summary);
    }

    private OptimoveInApp(@NonNull Application application) {
        this.application = application;
    }

    //==============================================================================================
    //-- Public APIs

    public static OptimoveInApp getInstance() {
        if (shared == null) {
            throw new IllegalStateException("OptimoveInApp is not initialized");
        }
        return shared;
    }

    /**
     * Returns up to 50 non-expired in-app messages stored in inbox
     */
    public List<InAppInboxItem> getInboxItems() {
        boolean inAppEnabled = isInAppEnabled();
        if (!inAppEnabled) {
            throw new RuntimeException("Optimobile: It is only possible to read In App inbox if In App messaging is enabled");
        }

        return InAppMessageService.readInboxItems(application);
    }

    /**
     * Presents selected inbox item
     *
     * @param item inbox item to present
     */
    public InboxMessagePresentationResult presentInboxMessage(@NonNull InAppInboxItem item) {
        boolean inAppEnabled = isInAppEnabled();
        if (!inAppEnabled) {
            throw new RuntimeException("Optimobile: It is only possible to present In App inbox if In App messaging is enabled");
        }

        return InAppMessageService.presentMessage(application, item);
    }

    /**
     * Deletes selected inbox item
     *
     * @param item inbox item to delete
     */
    public boolean deleteMessageFromInbox(@NonNull InAppInboxItem item) {
        return InAppMessageService.deleteMessageFromInbox(application, item.getId());
    }

    /**
     * Marks selected inbox item as read
     *
     * @param item inbox item to mark as read
     */
    public boolean markAsRead(@NonNull InAppInboxItem item) {
        if (item.isRead()) {
            return false;
        }

        boolean res = InAppMessageService.markInboxItemRead(application, item.getId(), true);
        maybeRunInboxUpdatedHandler(res);

        return res;
    }

    /**
     * Marks all inbox items as read.
     *
     */
    public boolean markAllInboxItemsAsRead() {
        return InAppMessageService.markAllInboxItemsAsRead(application);
    }

    /**
     * Set a handler to run when inbox has changes which might be relevant for presentation.
     * These concern messages with inbox set: fetched new message, message evicted, message opened, message deleted, message marked as read.
     * Handler runs on UI thread.
     *
     * @param inboxUpdatedHandler handler
     */
    public void setOnInboxUpdated(@Nullable InAppInboxUpdatedHandler inboxUpdatedHandler) {
        this.inboxUpdatedHandler = inboxUpdatedHandler;
    }

    /**
     * Asynchronously runs inbox summary handler on UI thread. Handler receives a single argument InAppInboxSummary
     *
     * @param inboxSummaryHandler handler
     */
    public void getInboxSummaryAsync(@NonNull InAppInboxSummaryHandler inboxSummaryHandler) {
        Runnable task = new InAppContract.ReadInboxSummaryRunnable(application, inboxSummaryHandler);
        Optimobile.executorService.submit(task);
    }


    /**
     * Used to update in-app consent when enablement strategy is EXPLICIT_BY_USER
     *
     * @param consentGiven
     */

    public void updateConsentForUser(boolean consentGiven) {
        if (Optimove.getConfig().getInAppConsentStrategy() != OptimoveConfig.InAppConsentStrategy.EXPLICIT_BY_USER) {
            throw new RuntimeException("Optimobile: It is only possible to update In App consent for user if consent strategy is set to EXPLICIT_BY_USER");
        }

        boolean inAppWasEnabled = isInAppEnabled();
        if (consentGiven != inAppWasEnabled) {
            updateInAppEnablementFlags(consentGiven);
            toggleInAppMessageMonitoring(consentGiven);
        }
    }

    /**
     * Allows setting the handler you want to use for in-app deep-link buttons
     *
     * @param handler
     */
    public void setDeepLinkHandler(@Nullable InAppDeepLinkHandlerInterface handler) {
        if (handler == null) {
            this.inAppDeepLinkHandler = null;
        } else {
            // Store a strong ref to a wrapper that only weakly references the app's handler
            this.inAppDeepLinkHandler = new WeakDeepLinkHandler(application, handler);
        }
    }

    public void setDisplayMode(OptimoveConfig.InAppDisplayMode mode) {
        presenter.setDisplayMode(mode);
    }


    //==============================================================================================
    //-- Internal Helpers

    static void initialize(Application application, OptimoveConfig currentConfig) {
        shared = new OptimoveInApp(application);

        OptimoveConfig.InAppConsentStrategy strategy = currentConfig.getInAppConsentStrategy();
        boolean inAppEnabled = shared.isInAppEnabled();

        if (strategy == OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL && !inAppEnabled) {
            inAppEnabled = true;
            shared.updateInAppEnablementFlags(true);
        } else if (strategy == null && inAppEnabled) {
            inAppEnabled = false;
            shared.updateInAppEnablementFlags(false);
            InAppMessageService.clearAllMessages(application);
            shared.clearLastSyncTime(application);
        }

        presenter = new InAppMessagePresenter(application, currentConfig.getInAppDisplayMode());

        shared.toggleInAppMessageMonitoring(inAppEnabled);
    }

    private void updateInAppEnablementFlags(boolean enabled) {
        updateRemoteInAppEnablementFlag(enabled);
        updateLocalInAppEnablementFlag(enabled);
    }

    boolean isInAppEnabled() {
        SharedPreferences prefs = application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPrefs.IN_APP_ENABLED, false);
    }

    private void updateRemoteInAppEnablementFlag(boolean enabled) {
        try {
            JSONObject params = new JSONObject().put("consented", enabled);

            Optimobile.trackEvent(application, "k.inApp.statusUpdated", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateLocalInAppEnablementFlag(boolean enabled) {
        SharedPreferences prefs = application.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefs.IN_APP_ENABLED, enabled);
        editor.apply();
    }

    private void clearLastSyncTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SharedPrefs.IN_APP_LAST_SYNC_TIME);
        editor.apply();
    }

    void handleInAppUserChange(Context context, OptimoveConfig currentConfig) {
        InAppMessageService.clearAllMessages(context);
        clearLastSyncTime(context);

        OptimoveConfig.InAppConsentStrategy strategy = currentConfig.getInAppConsentStrategy();
        if (strategy == OptimoveConfig.InAppConsentStrategy.EXPLICIT_BY_USER) {
            updateLocalInAppEnablementFlag(false);
            toggleInAppMessageMonitoring(false);
        } else if (strategy == OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL) {
            updateRemoteInAppEnablementFlag(true);

            fetchMessages();
        } else if (strategy == null) {
            updateRemoteInAppEnablementFlag(false);
        }
    }

    private void toggleInAppMessageMonitoring(boolean enabled) {
        if (enabled) {
            InAppSyncWorker.startPeriodicFetches(application);

            fetchMessages();
        } else {
            InAppSyncWorker.cancelPeriodicFetches(application);
        }
    }

    private void fetchMessages() {
        Optimobile.executorService.submit(() -> {
            InAppMessageService.fetch(application, true);
        });
    }

    void maybeRunInboxUpdatedHandler(boolean inboxNeedsUpdate) {
        if (!inboxNeedsUpdate || inboxUpdatedHandler == null) {
            return;
        }

        Optimobile.handler.post(inboxUpdatedHandler);
    }

    private static final class WeakDeepLinkHandler implements InAppDeepLinkHandlerInterface {
        private static final String TAG = "OptimoveInApp";
        private final java.lang.ref.WeakReference<InAppDeepLinkHandlerInterface> delegateRef;
        private final android.content.Context appContext;
        // Optional strict flag (wire this from your config if you like)
        private final boolean strictMode;

        WeakDeepLinkHandler(@NonNull android.content.Context context,
                            @NonNull InAppDeepLinkHandlerInterface delegate) {
            this(context, delegate, /*strictMode=*/false);
        }

        WeakDeepLinkHandler(@NonNull android.content.Context context,
                            @NonNull InAppDeepLinkHandlerInterface delegate,
                            boolean strictMode) {
            this.appContext = context.getApplicationContext();
            this.delegateRef = new java.lang.ref.WeakReference<>(delegate);
            this.strictMode = strictMode;
        }

        @Override
        public void handle(android.content.Context context,
                           InAppButtonPress buttonPress) {
            InAppDeepLinkHandlerInterface delegate = delegateRef.get();
            if (delegate == null) return;

            try {
                // Use app context to avoid propagating an Activity reference
                delegate.handle(appContext, buttonPress);
            } catch (RuntimeException e) {
                // Log with full stacktrace so integrators see the failure in Logcat
                android.util.Log.e(TAG, "DeepLinkHandler threw a RuntimeException", e);
                if (strictMode) throw e; // crash in strict/debug builds if desired
            } catch (Throwable t) {
                // Catch-all to prevent app crash, but still surface it
                android.util.Log.e(TAG, "DeepLinkHandler threw a non-fatal Throwable", t);
                if (strictMode) {
                    // Wrapping in RuntimeException so you can opt-in to fail fast
                    throw new RuntimeException("DeepLinkHandler non-fatal error", t);
                }
            }
        }
    }
}
