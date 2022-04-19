package com.optimove.sdk.optimove_sdk.optimobile;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import  com.optimove.sdk.optimove_sdk.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class InAppMessageService {
    private static final String TAG = InAppMessageService.class.getName();
    private static final String PRESENTED_WHEN_IMMEDIATELY = "immediately";
    private static final String PRESENTED_WHEN_NEXT_OPEN = "next-open";

    private static final List<Integer> pendingTickleIds = new ArrayList<>();

    static void clearAllMessages(Context context) {
        Runnable task = new InAppContract.ClearDbRunnable(context);
        Optimobile.executorService.submit(task);
    }

    static boolean fetch(Context context, boolean includeNextOpen) {
        SharedPreferences preferences = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        long millis = preferences.getLong(SharedPrefs.IN_APP_LAST_SYNC_TIME, 0L);
        Date lastSyncTime = millis == 0 ? null : new Date(millis);

        List<InAppMessage> inAppMessages = InAppRequestService.readInAppMessages(context, lastSyncTime);
        if (inAppMessages == null) {
            return false;
        }

        showFetchedMessages(context, inAppMessages, includeNextOpen);
        return true;
    }

    private static void showFetchedMessages(Context context, List<InAppMessage> inAppMessages, boolean includeNextOpen) {
        if (inAppMessages.isEmpty()) {
            return;
        }

        for (InAppMessage message : inAppMessages) {
            if (message.getDismissedAt() != null || message.getInboxDeletedAt() != null) {
                clearNotification(context, message.getInAppId());
            }
        }

        Callable<InAppSaveResult> task = new InAppContract.SaveInAppMessagesCallable(context, inAppMessages);

        List<InAppMessage> unreadMessages;
        List<Integer> deliveredIds;
        List<Integer> deletedIds;
        boolean inboxUpdated = false;
        try {
            InAppSaveResult result = task.call();
            unreadMessages = result.getItemsToPresent();
            deliveredIds = result.getDeliveredIds();
            deletedIds = result.getDeletedIds();
            inboxUpdated = result.wasInboxUpdated();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (int inAppId : deletedIds) {
            clearNotification(context, inAppId);
        }

        OptimoveInApp.maybeRunInboxUpdatedHandler(inboxUpdated);

        InAppMessageService.storeLastSyncTime(context, inAppMessages);

        trackDeliveredEvents(context, deliveredIds);

        List<InAppMessage> itemsToPresent = new ArrayList<>();
        for (InAppMessage message : unreadMessages) {
            boolean hasPendingTickleId = false;
            for (Integer pendingTickleId : pendingTickleIds) {
                if (message.getInAppId() == pendingTickleId) {
                    hasPendingTickleId = true;
                    break;
                }
            }
            if (message.getPresentedWhen().equals(PRESENTED_WHEN_IMMEDIATELY)
                    || (includeNextOpen && message.getPresentedWhen().equals(PRESENTED_WHEN_NEXT_OPEN))
                    || hasPendingTickleId) {
                itemsToPresent.add(message);
            }
        }

        OptimoveInApp.presenter.presentMessages(itemsToPresent, new ArrayList<>(pendingTickleIds));

        pendingTickleIds.clear();
    }

    private static void trackDeliveredEvents(Context context, List<Integer> deliveredIds) {

        JSONObject params = new JSONObject();

        for (Integer deliveredId : deliveredIds) {
            try {
                params.put("type", AnalyticsContract.MESSAGE_TYPE_IN_APP);
                params.put("id", deliveredId);

                Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_DELIVERED, params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    static void readAndPresentMessages(Context context, boolean fromBackground, Integer tickleId) {
        Runnable task = new ReadAndPresentMessagesRunnable(context, fromBackground, tickleId);
        Optimobile.executorService.submit(task);
    }

    private static void maybeDoExtraFetch(Context context, boolean fromBackground) {
        boolean shouldFetch = false;
        if (BuildConfig.DEBUG) {
            shouldFetch = true;
        } else {
            SharedPreferences preferences = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
            long lastSyncMillis = preferences.getLong(SharedPrefs.IN_APP_LAST_SYNC_TIME, 0L);

            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            if (lastSyncMillis != 0L && lastSyncMillis + 3600 * 1000 < now.getTimeInMillis()) {
                shouldFetch = true;
            }
        }

        if (shouldFetch) {
            Optimobile.executorService.submit(() -> {
                InAppMessageService.fetch(context, fromBackground);
            });
        }
    }

    static void handleMessageClosed(@NonNull Context context, @NonNull InAppMessage message) {
        updateDismissedAt(context, message);
        trackDismissedEvent(context, message.getInAppId());
        clearNotification(context, message.getInAppId());
    }

    private static void updateDismissedAt(Context context, InAppMessage message) {
        message.setDismissedAt(new Date());
        Runnable task = new InAppContract.TrackMessageDismissedRunnable(context, message);
        Optimobile.executorService.submit(task);
    }

    private static void trackDismissedEvent(Context context, int id) {
        JSONObject params = new JSONObject();
        try {
            params.put("type", AnalyticsContract.MESSAGE_TYPE_IN_APP);
            params.put("id", id);

            Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_DISMISSED, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleMessageOpened(@NonNull Context context, @NonNull InAppMessage message) {
        int id = message.getInAppId();

        boolean markedRead = false;
        if (message.getReadAt() == null) {
            markedRead = markInboxItemRead(context, id, false);
        }
        if (message.getInbox() != null) {
            OptimoveInApp.maybeRunInboxUpdatedHandler(markedRead);
        }

        JSONObject params = new JSONObject();
        try {
            params.put("type", AnalyticsContract.MESSAGE_TYPE_IN_APP);
            params.put("id", id);

            Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_OPENED, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void clearNotification(Context context, int inAppId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PushBroadcastReceiver.OPTIMOBILE_NOTIFICATION_TAG, inAppId);
    }

    private static void storeLastSyncTime(Context context, List<InAppMessage> inAppMessages) {
        Date maxUpdatedAt = inAppMessages.get(0).getUpdatedAt();

        for (int i = 1; i < inAppMessages.size(); i++) {
            Date messageUpdatedAt = inAppMessages.get(i).getUpdatedAt();
            if (messageUpdatedAt.after(maxUpdatedAt)) {
                maxUpdatedAt = messageUpdatedAt;
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SharedPrefs.IN_APP_LAST_SYNC_TIME, maxUpdatedAt.getTime());
        editor.apply();
    }

    static List<InAppInboxItem> readInboxItems(Context context) {
        Callable<List<InAppInboxItem>> task = new InAppContract.ReadInAppInboxCallable(context);
        final Future<List<InAppInboxItem>> future = Optimobile.executorService.submit(task);

        List<InAppInboxItem> inboxItems;
        try {
            inboxItems = future.get();
        } catch (InterruptedException | ExecutionException ex) {

            return new ArrayList<>();
        }

        return inboxItems;
    }

    static OptimoveInApp.InboxMessagePresentationResult presentMessage(Context context, InAppInboxItem item) {
        Callable<InAppMessage> task = new InAppContract.ReadInAppInboxMessageCallable(context, item.getId());
        final Future<InAppMessage> future = Optimobile.executorService.submit(task);

        InAppMessage inboxMessage;
        try {
            inboxMessage = future.get();
        } catch (InterruptedException | ExecutionException ex) {
            return OptimoveInApp.InboxMessagePresentationResult.FAILED;
        }

        if (inboxMessage == null) {
            return OptimoveInApp.InboxMessagePresentationResult.FAILED;
        }

        if (item.getAvailableTo() != null && item.getAvailableTo().getTime() < new Date().getTime()) {
            return OptimoveInApp.InboxMessagePresentationResult.FAILED_EXPIRED;
        }

        List<InAppMessage> itemsToPresent = new ArrayList<>();
        itemsToPresent.add(inboxMessage);

        OptimoveInApp.presenter.presentMessages(itemsToPresent, null);

        return OptimoveInApp.InboxMessagePresentationResult.PRESENTED;
    }

    static boolean deleteMessageFromInbox(Context context, int id) {
        JSONObject params = new JSONObject();
        try {
            params.put("type", AnalyticsContract.MESSAGE_TYPE_IN_APP);
            params.put("id", id);

            Optimobile.trackEvent(context, AnalyticsContract.MESSAGE_DELETED_FROM_INBOX, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        clearNotification(context, id);

        Callable<Boolean> task = new InAppContract.DeleteInAppInboxMessageCallable(context, id);
        final Future<Boolean> future = Optimobile.executorService.submit(task);

        Boolean result = false;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Optimobile.log(TAG, ex.getMessage());
        }

        OptimoveInApp.maybeRunInboxUpdatedHandler(result);

        return result;
    }

    static boolean markInboxItemRead(Context context, int id, boolean shouldWaitForResult) {
        Callable<Boolean> task = new InAppContract.MarkInAppInboxMessageAsReadCallable(context, id);
        final Future<Boolean> future = Optimobile.executorService.submit(task);

        Boolean result = true;
        if (shouldWaitForResult) {
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Optimobile.log(TAG, ex.getMessage());
            }
        }

        if (!result) {
            return result;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("type", AnalyticsContract.MESSAGE_TYPE_IN_APP);
            params.put("id", id);

            Optimobile.trackEvent(context, AnalyticsContract.EVENT_TYPE_MESSAGE_READ, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        clearNotification(context, id);

        return result;
    }

    static boolean markAllInboxItemsAsRead(Context context) {
        List<InAppInboxItem> inboxItems = readInboxItems(context);
        boolean result = true;
        boolean inboxNeedsUpdate = false;
        for (InAppInboxItem item : inboxItems) {
            if (item.isRead()) {
                continue;
            }

            boolean success = markInboxItemRead(context, item.getId(), true);
            if (success && !inboxNeedsUpdate) {
                inboxNeedsUpdate = true;
            }

            if (!success) {
                result = false;
            }
        }

        OptimoveInApp.maybeRunInboxUpdatedHandler(inboxNeedsUpdate);

        return result;
    }

    private static class ReadAndPresentMessagesRunnable implements Runnable {
        private static final String TAG = ReadAndPresentMessagesRunnable.class.getName();

        private final Context mContext;
        private final boolean fromBackground;
        private final Integer tickleId;

        ReadAndPresentMessagesRunnable(Context context, boolean fromBackground, @Nullable Integer tickleId) {
            mContext = context.getApplicationContext();
            this.fromBackground = fromBackground;
            this.tickleId = tickleId;
        }

        @Override
        public void run() {
            List<InAppMessage> unreadMessages = getMessagesToPresent();

            List<InAppMessage> itemsToPresent = new ArrayList<>();
            for (InAppMessage message : unreadMessages) {
                if (message.getPresentedWhen().equals(PRESENTED_WHEN_IMMEDIATELY)
                        || (fromBackground && message.getPresentedWhen().equals(PRESENTED_WHEN_NEXT_OPEN))
                        || Integer.valueOf(message.getInAppId()).equals(tickleId)) {
                    itemsToPresent.add(message);
                }
            }

            List<Integer> tickleIds = new ArrayList<>();
            if (tickleId != null) {
                boolean tickleMessageFound = false;
                for (InAppMessage message : itemsToPresent) {
                    if (message.getInAppId() == tickleId) {
                        tickleMessageFound = true;
                        break;
                    }
                }

                if (!tickleMessageFound) {
                    pendingTickleIds.add(tickleId);
                } else {
                    tickleIds.add(tickleId);
                }
            }

            OptimoveInApp.presenter.presentMessages(itemsToPresent, tickleIds);

            // TODO potential bug? logic in here doesn't take into account the pending tickles
            //      in prod builds if synced < 1hr ago, may not sync again? (although assumed sync happens on app startup so...)
            // Sync is also triggered from push receiver when a tickle arrives, so assume this is fine?
            maybeDoExtraFetch(mContext, fromBackground);
        }

        private List<InAppMessage> getMessagesToPresent() {
            List<InAppMessage> itemsToPresent = new ArrayList<>();
            try (SQLiteOpenHelper dbHelper = new InAppDbHelper(mContext)) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                String[] projection = {
                        InAppContract.InAppMessageTable.COL_ID,
                        InAppContract.InAppMessageTable.COL_PRESENTED_WHEN,
                        InAppContract.InAppMessageTable.COL_CONTENT_JSON,
                        InAppContract.InAppMessageTable.COL_DATA_JSON,
                        InAppContract.InAppMessageTable.COL_READ_AT,
                        InAppContract.InAppMessageTable.COL_INBOX_CONFIG_JSON
                };
                String selection = String.format("%s IS NULL AND (%s IS NULL OR (DATETIME(%s) > DATETIME('now')))",
                        InAppContract.InAppMessageTable.COL_DISMISSED_AT,
                        InAppContract.InAppMessageTable.COL_EXPIRES_AT,
                        InAppContract.InAppMessageTable.COL_EXPIRES_AT);

                String sortOrder = InAppContract.InAppMessageTable.COL_UPDATED_AT + " ASC";

                Cursor cursor = db.query(InAppContract.InAppMessageTable.TABLE_NAME, projection, selection, null, null, null, sortOrder);

                while (cursor.moveToNext()) {
                    int inAppId = cursor.getInt(cursor.getColumnIndexOrThrow(InAppContract.InAppMessageTable.COL_ID));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(InAppContract.InAppMessageTable.COL_CONTENT_JSON));
                    JSONObject data = InAppContract.getNullableJsonObject(cursor, InAppContract.InAppMessageTable.COL_DATA_JSON);
                    String presentedWhen = cursor.getString(cursor.getColumnIndexOrThrow(InAppContract.InAppMessageTable.COL_PRESENTED_WHEN));
                    Date readAt = InAppContract.getNullableDate(cursor, InAppContract.InAppMessageTable.COL_READ_AT);
                    JSONObject inbox = InAppContract.getNullableJsonObject(cursor, InAppContract.InAppMessageTable.COL_INBOX_CONFIG_JSON);

                    InAppMessage m = new InAppMessage(inAppId, presentedWhen, new JSONObject(content), data, inbox, readAt);
                    itemsToPresent.add(m);
                }
                cursor.close();
            } catch (SQLiteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Optimobile.log(TAG, e.getMessage());
            }

            return itemsToPresent;
        }
    }
}