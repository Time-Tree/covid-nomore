package com.reactlibrary;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import com.facebook.react.modules.storage.ReactDatabaseSupplier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class NearbyModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private final ReactApplicationContext reactContext;
    private NearbyService nearbyService;
    private boolean mBound;

    public NearbyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        final Intent nearbyServiceIntent = new Intent(reactContext, NearbyService.class);
        reactContext.startService(nearbyServiceIntent);
        reactContext.bindService(nearbyServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public String getName() {
        return "NearbyModule";
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.i(getName(), "onServiceConnected");
        NearbyService.NearbyBinder b = (NearbyService.NearbyBinder) binder;
        nearbyService = b.getService();
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(getName(), "onServiceDisconnected");
        nearbyService = null;
        mBound = false;
    }

    @ReactMethod
    public void getEvents(Promise promise) {
        try {
            if (mBound) {
                ArrayList<JSONObject> events = nearbyService.getEvents();
                WritableArray reponse = new WritableNativeArray();
                for (JSONObject event : events) {
                    WritableMap wm = convertJsonToMap(event);
                    reponse.pushMap(wm);
                }
                promise.resolve(reponse);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getStatus(Promise promise) {
        if (mBound) {
            Boolean isSubscribing = nearbyService.isSubscribing();
            Boolean isConnected = nearbyService.isConnected();
            if (isConnected == false) {
                nearbyService.checkAndConnect();
            }
            WritableMap map = Arguments.createMap();
            map.putBoolean("isSubscribing", isSubscribing);
            map.putBoolean("isConnected", isConnected);
            promise.resolve(map);
        }
    }

    @ReactMethod
    public void toggleState(Promise promise) {
        if (mBound) {


            try {
                SQLiteDatabase writableDatabase = ReactDatabaseSupplier.getInstance(this.reactContext).getWritableDatabase();
                String json = getDB();

                JSONObject value = new JSONObject(json);
                JSONObject eventsObj = new JSONObject(value.getString("events"));
                JSONArray eventsArray = eventsObj.getJSONArray("events");
                JSONObject newEvent = new JSONObject("{\"event\":\"TEST\"}");
                eventsArray.put(newEvent);
                eventsObj.put("events", eventsArray);
                value.put("events", eventsObj);
                String addToDbValue = value.toString();

                ContentValues insertValues = new ContentValues();
                insertValues.put("value", addToDbValue);
                writableDatabase.insert("catalystLocalStorage", null, insertValues);

                writableDatabase.close();

            } catch (Exception e) {
                // do something
            }
            String events = getDB();
            promise.resolve(events);
        }

    }

    private String getDB() {
            Cursor catalystLocalStorage = null;
            SQLiteDatabase readableDatabase = null;
            String response = null;

            try {
                readableDatabase = ReactDatabaseSupplier.getInstance(this.reactContext).getReadableDatabase();
                catalystLocalStorage = readableDatabase.query("catalystLocalStorage", new String[] { "key", "value" },
                        null, null, null, null, null);
                if (catalystLocalStorage.moveToFirst()) {
                    do {
                        try {
                            response = catalystLocalStorage.getString(catalystLocalStorage.getColumnIndex("value"));
                        } catch (Exception e) {
                            // do something
                        }
                    } while (catalystLocalStorage.moveToNext());
                }
            } finally {
                if (catalystLocalStorage != null) {
                    catalystLocalStorage.close();
                }

                if (readableDatabase != null) {
                    readableDatabase.close();
                }

            }

        return response;
    }

    private static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }
}
