package com.reactlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;


public class NearbyModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private final ReactApplicationContext reactContext;
    private NearbyService nearbyService;

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
        Log.d(getName(), "onServiceConnected");
        NearbyService.NearbyBinder b = (NearbyService.NearbyBinder) binder;
        nearbyService = b.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(getName(), "onServiceDisconnected");
        nearbyService = null;
    }

//     @ReactMethod
//     public void getMessages(Promise promise) {
//         promise.resolve(nearbyService.getMessages());
//     }
}
