package com.optimove.android.optistream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptistreamDbHelper extends SQLiteOpenHelper implements OptistreamPersistanceAdapter {
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


    private File dbFile;

    public OptistreamDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbFile = context.getDatabasePath(DATABASE_NAME);
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

    @Override
    public boolean insertEvent(String eventJson) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(OptistreamEntry.COLUMN_DATA, eventJson);
            contentValues.put(OptistreamEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
            db.insert(OptistreamEntry.TABLE_NAME, null, contentValues);

        } catch (Throwable e) {
            OptiLoggerStreamsContainer.error("An error occurred while inserting events - %s", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void removeEvents(String lastId) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();

            String deleteQuery = OptistreamEntry._ID + " <= " + lastId;

            db.delete(OptistreamEntry.TABLE_NAME, deleteQuery, null);
        } catch (SQLiteException e) {
            OptiLoggerStreamsContainer.error("An SQL error occurred while removing events - %s, deleting the whole DB",
                    e.getMessage());
            close();
            dbFile.delete();
        } catch (Throwable e) {
            OptiLoggerStreamsContainer.error("An error occurred while removing events - %s, deleting the whole DB", e.getMessage());
            close();
            dbFile.delete();
        }
    }

    @Override
    public void removeEventsByIds(@NonNull List<Long> rowIds) {
        if (rowIds.isEmpty()) {
            return;
        }
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            StringBuilder placeholders = new StringBuilder();
            String[] args = new String[rowIds.size()];
            for (int i = 0; i < rowIds.size(); i++) {
                if (i > 0) {
                    placeholders.append(',');
                }
                placeholders.append('?');
                args[i] = String.valueOf(rowIds.get(i));
            }
            String query = OptistreamEntry._ID + " IN (" + placeholders + ")";
            db.delete(OptistreamEntry.TABLE_NAME, query, args);
        } catch (SQLiteException e) {
            OptiLoggerStreamsContainer.error("An SQL error occurred while removing events by ids - %s",
                    e.getMessage());
            close();
            dbFile.delete();
        } catch (Throwable e) {
            OptiLoggerStreamsContainer.error("An error occurred while removing events by ids - %s", e.getMessage());
            close();
            dbFile.delete();
        }
    }

    @Override
    public @Nullable EventsBulk getFirstEvents(int numberOfEvents) {

        try {
            List<QueuedEvent> queued = new ArrayList<>();

            SQLiteDatabase db = this.getReadableDatabase();
            String eventsQuery = "SELECT * FROM " + OptistreamEntry.TABLE_NAME +
                    " ORDER BY " + OptistreamEntry.COLUMN_CREATED_AT + " ASC LIMIT " + numberOfEvents;

            Cursor res = db.rawQuery(eventsQuery, null);

            boolean exists = res.moveToFirst();

            while (exists) {
                long id = res.getLong(res.getColumnIndexOrThrow(OptistreamEntry._ID));
                String data = res.getString(res.getColumnIndexOrThrow(OptistreamEntry.COLUMN_DATA));
                queued.add(new QueuedEvent(id, data));
                exists = res.moveToNext();
            }

            res.close();
            return new EventsBulk(queued);
        } catch (Exception e) {
            OptiLoggerStreamsContainer.error("An error occurred while querying events - %s",
                    e.getMessage());
            return null;
        }

    }
}
