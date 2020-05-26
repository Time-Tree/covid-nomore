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
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class NearbyBLEScanner {
    private static String TAG = "NearbyBLEScanner";
    private BluetoothLeScanner scanner;
    private boolean isStarted;
    private NearbySql dbHelper;
    private Context context;
    private List<BluetoothGatt> gattConnections = new ArrayList<>();
    private HashMap<String, Long>  deviceTimes = new HashMap<>();

    public NearbyBLEScanner(final BluetoothAdapter bluetoothAdapter, NearbySql dbHelper, final Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        isStarted = false;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            ScanRecord record = result.getScanRecord();
            BluetoothDevice device = result.getDevice();
            if (record == null || device == null) return;


            Long deviceTime = deviceTimes.get(device.getAddress());
            if (deviceTime == null) deviceTime = 1L;

            long newTimestamp = Calendar.getInstance().getTimeInMillis() / 10000 * 10000;
            long trimTime = deviceTime / 10000 * 10000;
            if (newTimestamp == trimTime) return;

            deviceTimes.put(device.getAddress(), Calendar.getInstance().getTimeInMillis());

            BluetoothGatt bluetoothGatt = result.getDevice().connectGatt(context, false, gattCallback, TRANSPORT_LE);
            gattConnections.add(bluetoothGatt);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "error");
        }
    };

    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                    if (status == GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            gatt.close();
                        } else {
                            gatt.disconnect();
                        }
                    } else {
                        gatt.close();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == GATT_SUCCESS) {
                       BluetoothGattService service = gatt.getService(UUID.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6"));
                       List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic gattCharacteristic :
                                gattCharacteristics) {
                            if (gattCharacteristic.getUuid() != null) {
                                Log.i(TAG, "BLE FOUND" + gattCharacteristic.getUuid().toString());
                            }
                        }

                    }
                    gatt.disconnect();
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == GATT_SUCCESS) {
                        Log.i(TAG, "BLE FOUND" + characteristic.getUuid().toString());
                    }
                }

            };


    public void start() {
        try {
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settingsBuilder.setLegacy(true);
            }
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6"))
                    .build();
            scanner.startScan(Collections.singletonList(filter), settingsBuilder.build(), scanCallback);
            isStarted = true;
            Log.i(TAG, "Scan started");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void clearServicesCache()
    {
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
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void restart() {
        try {
            if (isStarted) {
                stop();
                clearServicesCache();
            }
            start();
        } catch (Exception e) {
            Log.e(TAG, "Restart error " + e.toString());
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
