package com.reactlibrary;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NearbyService extends Service {

    static String TAG = "Service";
    static String NOTIFICATION_CHANNEL = "CovidNoMore";
    static Integer NOTIFICATION_CHANNEL_ID = 123456;
    static NotificationManagerCompat notificationManager;
    public Integer code = 0;
    private DBManager dbHelper;
    private BLEScanner mBLEScanner = null;
    private BLEAdvertiser mBLEAdvertiser = null;
    private NearbyManager nearbyManager = null;
    private static Timer nearbyStartTimer = null;
    private static Timer bleStartTimer = null;
    private HandlerThread nearbyThread = new HandlerThread("nearby-background-thread");
    private HandlerThread bleThread = new HandlerThread("ble-background-thread");
    private Handler nearbyStopHandler = null;
    private Handler bleStopHandler = null;

    private int BLE_INTERVAL = 3 * 60 * 1000; // minutes
    private int BLE_DURATION = 1 * 60 * 1000;
    private int NEARBY_INTERVAL = 3 * 60 * 1000;
    private int NEARBY_DURATION = 1 * 60 * 1000;

    private final IBinder myBinder = new NearbyBinder();

    public class NearbyBinder extends Binder {
        NearbyService getService() {
            return NearbyService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate NEARBY SERVICE");
        super.onCreate();
        createBackgroundNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFICATION_CHANNEL_ID,
                buildForegroundNotification("CovidNoMore", "Background Service", true));
        Context context = this.getApplicationContext();
        dbHelper = new DBManager(context);
        if (checkBluetooth()) {
            mBLEScanner = new BLEScanner(dbHelper);
            mBLEAdvertiser = new BLEAdvertiser(dbHelper, context);
        } else {
            Log.e(TAG, "Bluetooth init error");
        }
        nearbyManager = NearbyManager.getInstance(context);
        nearbyThread.start();
        nearbyStopHandler = new Handler(nearbyThread.getLooper());
        bleThread.start();
        bleStopHandler = new Handler(bleThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void setCurrentApplication(Application application) {
        mBLEScanner.setCurrentApplication(application);
        startTimers();
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
        Log.i(TAG, "startTimers");
        if (nearbyStartTimer != null) {
            nearbyStartTimer.cancel();
            nearbyStartTimer.purge();
        }
        if (bleStartTimer != null) {
            bleStartTimer.cancel();
            bleStartTimer.purge();
        }
        ContentValues settings = getSettingsData();
        parseSettingsData(settings);
        if (settings.getAsInteger("bleProcess") == 1) {
            startBLETimerTask();
        }
        if (settings.getAsInteger("nearbyProcess") == 1) {
            startNearbyTimerTask();
        }
    }

    public void restartService() {
        Log.i(TAG, "restartService");
        ContentValues settings = getSettingsData();
        parseSettingsData(settings);
        stopAllProcess();
        if (settings.getAsInteger("bleProcess") == 1) {
            startBLETimerTask();
        }
        if (settings.getAsInteger("nearbyProcess") == 1) {
            startNearbyTimerTask();
        }
    }

    private void stopAllProcess() {
        Log.i(TAG, "stopAllProcess");
        if (nearbyStartTimer != null) {
            nearbyStartTimer.cancel();
            nearbyStartTimer.purge();
            nearbyManager.unsubscribe();
            nearbyManager.unpublish();
        }
        if (bleStartTimer != null) {
            bleStartTimer.cancel();
            bleStartTimer.purge();
            mBLEAdvertiser.stopAdvertising();
            mBLEScanner.stopScanner();
        }
        nearbyStopHandler.removeCallbacksAndMessages(null);
        bleStopHandler.removeCallbacksAndMessages(null);
    }

    private void parseSettingsData(ContentValues settings) {
        BLE_INTERVAL = settings.getAsInteger("bleInterval") * 60 * 1000;
        BLE_DURATION = settings.getAsInteger("bleDuration") * 60 * 1000;
        NEARBY_INTERVAL = settings.getAsInteger("nearbyInterval") * 60 * 1000;
        NEARBY_DURATION = settings.getAsInteger("nearbyDuration") * 60 * 1000;
    }

    private void startNearbyTimerTask() {
        Log.i(TAG, "startNearbyTimerTask");
        nearbyStartTimer = new Timer();
        TimerTask startNearbyTimerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                code = 1000 + new Random().nextInt(9000);
                Log.i(TAG, "New generated code = " + code);
                nearbyManager.checkAndConnect();
                nearbyManager.subscribe();
                nearbyManager.publish(code);
                nearbyStopHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "stopNearbyTimerTask");
                        nearbyManager.unsubscribe();
                        nearbyManager.unpublish();
                    }
                }, NEARBY_DURATION);
            }
        };
        nearbyStartTimer.schedule(startNearbyTimerTask, 0, NEARBY_INTERVAL);
    }

    private void startBLETimerTask() {
        Log.i(TAG, "startBLETimerTask");
        bleStartTimer = new Timer();
        TimerTask startBLETimerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                if (mBLEAdvertiser != null) {
                    mBLEAdvertiser.startAdvertising();
                }
                if (mBLEScanner != null) {
                    mBLEScanner.startScanner();
                }
                bleStopHandler.postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        Log.i(TAG, "stopBLETimerTask");
                        mBLEAdvertiser.stopAdvertising();
                        mBLEScanner.stopScanner();
                    }
                }, BLE_DURATION);
            }
        };
        bleStartTimer.schedule(startBLETimerTask, 0, BLE_INTERVAL);
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
            db.delete(EventContract.EventEntry.TABLE_NAME, null, null);
            db.close();
        } catch (Exception e) {
            // do something
        }
    }

    private ContentValues getSettingsData() {
        ContentValues response = new ContentValues();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
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
}
