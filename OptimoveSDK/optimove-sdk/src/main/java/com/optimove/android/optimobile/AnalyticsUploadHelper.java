package com.optimove.android.optimobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import androidx.annotation.WorkerThread;

import com.optimove.android.AuthJwtResolver;
import com.optimove.android.AuthManager;
import com.optimove.android.Optimove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

class AnalyticsUploadHelper {
    private static final String TAG = AnalyticsUploadHelper.class.getName();

    enum Result {
        SUCCESS,
        FAILED_RETRY_LATER,
        FAILED_NO_RETRY
    }

    private static final class AnalyticsEventRow {
        final long id;
        final JSONObject event;

        AnalyticsEventRow(long id, JSONObject event) {
            this.id = id;
            this.event = event;
        }
    }

    @WorkerThread
    Result flushEvents(Context context) {
        try (SQLiteOpenHelper dbHelper = new AnalyticsDbHelper(context)) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            long cursor = 0L;
            while (true) {
                Pair<List<AnalyticsEventRow>, Long> batch = this.getBatchOfEvents(db, cursor);
                List<AnalyticsEventRow> rows = batch.first;
                if (rows.isEmpty()) {
                    break;
                }
                if (!this.flushBatchToNetwork(context, rows)) {
                    return Result.FAILED_RETRY_LATER;
                }
                cursor = batch.second;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            return Result.FAILED_RETRY_LATER;
        } catch (Optimobile.PartialInitialisationException e) {
            return Result.FAILED_NO_RETRY;
        }

        return Result.SUCCESS;
    }

    private boolean flushBatchToNetwork(Context context, List<AnalyticsEventRow> rows) throws Optimobile.PartialInitialisationException {
        Map<String, List<AnalyticsEventRow>> groups = new LinkedHashMap<>();
        for (AnalyticsEventRow row : rows) {
            String key = analyticsUserKey(row.event);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        AuthManager authManager = Optimove.getAuthManager();

        final OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();

        final String url = Optimobile.urlForService(UrlBuilder.Service.EVENTS, "/v1/app-installs/" + Optimobile.getInstallId() + "/events");

        final String installId = Optimobile.getInstallId();

        for (List<AnalyticsEventRow> group : groups.values()) {
            JSONArray data = new JSONArray();
            for (AnalyticsEventRow r : group) {
                data.put(r.event);
            }
            String jwt = null;
            String uidKey = group.isEmpty() ? "" : analyticsUserKey(group.get(0).event);
            boolean visitorBatch = uidKey.isEmpty() || installId.equals(uidKey);
            if (authManager != null && !visitorBatch) {
                jwt = AuthJwtResolver.blockingJwt(authManager, uidKey, 30_000L);
            }
            if (!visitorBatch
                    && AuthJwtResolver.isMissingRequiredJwt(authManager, uidKey, jwt)) {
                return false;
            }
            try (Response response = httpClient.postSync(url, data, jwt)) {
                if (!response.isSuccessful()) {
                    int code = response.code();
                    boolean authNotConfigured = authManager == null
                            && code == 401
                            && !visitorBatch;
                    if (authNotConfigured) {
                        List<Long> ids = new ArrayList<>(group.size());
                        for (AnalyticsEventRow r : group) {
                            ids.add(r.id);
                        }
                        deletePersistedEventsByIds(context, ids);
                        continue;
                    }
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            List<Long> ids = new ArrayList<>(group.size());
            for (AnalyticsEventRow r : group) {
                ids.add(r.id);
            }
            deletePersistedEventsByIds(context, ids);
        }
        return true;
    }

    private static String analyticsUserKey(JSONObject event) {
        try {
            if (!event.has("userId") || event.isNull("userId")) {
                return "";
            }
            String u = event.optString("userId", "");
            return u == null ? "" : u.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void deletePersistedEventsByIds(Context context, List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }
        try (SQLiteOpenHelper dbHelper = new AnalyticsDbHelper(context)) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            StringBuilder ph = new StringBuilder();
            String[] args = new String[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) {
                    ph.append(',');
                }
                ph.append('?');
                args[i] = String.valueOf(ids.get(i));
            }
            db.delete(
                    AnalyticsContract.AnalyticsEvent.TABLE_NAME,
                    AnalyticsContract.AnalyticsEvent.COL_ID + " IN (" + ph + ")",
                    args);

            Optimobile.log(TAG, "Deleted persistent analytics events: " + ids.size());
        } catch (SQLiteException e) {
            Optimobile.log(TAG, "Failed to delete persistent analytics events");
            e.printStackTrace();
        }
    }

    private Pair<List<AnalyticsEventRow>, Long> getBatchOfEvents(SQLiteDatabase db, long minEventId) {
        String[] projection = {
                AnalyticsContract.AnalyticsEvent.COL_ID,
                AnalyticsContract.AnalyticsEvent.COL_HAPPENED_AT_MILLIS,
                AnalyticsContract.AnalyticsEvent.COL_UUID,
                AnalyticsContract.AnalyticsEvent.COL_EVENT_TYPE,
                AnalyticsContract.AnalyticsEvent.COL_PROPERTIES,
                AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER
        };

        String sortBy = AnalyticsContract.AnalyticsEvent.COL_ID + " ASC";

        String selection = AnalyticsContract.AnalyticsEvent.COL_ID + " > ?";
        String[] params = new String[]{String.valueOf(minEventId)};

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

        List<AnalyticsEventRow> rows = new ArrayList<>();
        long maxEventId = minEventId;

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

                long rowId = cursor.getLong(cursor.getColumnIndex(AnalyticsContract.AnalyticsEvent.COL_ID));
                rows.add(new AnalyticsEventRow(rowId, event));
                maxEventId = Math.max(maxEventId, rowId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        return new Pair<>(rows, maxEventId);
    }

}
