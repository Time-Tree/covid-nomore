package com.reactlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DBUtil {

    private static DBUtil sharedInstance;
    private DBManager dbManager;

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
}