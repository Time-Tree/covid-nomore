package com.reactlibrary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NearbyService extends Service {

    static String TAG = "Service";
    static String NOTIFICATION_CHANNEL = "CovidNoMore";
    static Integer NOTIFICATION_CHANNEL_ID = 123456;
    static NotificationManagerCompat notificationManager;
    public Integer code = 0;
    private NearbySql dbHelper;
    private BLEScanner mBLEScanner = null;
    private BLEAdvertiser mBLEAdvertiser = null;
    private NearbyManager nearbyManager = null;
    private Timer nearbyStartTimer = null;
    private Timer bleStartTimer = null;
    private HandlerThread nearbyThread = new HandlerThread("nearby-background-thread");
    private HandlerThread bleThread = new HandlerThread("ble-background-thread");

    private final IBinder myBinder = new NearbyBinder();

    public class NearbyBinder extends Binder {
        NearbyService getService() {
            return NearbyService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate NEARBY SERVICE");
        super.onCreate();
        createBackgroundNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFICATION_CHANNEL_ID,
                buildForegroundNotification("CovidNoMore", "Background Service", true));
        Context context = this.getApplicationContext();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        dbHelper = new NearbySql(context);
        if (checkBluetooth()) {
            mBLEScanner = new BLEScanner(bluetoothAdapter, dbHelper, context);
            mBLEAdvertiser = new BLEAdvertiser(dbHelper, context);
        } else {
            Log.e(TAG, "Bluetooth init error");
        }
        nearbyManager = NearbyManager.getInstance(context);
        nearbyThread.start();
        bleThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimers();
        return START_STICKY;
    }

    private Boolean checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            boolean isBluetoothSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
            if (isBluetoothSupported) {
                return true;
            } else {
                Log.i(TAG, "Bluetooth not supported");
            }
        } else {
            Log.i(TAG, "Bluetooth not enabled");
        }
        return false;
    }

    public void startTimers() {
        if (nearbyStartTimer != null) {
            nearbyStartTimer.cancel();
            nearbyStartTimer.purge();
        }
        if (bleStartTimer != null) {
            bleStartTimer.cancel();
            bleStartTimer.purge();
        }
        nearbyStartTimer = new Timer();
        // nearbyStartTimer.schedule(startNearbyTimerTask, 1000, 5 * 60 * 1000);
        bleStartTimer = new Timer();
        bleStartTimer.schedule(startBLETimerTask, 3000, 5 * 60 * 1000);
    }

    TimerTask startNearbyTimerTask = new TimerTask() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            code = 1000 + new Random().nextInt(9000);
            Log.i(TAG, "New generated code = " + code);
            nearbyManager.checkAndConnect();
            nearbyManager.subscribe();
            nearbyManager.publish(code);
            createStopNearbyTimerTask();
        }
    };

    private void createStopNearbyTimerTask() {
        Log.i(TAG, "createStopNearbyTimerTask ???");
        new Handler(nearbyThread.getLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "createStopNearbyTimerTask !!!");
                nearbyManager.unsubscribe();
                nearbyManager.unpublish();
            }
        }, 3 * 60 * 1000);
    }

    TimerTask startBLETimerTask = new TimerTask() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            if (mBLEAdvertiser != null) {
                mBLEAdvertiser.startAdvertising();
            }
            if (mBLEScanner != null) {
                mBLEScanner.start();
            }
            createStopBLETimerTask();
        }
    };

    private void createStopBLETimerTask() {
        Log.i(TAG, "createStopBLETimerTask ???");
        new Handler(bleThread.getLooper()).postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                Log.i(TAG, "createStopBLETimerTask !!!");
                mBLEAdvertiser.stopAdvertising();
                mBLEScanner.stop();
                mBLEScanner.clearServicesCache();
            }
        }, 3 * 60 * 1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private Notification buildForegroundNotification(String title, String text, boolean nonRemovable) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);

        b.setOngoing(false).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(text)
                .setOngoing(nonRemovable);

        return (b.build());
    }

    private void createBackgroundNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.connectivity_channel_name);
            String description = getString(R.string.connectivity_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void removeEvents() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(NearbyEventContract.EventEntry.TABLE_NAME, null, null);
            db.close();
        } catch (Exception e) {
            // do something
        }
    }
}
