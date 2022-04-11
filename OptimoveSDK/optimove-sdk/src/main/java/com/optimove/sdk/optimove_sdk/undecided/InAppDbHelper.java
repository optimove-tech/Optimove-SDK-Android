package com.optimove.sdk.optimove_sdk.undecided;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class InAppDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "k_in_app.db";
    private static final int DB_VERSION = 4;

    private static final String SQL_CREATE_IN_APP_MESSAGES
            = "CREATE TABLE " + InAppContract.InAppMessageTable.TABLE_NAME + "("
            + InAppContract.InAppMessageTable.COL_ID + " INTEGER PRIMARY KEY, "
            + InAppContract.InAppMessageTable.COL_CONTENT_JSON + " TEXT NOT NULL,"
            + InAppContract.InAppMessageTable.COL_PRESENTED_WHEN + " TEXT NOT NULL,"
            + InAppContract.InAppMessageTable.COL_DATA_JSON + " TEXT,"
            + InAppContract.InAppMessageTable.COL_BADGE_CONFIG_JSON + " TEXT,"
            + InAppContract.InAppMessageTable.COL_INBOX_CONFIG_JSON + " TEXT,"
            + InAppContract.InAppMessageTable.COL_INBOX_FROM + " DATETIME,"
            + InAppContract.InAppMessageTable.COL_INBOX_TO + " DATETIME,"
            + InAppContract.InAppMessageTable.COL_DISMISSED_AT + " DATETIME,"
            + InAppContract.InAppMessageTable.COL_UPDATED_AT + " DATETIME NOT NULL,"
            + InAppContract.InAppMessageTable.COL_EXPIRES_AT + " DATETIME,"
            + InAppContract.InAppMessageTable.COL_READ_AT + " DATETIME,"
            + InAppContract.InAppMessageTable.COL_SENT_AT + " DATETIME)";

    InAppDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_IN_APP_MESSAGES);
        } catch (SQLException e) {
            Kumulos.log("Failed to create in app table");
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (int i = oldVersion + 1; i <= newVersion; ++i) {
            switch (i) {
                case 2:
                    this.upgradeToVersion2(db);
                    break;
                case 3:
                    this.upgradeToVersion3(db);
                    break;
                case 4:
                    this.upgradeToVersion4(db);
                    break;
                default:
                    throw new IllegalStateException("onUpgrade() with unknown newVersion " + newVersion);
            }
        }

    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + InAppContract.InAppMessageTable.TABLE_NAME + " ADD COLUMN " + InAppContract.InAppMessageTable.COL_EXPIRES_AT + " DATETIME;");
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + InAppContract.InAppMessageTable.TABLE_NAME + " ADD COLUMN " + InAppContract.InAppMessageTable.COL_READ_AT + " DATETIME;");
        db.execSQL("ALTER TABLE " + InAppContract.InAppMessageTable.TABLE_NAME + " ADD COLUMN " + InAppContract.InAppMessageTable.COL_SENT_AT + " DATETIME;");
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        db.execSQL("UPDATE " + InAppContract.InAppMessageTable.TABLE_NAME +
                " SET " + InAppContract.InAppMessageTable.COL_SENT_AT + " = " + InAppContract.InAppMessageTable.COL_UPDATED_AT +
                " WHERE " + InAppContract.InAppMessageTable.COL_SENT_AT + " IS NULL ");
    }
}