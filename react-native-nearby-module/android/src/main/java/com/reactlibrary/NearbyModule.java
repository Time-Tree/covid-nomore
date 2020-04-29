package com.reactlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
