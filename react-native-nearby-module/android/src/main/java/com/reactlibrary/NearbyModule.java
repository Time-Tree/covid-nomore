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
import com.facebook.react.bridge.WritableMap;

public class NearbyModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private final ReactApplicationContext reactContext;
    private NearbyService nearbyService;
    private NearbyManager nearbyManager;
    private boolean mBound;

    public NearbyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        nearbyManager = NearbyManager.getInstance(reactContext);
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
    public void getStatus(Promise promise) {
        if (mBound) {
            Boolean isSubscribing = nearbyManager.isSubscribing();
            Boolean isConnected = nearbyManager.isConnected();
            if (isConnected == false) {
                nearbyManager.checkAndConnect();
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
            nearbyManager.removeEvents();
            promise.resolve(true);
        }
    }

}
