package com.reactlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DBUtil {

    private static DBUtil sharedInstance;
    private DBManager dbManager;
    private String TAG = "DBUtil";

    public static synchronized DBUtil getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DBUtil(context);
        }
        return sharedInstance;
    }

    private DBUtil(Context context) {
        dbManager = new DBManager(context);
    }

    public synchronized long addEvent(String eventType, String message) {
        long newRowId = -1;
        String formatDate = getFormattedDate();
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, eventType);
            values.put(EventContract.EventEntry.COLUMN_NAME_MESSAGE, message);
            values.put(EventContract.EventEntry.COLUMN_NAME_FORMAT_DATE, formatDate);
            values.put(EventContract.EventEntry.COLUMN_NAME_TIMESTAMP, timestamp);
            newRowId = db.insert(EventContract.EventEntry.TABLE_NAME, null, values);
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        return newRowId;
    }

    private synchronized String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    public synchronized ContentValues getSettingsData() {
        ContentValues response = new ContentValues();
        try {
            SQLiteDatabase db = dbManager.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + SettingsContract.SettingsEntry.TABLE_NAME + " WHERE _ID = 1",
                    null);
            if (c.moveToFirst()) {
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_PROCESS, c.getInt(1));
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_INTEVAL, c.getInt(2));
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_BLE_DURATION, c.getInt(3));
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_PROCESS, c.getInt(4));
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_INTEVAL, c.getInt(5));
                response.put(SettingsContract.SettingsEntry.COLUMN_NAME_NEARBY_DURATION, c.getInt(6));
            }
            c.close();
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        return response;
    }

    public synchronized void removeEvents() {
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            db.delete(EventContract.EventEntry.TABLE_NAME, null, null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            // do something
        }
    }

    public synchronized long addToken(String token) {
        long newRowId = -1;
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TokenContract.TokenEntry.COLUMN_NAME_TOKEN, token);
            values.put(TokenContract.TokenEntry.COLUMN_NAME_CREATED, timestamp);
            values.put(TokenContract.TokenEntry.COLUMN_NAME_USED, 0);
            newRowId = db.insert(TokenContract.TokenEntry.TABLE_NAME, null, values);
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        Log.i(TAG, "newRowId: " + newRowId);
        return newRowId;
    }

    public synchronized long updateTokenUsed(String token) {
        int nRowsEffected = -1;
        Log.i(TAG, "updateTokenUsed with token: " + token);
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TokenContract.TokenEntry.COLUMN_NAME_USED, 1);
            nRowsEffected = db.update(TokenContract.TokenEntry.TABLE_NAME, values,
                    TokenContract.TokenEntry.COLUMN_NAME_TOKEN + " = ?", new String[] { token });
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        Log.i(TAG, "nRowsEffected: " + nRowsEffected);
        return nRowsEffected;
    }

    public synchronized ContentValues getLastToken() {
        ContentValues response = new ContentValues();
        try {
            SQLiteDatabase db = dbManager.getReadableDatabase();
            String query = "SELECT " + TokenContract.TokenEntry.COLUMN_NAME_TOKEN + ", "
                    + TokenContract.TokenEntry.COLUMN_NAME_CREATED + " FROM " + TokenContract.TokenEntry.TABLE_NAME;
            query += " ORDER BY " + TokenContract.TokenEntry.COLUMN_NAME_CREATED + " DESC LIMIT 1";
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                response.put(TokenContract.TokenEntry.COLUMN_NAME_TOKEN, c.getString(0));
                response.put(TokenContract.TokenEntry.COLUMN_NAME_CREATED, c.getLong(1));
            }
            c.close();
            db.close();
        } catch (Error e) {
            Log.e(TAG, "error = " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    public synchronized long addHandshake(ContentValues data) {
        Log.i(TAG, "addHandshake " + data);
        long newRowId = -1;
        String token = data.getAsString("token");
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HandshakeContract.HandshakeEntry.COLUMN_NAME_TOKEN, token);
            values.put(HandshakeContract.HandshakeEntry.COLUMN_NAME_DISCOVERED, timestamp);
            values.put(HandshakeContract.HandshakeEntry.COLUMN_NAME_RSSI, data.getAsString("rssi"));
            values.put(HandshakeContract.HandshakeEntry.COLUMN_NAME_DATA, data.getAsString("characteristicData"));
            int nRowsEffected = db.update(HandshakeContract.HandshakeEntry.TABLE_NAME, values,
                    HandshakeContract.HandshakeEntry.COLUMN_NAME_TOKEN + " = ?", new String[] { token });
            Log.i(TAG, "nRowsEffected = " + nRowsEffected);
            if (nRowsEffected == 0) {
                newRowId = db.insert(HandshakeContract.HandshakeEntry.TABLE_NAME, null, values);
            }
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        Log.i(TAG, "newRowId: " + newRowId);
        return newRowId;
    }

    public synchronized long updateCharacteristicData(String token) {
        int nRowsEffected = -1;
        Log.i(TAG, "updateCharacteristicData with token: " + token);
        try {
            SQLiteDatabase db = dbManager.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HandshakeContract.HandshakeEntry.COLUMN_NAME_DATA, 1);
            nRowsEffected = db.update(HandshakeContract.HandshakeEntry.TABLE_NAME, values,
                    HandshakeContract.HandshakeEntry.COLUMN_NAME_TOKEN + " = ?", new String[] { token });
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        Log.i(TAG, "nRowsEffected: " + nRowsEffected);
        return nRowsEffected;
    }
}