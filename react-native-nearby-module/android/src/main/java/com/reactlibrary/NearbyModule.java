package com.reactlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import androidx.annotation.RequiresApi;

public class NearbyModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private final ReactApplicationContext reactContext;
    private NearbyService nearbyService;
//    private NearbyManager nearbyManager;
    private boolean mBound;
    private final String TAG = "NearbyModule";

    public NearbyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
//        nearbyManager = NearbyManager.getInstance(reactContext);
        final Intent nearbyServiceIntent = new Intent(reactContext, NearbyService.class);
        reactContext.startService(nearbyServiceIntent);
        reactContext.bindService(nearbyServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public String getName() {
        return "NearbyModule";
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.i(TAG, "onServiceConnected");
        NearbyService.NearbyBinder b = (NearbyService.NearbyBinder) binder;
        nearbyService = b.getService();
//        nearbyService.setCurrentApplication(getCurrentActivity().getApplication());
        nearbyService.setCurrentApplication(this.reactContext.getCurrentActivity().getApplication());
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "onServiceDisconnected");
        nearbyService = null;
        mBound = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void startService(Promise promise) {
        Log.i(TAG, "startService");
       String status = nearbyService.startAll();
       promise.resolve(status);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void stopService(Promise promise) {
        Log.i(TAG, "stopService");
        String status = nearbyService.stopAll();
        promise.resolve(status);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void restartService(Promise promise) {
        String status = nearbyService.restartService();
        promise.resolve(status);
    }

    @ReactMethod
    public void getStatus(Promise promise) {
        if (mBound) {
//            Boolean isSubscribing = nearbyManager.isSubscribing();
//            Boolean isConnected = nearbyManager.isConnected();
//            if (isConnected == false) {
//                nearbyManager.checkAndConnect();
//            }
            WritableMap map = Arguments.createMap();
            map.putBoolean("isSubscribing", false);
            map.putBoolean("isConnected", false);
            promise.resolve(map);
        }
    }

    @ReactMethod
    public void toggleState(Promise promise) {
        if (mBound) {
//            nearbyManager.removeEvents();
            promise.resolve(true);
        }
    }



}
