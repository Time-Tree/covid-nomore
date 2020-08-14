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

    static String TAG = "NearbyService";
    static String NOTIFICATION_CHANNEL = "CovidNoMore";
    static Integer NOTIFICATION_CHANNEL_ID = 123456;
    static NotificationManagerCompat notificationManager;
    public Integer code = 0;
    private DBUtil dbHelper;
    private BLEScanner mBLEScanner = null;
    private BeaconScanner mBeaconScanner = null;
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
        dbHelper = DBUtil.getInstance(context);
        mBLEScanner = new BLEScanner(context);
        mBeaconScanner = new BeaconScanner(context);
        mBLEAdvertiser = new BLEAdvertiser(context);

//        nearbyManager = NearbyManager.getInstance(context);
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
        mBeaconScanner.setCurrentApplication(application);
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

    public String startAll() {
        if (checkBluetooth()) {
            if (startTimers()) {
                return "SUCCESS";
            } else {
                return "ERROR";
            }
        } else {
            Log.e(TAG, "Bluetooth init error");
            return "BLE_ERROR";
        }
    }

    public Boolean startTimers() {
        Log.i(TAG, "startTimers");
        ContentValues settings = dbHelper.getSettingsData();
        parseSettingsData(settings);
        Integer nearbySettings = settings.getAsInteger("nearbyProcess");
        Integer bleSettings = settings.getAsInteger("bleProcess");
        if (nearbySettings == 0 && bleSettings == 0) {
            return true;
        }
        if (nearbyStartTimer != null) {
            nearbyStartTimer.cancel();
            nearbyStartTimer.purge();
        }
        if (bleStartTimer != null) {
            bleStartTimer.cancel();
            bleStartTimer.purge();
        }

        Boolean BLE_ON = false;
        Boolean NEARBY_ON = false;
        if (bleSettings == 1) {
            BLE_ON = startBLETimerTask();
            if (BLE_ON) {
                dbHelper.updateServiceStatus("BLE", "ON");
            }
        }
        if (nearbySettings == 1) {
            NEARBY_ON = startNearbyTimerTask();
            if (NEARBY_ON) {
                dbHelper.updateServiceStatus("NEARBY", "ON");
            }
        }
        if (BLE_ON || NEARBY_ON) {
            return true;
        } else {
            return false;
        }
    }

    public String stopAll() {
        Log.i(TAG, "stopAllProcess");
        try {
            if (nearbyStartTimer != null) {
                nearbyStartTimer.cancel();
                nearbyStartTimer.purge();
    //            nearbyManager.unsubscribe();
    //            nearbyManager.unpublish();
                mBeaconScanner.stopScanner();
            }
            if (bleStartTimer != null) {
                bleStartTimer.cancel();
                bleStartTimer.purge();
                mBLEAdvertiser.stopAdvertising();
                mBLEScanner.stopScanner();
            }
            nearbyStopHandler.removeCallbacksAndMessages(null);
            bleStopHandler.removeCallbacksAndMessages(null);
            dbHelper.updateServiceStatus("NEARBY", "OFF");
            dbHelper.updateServiceStatus("BLE", "OFF");
            return "SUCCESS";
        } catch (Exception e) {
            Log.e(TAG, "stopAllProcess error " + e);
            return "ERROR";
        }
    }


    public String restartService() {
        Log.i(TAG, "restartService");
        String stopStatus = stopAll();
        if (stopStatus.equals("SUCCESS")) {
            return startAll();
        } else {
            return "ERROR";
        }
    }

    private void parseSettingsData(ContentValues settings) {
        BLE_INTERVAL = settings.getAsInteger("bleInterval") * 60 * 1000;
        BLE_DURATION = settings.getAsInteger("bleDuration") * 60 * 1000;
        NEARBY_INTERVAL = settings.getAsInteger("nearbyInterval") * 60 * 1000;
        NEARBY_DURATION = settings.getAsInteger("nearbyDuration") * 60 * 1000;
        Log.i(TAG, "Starting with settings: BLE_INTERVAL " + BLE_INTERVAL + " BLE_DURATION " + BLE_DURATION + " NEARBY_INTERVAL " + NEARBY_INTERVAL + " NEARBY_DURATION " + NEARBY_DURATION);
    }

    private Boolean startNearbyTimerTask() {
        Log.i(TAG, "startNearbyTimerTask");
        nearbyStartTimer = new Timer();
        try {
            TimerTask startNearbyTimerTask = new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void run() {
//                code = 1000 + new Random().nextInt(9000);
//                Log.i(TAG, "New generated code = " + code);
//                nearbyManager.checkAndConnect();
//                nearbyManager.subscribe();
//                nearbyManager.publish(code);
                    if (mBeaconScanner != null) {
                        mBeaconScanner.startScanner();
                    }
                    nearbyStopHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "stopNearbyTimerTask");
//                        nearbyManager.unsubscribe();
//                        nearbyManager.unpublish();
                            mBeaconScanner.stopScanner();
                        }
                    }, NEARBY_DURATION);
                }
            };
            nearbyStartTimer.schedule(startNearbyTimerTask, 0, NEARBY_INTERVAL);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "startNearbyTimerTask error " + e);
            return false;
        }
    }

    private Boolean startBLETimerTask() {
        Log.i(TAG, "startBLETimerTask");
        try {
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
            return true;
        } catch (Exception e) {
            Log.e(TAG, "startBLETimerTask error " + e);
            return false;
        }
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


}
