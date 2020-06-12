package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScanner {
    private static String TAG = "BLEScanner";
    private BluetoothLeScanner scanner;
    private boolean isStarted;
    private DBManager dbHelper;
    private Context context;
    private List<BluetoothGatt> gattConnections = new ArrayList<>();
    private HashMap<String, Long> deviceTimes = new HashMap<>();

    public BLEScanner(final BluetoothAdapter bluetoothAdapter, DBManager dbHelper, final Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        isStarted = false;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            Log.i(TAG, "onScanResult " + callbackType + " result " + result);
            ScanRecord record = result.getScanRecord();
            BluetoothDevice device = result.getDevice();
            if (record == null || device == null)
                return;

            Long deviceTime = deviceTimes.get(device.getAddress());
            if (deviceTime == null)
                deviceTime = 1L;

            long newTimestamp = Calendar.getInstance().getTimeInMillis() / 10000 * 10000;
            long trimTime = deviceTime / 10000 * 10000;
            if (newTimestamp == trimTime)
                return;
            Log.i(TAG, "onScanResult: " + device.getAddress());

            deviceTimes.put(device.getAddress(), Calendar.getInstance().getTimeInMillis());

            BluetoothGatt bluetoothGatt = result.getDevice().connectGatt(context, false, gattCallback, TRANSPORT_LE);
            gattConnections.add(bluetoothGatt);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "onScanFailed error: " + errorCode);
            addEvent("BLE_SCANNER_ERROR", String.valueOf(errorCode));
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                restart();
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            Log.d(TAG, "onConnectionStateChange status" + status + " newState: " + newState);
            if (status == GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "STATE_CONNECTED: " + gatt.getDevice().getAddress());
                    // gatt.discoverServices();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            boolean ans = gatt.discoverServices();
                            Log.d(TAG, "Discover Services started: " + ans);
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "STATE_DISCONNECTED: " + gatt.getDevice().getName());
                    gatt.close();
                } else {
                    Log.i(TAG, "STATE: " + newState);
                    gatt.disconnect();
                }
            } else {
                Log.i(TAG, "STATUS: " + status);
                deviceTimes.remove(gatt.getDevice().getAddress());
                gatt.close();
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered entered " + gatt.getDevice() + " status " + status);
            if (status == GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered: " + gatt.getDevice().getAddress());
                BluetoothGattService service = gatt.getService(UUID.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6"));
                Log.i(TAG, "service " + service.toString());
                List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                Log.i(TAG, "gattCharacteristics size" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    Log.i(TAG, "gattCharacteristic uuid" + gattCharacteristic.getUuid());
                    if (gattCharacteristic.getUuid() != null) {
                        String uuid = gattCharacteristic.getUuid().toString();
                        Log.i(TAG, "uuid.substring(18): " + uuid.substring(18));
                        if (uuid.substring(18) == "-0000-000000000000") {
                            uuid = uuid.substring(0, 18);
                        }
                        String message = "NM: " + gatt.getDevice().getName() + " ID: " + uuid;
                        addEvent("BLE_FOUND", message);
                        Log.i(TAG, "BLE FOUND" + uuid);
                    }
                }
            } else {
                Log.i(TAG, "onServicesDiscovered: failed" + status);
            }
            Log.i(TAG, "onServicesDiscovered: DISCONNECT");
            gatt.disconnect();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead " + gatt.getDevice() + " status " + status);
            if (status == GATT_SUCCESS) {
                Log.i(TAG, "BLE FOUND" + characteristic.getUuid().toString());
            }
        }

    };

    public void start() {
        try {
            Log.i(TAG, "start");
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settingsBuilder.setLegacy(true);
            }
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6")).build();
            scanner.startScan(Collections.singletonList(filter), settingsBuilder.build(), scanCallback);
            isStarted = true;
            Log.i(TAG, "Scan started");
            addEvent("BLE_SCANNER", "Scanning started");
        } catch (Exception e) {
            addEvent("BLE_SCANNER_ERROR", "Start advertising failed: " + e.getMessage());
            Log.e(TAG, "Start advertising failed: " + e.getMessage());
        }
    }

    public void clearServicesCache() {
        Log.d(TAG, "clearServicesCache");
        List<BluetoothGatt> toRemove = new ArrayList<>();
        for (BluetoothGatt gatt : gattConnections) {
            try {
                Method refreshMethod = gatt.getClass().getMethod("refresh");
                refreshMethod.invoke(gatt);
                toRemove.add(gatt);
            } catch (Exception e) {
                Log.e(TAG, "ERROR: Could not invoke refresh method");
            }
        }
        gattConnections.removeAll(toRemove);
    }

    public void stop() {
        try {
            scanner.stopScan(scanCallback);
            isStarted = false;
            Log.i(TAG, "Scan stopped");
            addEvent("BLE_SCANNER", "Scanning stopped");
        } catch (Exception e) {
            Log.e(TAG, "Scan stop " + e.getMessage());
        }
    }

    public void restart() {
        try {
            Log.i(TAG, "Scan restarted");
            if (isStarted) {
                stop();
                clearServicesCache();
            }
            start();
        } catch (Exception e) {
            Log.e(TAG, "Restart error " + e.toString());
        }
    }

    private long addEvent(String eventType, String message) {
        long newRowId = -1;
        String formatDate = getFormattedDate();
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    public String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        return formattedDate;
    }
}
