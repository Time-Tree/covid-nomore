package com.reactlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

final class EventContract {
    private EventContract() {
    }

    public static class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "NearbyEvents";
        public static final String COLUMN_NAME_EVENT_TYPE = "eventType";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_FORMAT_DATE = "formatDate";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}

final class SettingsContract {
    private SettingsContract() {
    }

    public static class SettingsEntry implements BaseColumns {
        public static final String TABLE_NAME = "Settings";
        public static final String COLUMN_NAME_BLE_PROCESS = "bleProcess";
        public static final String COLUMN_NAME_BLE_INTEVAL = "bleInterval";
        public static final String COLUMN_NAME_BLE_DURATION = "bleDuration";
        public static final String COLUMN_NAME_NEARBY_PROCESS = "nearbyProcess";
        public static final String COLUMN_NAME_NEARBY_INTEVAL = "nearbyInterval";
        public static final String COLUMN_NAME_NEARBY_DURATION = "nearbyDuration";
    }
}

final class TokenContract {
    private TokenContract() {
    }

    public static class TokenEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tokens";
        public static final String COLUMN_NAME_TOKEN = "token";
        public static final String COLUMN_NAME_CREATED = "created";
        public static final String COLUMN_NAME_USED = "used";
    }
}

final class HandshakeContract {
    private HandshakeContract() {
    }

    public static class HandshakeEntry implements BaseColumns {
        public static final String TABLE_NAME = "Handshakes";
        public static final String COLUMN_NAME_TOKEN = "token";
        public static final String COLUMN_NAME_DISCOVERED = "discovered";
        public static final String COLUMN_NAME_RSSI = "rssi";
        public static final String COLUMN_NAME_DATA = "characteristicData";
    }
}

public class DBManager extends SQLiteOpenHelper {

    private static final String SQL_CREATE_EVENTS = "CREATE TABLE " + EventContract.EventEntry.TABLE_NAME + " ("
            + EventContract.EventEntry._ID + " INTEGER PRIMARY KEY," + EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE
            + " TEXT," + EventContract.EventEntry.COLUMN_NAME_MESSAGE + " TEXT,"
            + EventContract.EventEntry.COLUMN_NAME_TIMESTAMP + " TEXT,"
            + EventContract.EventEntry.COLUMN_NAME_FORMAT_DATE + " TEXT)";

    private static final String SQL_CREATE_SETTINGS = "CREATE TABLE IF NOT EXISTS "
            + SettingsContract.SettingsEntry.TABLE_NAME + " (" + SettingsContract.SettingsEntry._ID
            + " INTEGER PRIMARY KEY," + SettingsContract.SettingsEntry.COLUMN_NAME_BLE_PROCESS + " INTEGER,"
            + SettingsContract.SettingsEntry.COLUMN_NAME_BLE_INTEVAL + " INTEGER,"
            + SettingsContract.SettingsEntry.COLUMN_NAME_BLE_DURATION + " INTEGER,"
            + SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_PROCESS + " INTEGER,"
            + SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_INTEVAL + " INTEGER,"
            + SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_DURATION + " INTEGER)";

    private static final String SQL_CREATE_TOKENS = "CREATE TABLE IF NOT EXISTS " + TokenContract.TokenEntry.TABLE_NAME
            + " (" + TokenContract.TokenEntry._ID + " INTEGER PRIMARY KEY," + TokenContract.TokenEntry.COLUMN_NAME_TOKEN
            + " TEXT," + TokenContract.TokenEntry.COLUMN_NAME_CREATED + " INTEGER,"
            + TokenContract.TokenEntry.COLUMN_NAME_USED + " INTEGER)";

    private static final String SQL_CREATE_HANDSHAKES = "CREATE TABLE IF NOT EXISTS "
            + HandshakeContract.HandshakeEntry.TABLE_NAME + " (" + HandshakeContract.HandshakeEntry._ID
            + " INTEGER PRIMARY KEY," + HandshakeContract.HandshakeEntry.COLUMN_NAME_TOKEN + " TEXT UNIQUE,"
            + HandshakeContract.HandshakeEntry.COLUMN_NAME_DISCOVERED + " INTEGER,"
            + HandshakeContract.HandshakeEntry.COLUMN_NAME_RSSI + " TEXT,"
            + HandshakeContract.HandshakeEntry.COLUMN_NAME_DATA + " INTEGER)";

    private static final String SQL_DELETE_EVENTS = "DROP TABLE IF EXISTS " + EventContract.EventEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NearbyEvents";

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        insertDefaultSettingsData();
        createExtraTables();
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS);
        db.execSQL(SQL_CREATE_TOKENS);
        db.execSQL(SQL_CREATE_HANDSHAKES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_EVENTS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void insertDefaultSettingsData() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(SQL_CREATE_SETTINGS);
            Cursor c = db.rawQuery("SELECT * FROM " + SettingsContract.SettingsEntry.TABLE_NAME, null);
            Boolean recordExist = false;
            if (c.moveToFirst()) {
                recordExist = true;
            }
            if (recordExist) {
                Log.i("NearbySQL", "recordExist");
            } else {
                ContentValues values = new ContentValues();
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_PROCESS, 1);
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_INTEVAL, 3);
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_DURATION, 1);
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_PROCESS, 1);
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_INTEVAL, 3);
                values.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_DURATION, 1);
                long newRowId = db.insert(SettingsContract.SettingsEntry.TABLE_NAME, null, values);
                Log.i("NearbySQL", "newRowId: " + newRowId);
            }
            c.close();
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    private void createExtraTables() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(SQL_CREATE_TOKENS);
            db.execSQL(SQL_CREATE_HANDSHAKES);
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

}