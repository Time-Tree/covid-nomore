package com.reactlibrary;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class NearbyBLEScanner {
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private Handler handler;
    private boolean isStarted;
    private long timestamp;
    private NearbySql dbHelper;

    public NearbyBLEScanner(NearbySql dbHelper) {
        this.dbHelper = dbHelper;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.i("BLE Adapter", "NO supported");
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            Log.i("BLE Adapter", "NOT Enabled");
        } else {
            // Bluetooth is enabled
            Log.i("BLE Adapter", "Ok");
        }
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler();
        isStarted = false;
        timestamp = Calendar.getInstance().getTimeInMillis();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                //super.onScanResult(callbackType, result);
                ScanRecord sr = result.getScanRecord();
                if (sr != null) {
                    String msg = "";
                    if (sr.getDeviceName() != null) {
                        long newTimestamp = Calendar.getInstance().getTimeInMillis() / 10000 * 10000;
                        long trimTime = timestamp / 10000 * 10000;
                        if (newTimestamp != trimTime) {
                            timestamp = Calendar.getInstance().getTimeInMillis();
                            msg = "NM: " + sr.getDeviceName();
                            msg = msg + " TX: " + sr.getTxPowerLevel();
                            msg = msg + " AF: " + sr.getAdvertiseFlags();
                            if (sr.getServiceUuids() != null) {
                                msg = msg + " SU: " + sr.getServiceUuids().toString();
                            }
                            Log.i("BLE SCAN FOUND", msg);
                            addEvent("BLE SCAN", msg, getFormattedDate(), Calendar.getInstance().getTimeInMillis());
                        }
                    }
                }
            }
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.i("BLE SCAN FAILED", "error");
            }
        };
    }

    public void start() {
        try {
            Log.i("BLE", "Scan starting...");
            //parseInt(new Date().getTime() / 1000 ) * 1000


            if (isStarted) {
                stop();
            }
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            scanner.startScan(null, scanSettings, scanCallback);
            isStarted = true;
            Log.i("BLE", "Scan started");
        } catch (Exception e) {
            Log.e("BLE ERROR", e.getMessage());
        }
    }

    public void stop() {
        try {
            Log.i("BLE", "Scan stopping...");
            scanner.stopScan(scanCallback);
            Log.i("BLE", "Scan stopped");
        } catch (Exception e) {
            Log.e("BLE ERROR", e.getMessage());
        }
    }

    private long addEvent(String eventType, String message, String formatDate, Long timestamp) {
        long newRowId = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NearbyEventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, eventType);
            values.put(NearbyEventContract.EventEntry.COLUMN_NAME_MESSAGE, message);
            values.put(NearbyEventContract.EventEntry.COLUMN_NAME_FORMAT_DATE, formatDate);
            values.put(NearbyEventContract.EventEntry.COLUMN_NAME_TIMESTAMP, timestamp);
            newRowId = db.insert(NearbyEventContract.EventEntry.TABLE_NAME, null, values);
            db.close();
        } catch (Error e) {
            e.printStackTrace();
        }
        return newRowId;
    }

    public String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        return formattedDate;
    }
}
