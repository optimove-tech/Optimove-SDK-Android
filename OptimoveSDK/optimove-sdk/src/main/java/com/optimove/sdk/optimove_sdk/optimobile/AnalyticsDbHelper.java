package com.optimove.sdk.optimove_sdk.optimobile;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** package */ class AnalyticsDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "k_analytics.db";
    private static final int DB_VERSION = 2;

    private static final String SQL_CREATE_EVENTS
            = "CREATE TABLE " + AnalyticsContract.AnalyticsEvent.TABLE_NAME + "("
            + AnalyticsContract.AnalyticsEvent.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AnalyticsContract.AnalyticsEvent.COL_HAPPENED_AT_MILLIS + " INTEGER NOT NULL,"
            + AnalyticsContract.AnalyticsEvent.COL_EVENT_TYPE + " TEXT NOT NULL,"
            + AnalyticsContract.AnalyticsEvent.COL_UUID + " TEXT UNIQUE NOT NULL,"
            + AnalyticsContract.AnalyticsEvent.COL_PROPERTIES + " TEXT )";

    AnalyticsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_EVENTS);
            onUpgrade(db, 1, DB_VERSION);
        }
        catch (SQLException e) {
            Optimobile.log("Failed to create analytics events table");
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // See: https://stackoverflow.com/a/26916986
        switch (oldVersion) {
            case 1:
                db.execSQL(String.format("ALTER TABLE %s ADD COLUMN %s TEXT DEFAULT NULL",
                        AnalyticsContract.AnalyticsEvent.TABLE_NAME, AnalyticsContract.AnalyticsEvent.COL_USER_IDENTIFIER));
                // nobreak: fallthrough for future version upgrades
        }
    }
}
