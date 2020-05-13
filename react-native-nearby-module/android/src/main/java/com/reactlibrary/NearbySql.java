package com.reactlibrary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

final class NearbyEventContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private NearbyEventContract() {}

    /* Inner class that defines the table contents */
    public static class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "NearbyEvents";
        public static final String COLUMN_NAME_EVENT_TYPE = "eventType";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_FORMAT_DATE = "formatDate";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}


public class NearbySql extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NearbyEventContract.EventEntry.TABLE_NAME + " (" +
                    NearbyEventContract.EventEntry._ID + " INTEGER PRIMARY KEY," +
                    NearbyEventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + " TEXT," +
                    NearbyEventContract.EventEntry.COLUMN_NAME_MESSAGE + " TEXT," +
                    NearbyEventContract.EventEntry.COLUMN_NAME_TIMESTAMP + " TEXT," +
                    NearbyEventContract.EventEntry.COLUMN_NAME_FORMAT_DATE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NearbyEventContract.EventEntry.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NearbyEvents";

    public NearbySql(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}