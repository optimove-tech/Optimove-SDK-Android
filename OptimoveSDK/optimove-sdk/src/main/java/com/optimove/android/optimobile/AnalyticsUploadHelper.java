package com.optimove.android.optimobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

class AnalyticsUploadHelper {
    private static final String TAG = AnalyticsUploadHelper.class.getName();

    enum Result {
        SUCCESS,
        FAILED_RETRY_LATER,
        FAILED_NO_RETRY
    }

    /**
     * package
     */
    @WorkerThread
    Result flushEvents(Context context) {
        try (SQLiteOpenHelper dbHelper = new AnalyticsDbHelper(context)) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Pair<ArrayList<JSONObject>, Long> eventsResult = this.getBatchOfEvents(db, 0L);
            ArrayList<JSONObject> events = eventsResult.first;
            long maxEventId = eventsResult.second;

            while (!events.isEmpty()) {
                if (!this.flushBatchToNetwork(context, events, maxEventId)) {
                    return Result.FAILED_RETRY_LATER;
                }

                eventsResult = this.getBatchOfEvents(db, maxEventId);
                events = eventsResult.first;
                maxEventId = eventsResult.second;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            return Result.FAILED_RETRY_LATER;
        } catch (Optimobile.PartialInitialisationException e) {
            return Result.FAILED_NO_RETRY;
        }

        return Result.SUCCESS;
    }

    private boolean flushBatchToNetwork(Context context, ArrayList<JSONObject> events, long maxEventId) throws Optimobile.PartialInitialisationException {
        JSONArray data = new JSONArray(events);

        final OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();

        final String url = Optimobile.urlForService(UrlBuilder.Service.EVENTS, "/v1/app-installs/" + Optimobile.getInstallId() + "/events");

        String batchUserId = extractUserIdFromBatch(events);
        boolean isVisitorBatch = batchUserId != null && batchUserId.equals(Optimobile.getInstallId());
        String authUserId = isVisitorBatch ? null : batchUserId;

        boolean result = false;
        try (Response response = httpClient.postSync(url, data, authUserId)) {
            if (response.isSuccessful()) {
                result = true;
            } else if (response.code() == 401) {
                deletePersistedEvents(context, maxEventId);
                return true;
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Auth token fetch failed")) {
                deletePersistedEvents(context, maxEventId);
                return true;
            }
            e.printStackTrace();
        }

        if (result) {
            deletePersistedEvents(context, maxEventId);
        }

        return result;
    }

    private static @Nullable String extractUserIdFromBatch(ArrayList<JSONObject> events) {
        if (events.isEmpty()) return null;
        return events.get(0).optString("userId", null);
    }

    private void deletePersistedEvents(Context context, long maxEventId){
        try (SQLiteOpenHelper dbHelper = new AnalyticsDbHelper(context)) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.delete(
                    AnalyticsContract.AnalyticsEvent.TABLE_NAME,
                    AnalyticsContract.AnalyticsEvent.COL_ID + " <= ?",
                    new String[]{String.valueOf(maxEventId)});

            Optimobile.log(TAG, "Deleted persistent events up to " + maxEventId + " (inclusive)");
        } catch (SQLiteException e) {
            Optimobile.log(TAG, "Failed to delete persistent events up to " + maxEventId + " (inclusive)");
            e.printStackTrace();
        }
    }

    private Pair<ArrayList<JSONObject>, Long> getBatchOfEvents(SQLiteDatabase db, long minEventId) {
        String sortBy = AnalyticsContract.AnalyticsEvent.COL_ID + " ASC";

        // Peek at the oldest event to determine which user_identifier to batch
        String peekSelection = AnalyticsContract.AnalyticsEvent.COL_ID + " > ?";
        String[] peekParams = new String[]{String.valueOf(minEventId)};

        Cursor peekCursor = db.query(
                AnalyticsContract.AnalyticsEvent.TABLE_NAME,
                new String[]{AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER},
                peekSelection, peekParams, null, null, sortBy, "1");

        String batchUserIdentifier = null;
        boolean hasPeek = false;
        if (peekCursor.moveToFirst()) {
            hasPeek = true;
            int idx = peekCursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER);
            batchUserIdentifier = peekCursor.isNull(idx) ? null : peekCursor.getString(idx);
        }
        peekCursor.close();

        if (!hasPeek) {
            return new Pair<>(new ArrayList<>(), -1L);
        }

        // Fetch up to 100 events with the same user_identifier
        String selection;
        String[] params;
        if (batchUserIdentifier != null) {
            selection = AnalyticsContract.AnalyticsEvent.COL_ID + " > ? AND "
                    + AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER + " = ?";
            params = new String[]{String.valueOf(minEventId), batchUserIdentifier};
        } else {
            selection = AnalyticsContract.AnalyticsEvent.COL_ID + " > ? AND "
                    + AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER + " IS NULL";
            params = new String[]{String.valueOf(minEventId)};
        }

        String[] projection = {
                AnalyticsContract.AnalyticsEvent.COL_ID,
                AnalyticsContract.AnalyticsEvent.COL_HAPPENED_AT_MILLIS,
                AnalyticsContract.AnalyticsEvent.COL_UUID,
                AnalyticsContract.AnalyticsEvent.COL_EVENT_TYPE,
                AnalyticsContract.AnalyticsEvent.COL_PROPERTIES,
                AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER
        };

        Cursor cursor = db.query(
                AnalyticsContract.AnalyticsEvent.TABLE_NAME,
                projection,
                selection,
                params,
                null,
                null,
                sortBy,
                String.valueOf(100)
        );

        ArrayList<JSONObject> events = new ArrayList<>();
        long maxEventId = -1L;

        while (cursor.moveToNext()) {
            JSONObject event = new JSONObject();

            try {
                event.put("type", cursor.getString(cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_EVENT_TYPE)));
                event.put("uuid", cursor.getString(cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_UUID)));
                event.put("timestamp", cursor.getLong(cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_HAPPENED_AT_MILLIS)));

                int propsIdx = cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_PROPERTIES);
                if (!cursor.isNull(propsIdx)) {
                    String eventPropsStr = cursor.getString(propsIdx);
                    event.put("data", new JSONObject(eventPropsStr));
                }

                String userId = null;
                int userIdIdx = cursor.getColumnIndex(
                        AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER);

                if (!cursor.isNull(userIdIdx)) {
                    userId = cursor.getString(userIdIdx);
                }

                event.put("userId", userId);

                events.add(event);

                maxEventId = cursor.getLong(cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_ID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        return new Pair<>(events, maxEventId);
    }

}
