package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OptistreamDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Optistream.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OptistreamEntry.TABLE_NAME + " (" +
                    OptistreamEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    OptistreamEntry.COLUMN_DATA + " TEXT," +
                    OptistreamEntry.COLUMN_CREATED_AT + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + OptistreamEntry.TABLE_NAME;

    private static class OptistreamEntry implements BaseColumns {
        private static final String TABLE_NAME = "optistream_events";
        private static final String COLUMN_DATA = "data";
        private static final String COLUMN_CREATED_AT = "created_at";
    }

    public OptistreamDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    boolean insertEvents(JSONArray jsonArray) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject event = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(OptistreamEntry.COLUMN_DATA, event.toString());
                contentValues.put(OptistreamEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
                db.insert(OptistreamEntry.TABLE_NAME, null, contentValues);
            }
        } catch (Exception e){
            OptiLoggerStreamsContainer.error("An error occurred while inserting events - %s",e.getMessage());
            return false;
        }
        return true;
    }
    boolean insertEvent(String eventJson) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(OptistreamEntry.COLUMN_DATA, eventJson);
            contentValues.put(OptistreamEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
            db.insert(OptistreamEntry.TABLE_NAME, null, contentValues);

        } catch (Exception e){
            OptiLoggerStreamsContainer.error("An error occurred while inserting events - %s",e.getMessage());
            return false;
        }
        return true;
    }

    void removeEvents(String lastId) {
        final SQLiteDatabase db = this.getWritableDatabase();

        try {
                String deleteQuery = OptistreamEntry._ID + " <= " + lastId;

            db.delete(OptistreamEntry.TABLE_NAME, deleteQuery, null);
        } catch (final SQLiteException e) {
            OptiLoggerStreamsContainer.error("An SQL error occurred while removing events - %s, deleting the whole DB",
                    e.getMessage());

            db.execSQL(SQL_DELETE_ENTRIES);
        } catch (final Exception e) {
            OptiLoggerStreamsContainer.error("An error occurred while removing events - %s, deleting the whole DB", e.getMessage());
            db.execSQL(SQL_DELETE_ENTRIES);
        }
    }

    EventsBulk getFirstEvents(int numberOfEvents) {

        String lastId = null;
        List<String> eventJsons = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        String eventsQuery = "SELECT * FROM " + OptistreamEntry.TABLE_NAME +
                        " ORDER BY " + OptistreamEntry.COLUMN_CREATED_AT + " ASC LIMIT " + numberOfEvents;

        Cursor res = db.rawQuery(eventsQuery, null);
        res.moveToFirst();

        while (res.moveToNext()) {
            if (res.isLast()) {
                lastId = res.getString(res.getColumnIndex(OptistreamEntry._ID));
            }
            eventJsons.add(res.getString(res.getColumnIndex(OptistreamEntry.COLUMN_DATA)));
        }
        return new EventsBulk(lastId, eventJsons);
    }

    class EventsBulk {

        private String lastId;
        private List<String> eventJsons;

        EventsBulk(String lastId, List<String> eventJsons) {
            this.lastId = lastId;
            this.eventJsons = eventJsons;
        }

        String getLastId() {
            return lastId;
        }

        void setLastId(String lastId) {
            this.lastId = lastId;
        }

        List<String> getEventJsons() {
            return eventJsons;
        }

        void setEventJsons(List<String> eventJsons) {
            this.eventJsons = eventJsons;
        }
    }
}