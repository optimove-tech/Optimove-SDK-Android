package com.optimove.android.optimobile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    
    @Nullable
    private InAppMessageInterceptor inAppMessageInterceptor = null;
    
    private long interceptorTimeoutMs = 5000; // Default 5 seconds

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
    public void setDeepLinkHandler(InAppDeepLinkHandlerInterface handler) {
        this.inAppDeepLinkHandler = handler;
    }

    public void setDisplayMode(OptimoveConfig.InAppDisplayMode mode) {
        presenter.setDisplayMode(mode);
    }

    /**
     * Sets an interceptor for conditional message display when mode is INTERCEPTED.
     * 
     * @param interceptor The interceptor to use, or null to remove
     */
    public void setInAppMessageInterceptor(@Nullable InAppMessageInterceptor interceptor) {
        this.inAppMessageInterceptor = interceptor;
        if (presenter != null) {
            presenter.setInAppMessageInterceptor(interceptor);
        }
    }

    @Nullable
    public InAppMessageInterceptor getInAppMessageInterceptor() {
        return inAppMessageInterceptor;
    }

    /**
     * Sets the timeout for message interceptor callbacks.
     * 
     * @param timeoutMs Timeout in milliseconds (minimum 1000ms, default 5000ms)
     */
    public void setInAppMessageInterceptorTimeout(long timeoutMs) {
        if (timeoutMs < 1000) {
            Log.w("OptimoveInApp", "Interceptor timeout must be at least 1000ms, using 1000ms");
            this.interceptorTimeoutMs = 1000;
        } else {
            this.interceptorTimeoutMs = timeoutMs;
        }
        
        if (presenter != null) {
            presenter.setInterceptorTimeout(timeoutMs);
        }
    }

    /**
     * Gets the current interceptor timeout in milliseconds.
     * 
     * @return Timeout in milliseconds
     */
    public long getInAppMessageInterceptorTimeout() {
        return interceptorTimeoutMs;
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

        if (presenter != null) {
            presenter.cleanup();
        }
        
        presenter = new InAppMessagePresenter(application, currentConfig.getInAppDisplayMode());
        
        if (shared.inAppMessageInterceptor != null) {
            presenter.setInAppMessageInterceptor(shared.inAppMessageInterceptor);
        }
        
        presenter.setInterceptorTimeout(shared.interceptorTimeoutMs);

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
}
