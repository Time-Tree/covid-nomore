package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScanner {
    private static String TAG = "BLEScanner";
    private BluetoothLeScanner scanner;
    private boolean isStarted;
    private NearbySql dbHelper;
    private Context context;
    private List<BluetoothGatt> gattConnections = new ArrayList<>();
    private HashMap<String, Long> deviceTimes = new HashMap<>();
    private final Handler callBackHandler = new Handler();

    private static final long SCAN_TIMEOUT = 180_000L;
    private static final int SCAN_RESTART_DELAY = 1000;
    private static final int MAX_CONNECTION_RETRIES = 1;
    private static final int MAX_CONNECTED_PERIPHERALS = 7;

    private Runnable timeoutRunnable;
    private final Object connectLock = new Object();
    private boolean expectingBluetoothOffDisconnects = false;
    private final Map<String, BLEScannerHelper> connectedPeripherals = new ConcurrentHashMap<>();
    private final Map<String, BLEScannerHelper> unconnectedPeripherals = new ConcurrentHashMap<>();
    private final Map<String, BLEScannerHelper> reconnectCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Integer> connectionRetries = new ConcurrentHashMap<>();
    private final List<String> reconnectPeripheralAddresses = new ArrayList<>();
    private Runnable disconnectRunnable;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BLEScanner(final BluetoothAdapter bluetoothAdapter, NearbySql dbHelper, final Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        isStarted = false;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            synchronized (this) {
                callBackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BLEScannerHelper peripheral = new BLEScannerHelper(context, result.getDevice(),
                                internalCallback, callBackHandler);
                        connectPeripheral(peripheral);
                    }
                });
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "onScanFailed error: " + errorCode);
        }
    };

    private final BLEScannerHelper.InternalCallback internalCallback = new BLEScannerHelper.InternalCallback() {

        @Override
        public void connected(final BLEScannerHelper peripheral) {
            connectionRetries.remove(peripheral.getAddress());
            unconnectedPeripherals.remove(peripheral.getAddress());
            connectedPeripherals.put(peripheral.getAddress(), peripheral);
            if (connectedPeripherals.size() == MAX_CONNECTED_PERIPHERALS) {
                Log.i(TAG, "maximum amount (%d) of connected peripherals reached" + MAX_CONNECTED_PERIPHERALS);
            }
        }

        @Override
        public void connectFailed(final BLEScannerHelper peripheral, final int status) {
            unconnectedPeripherals.remove(peripheral.getAddress());

            // Get the number of retries for this peripheral
            int nrRetries = 0;
            if (connectionRetries.get(peripheral.getAddress()) != null) {
                Integer retries = connectionRetries.get(peripheral.getAddress());
                if (retries != null)
                    nrRetries = retries;
            }

            // Retry connection or conclude the connection has failed
            if (nrRetries < MAX_CONNECTION_RETRIES && status != 8) {
                Log.i(TAG, "retrying connection to " + peripheral.getAddress());
                nrRetries++;
                connectionRetries.put(peripheral.getAddress(), nrRetries);
                unconnectedPeripherals.put(peripheral.getAddress(), peripheral);

                // Retry with autoconnect
                peripheral.autoConnect();
            }
        }

        @Override
        public void disconnected(final BLEScannerHelper peripheral, final int status) {
            if (expectingBluetoothOffDisconnects) {
                cancelDisconnectionTimer();
                expectingBluetoothOffDisconnects = false;
            }

            connectedPeripherals.remove(peripheral.getAddress());
            unconnectedPeripherals.remove(peripheral.getAddress());
        }
    };

    public void startScan() {
        try {
            ScanSettings scanSettings;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT).setReportDelay(0L).build();
            } else {
                scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(0L).build();
            }
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6")).build();
            scanner.startScan(Collections.singletonList(filter), scanSettings, scanCallback);
            setScanTimer();
            isStarted = true;
            Log.i(TAG, "Scan started");
            addEvent("BLE_SCANNER", "Scanning started");
        } catch (Exception e) {
            addEvent("BLE_SCANNER_ERROR", "Start advertising failed: " + e.getMessage());
            Log.e(TAG, "Start advertising failed: " + e.getMessage());
        }
    }

    public void stopScan() {
        cancelTimeoutTimer();
        if (isScanning()) {
            scanner.stopScan(scanCallback);
            Log.i(TAG, "scan stopped");
        } else {
            Log.i(TAG, "no scan to stop because no scan is running");
        }
    }

    /**
     * Check if a scanning is active
     *
     * @return true if a scan is active, otherwise false
     */
    public boolean isScanning() {
        return (scanner != null);
    }

    public void connectPeripheral(BLEScannerHelper peripheral) {
        synchronized (connectLock) {
            // Make sure peripheral is valid
            if (peripheral == null) {
                Log.i(TAG, "no valid peripheral specified, aborting connection");
                return;
            }

            // Check if we are already connected to this peripheral
            if (connectedPeripherals.containsKey(peripheral.getAddress())) {
                Log.i(TAG, "already connected to  '" + peripheral.getAddress());
                return;
            }

            // Check if we already have an outstanding connection request for this
            // peripheral
            if (unconnectedPeripherals.containsKey(peripheral.getAddress())) {
                Log.i(TAG, "already connecting to " + peripheral.getAddress());
                return;
            }

            // Check if the peripheral is cached or not. If not, issue a warning
            int deviceType = peripheral.getType();
            if (deviceType == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                // The peripheral is not cached so connection is likely to fail
                Log.i(TAG, "peripheral with address '%s' is not in the Bluetooth cache, hence connection may fail"
                        + peripheral.getAddress());
            }

            // It is all looking good! Set the callback and prepare to connect
            unconnectedPeripherals.put(peripheral.getAddress(), peripheral);

            // Now connect
            peripheral.connect();
        }
    }

    public void cancelConnection(final BLEScannerHelper peripheral) {
        // Check if peripheral is valid
        if (peripheral == null) {
            Log.i(TAG, "cannot cancel connection, peripheral is null");
            return;
        }

        // First check if we are doing a reconnection scan for this peripheral
        String peripheralAddress = peripheral.getAddress();
        if (reconnectPeripheralAddresses.contains(peripheralAddress)) {
            // Clean up first
            reconnectPeripheralAddresses.remove(peripheralAddress);
            reconnectCallbacks.remove(peripheralAddress);
            Log.i(TAG, "cancelling autoconnect for " + peripheralAddress);
            return;
        }

        // Check if it is an unconnected peripheral
        if (unconnectedPeripherals.containsKey(peripheralAddress)) {
            BLEScannerHelper unconnectedPeripheral = unconnectedPeripherals.get(peripheralAddress);
            if (unconnectedPeripheral != null) {
                unconnectedPeripheral.cancelConnection();
            }
            return;
        }

        // Check if this is a connected peripheral
        if (connectedPeripherals.containsKey(peripheralAddress)) {
            BLEScannerHelper connectedPeripheral = connectedPeripherals.get(peripheralAddress);
            if (connectedPeripheral != null) {
                connectedPeripheral.cancelConnection();
            }
        } else {
            Log.i(TAG, "cannot cancel connection to unknown peripheral %" + peripheralAddress);
        }
    }

    private void setScanTimer() {
        cancelTimeoutTimer();

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "scanning timeout, restarting scan");
                stopScan();

                // Restart the scan and timer
                callBackHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startScan();
                    }
                }, SCAN_RESTART_DELAY);
            }
        };

        mainHandler.postDelayed(timeoutRunnable, SCAN_TIMEOUT);
    }

    private void cancelTimeoutTimer() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private void cancelAllConnectionsWhenBluetoothOff() {
        Log.i(TAG, "disconnect all peripherals because bluetooth is off");
        // Call cancelConnection for connected peripherals
        for (final BLEScannerHelper peripheral : connectedPeripherals.values()) {
            peripheral.disconnectWhenBluetoothOff();
        }
        connectedPeripherals.clear();

        // Call cancelConnection for unconnected peripherals
        for (final BLEScannerHelper peripheral : unconnectedPeripherals.values()) {
            peripheral.disconnectWhenBluetoothOff();
        }
        unconnectedPeripherals.clear();

        // Clean up autoconnect by scanning information
        reconnectPeripheralAddresses.clear();
        reconnectCallbacks.clear();
    }

    private void startDisconnectionTimer() {
        cancelDisconnectionTimer();
        disconnectRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "bluetooth turned off but no automatic disconnects happening, so doing it ourselves");
                cancelAllConnectionsWhenBluetoothOff();
                disconnectRunnable = null;
            }
        };

        mainHandler.postDelayed(disconnectRunnable, 1000);
    }

    private void cancelDisconnectionTimer() {
        if (disconnectRunnable != null) {
            mainHandler.removeCallbacks(disconnectRunnable);
            disconnectRunnable = null;
        }
    }

    private long addEvent(String eventType, String message) {
        long newRowId = -1;
        String formatDate = getFormattedDate();
        Long timestamp = Calendar.getInstance().getTimeInMillis();
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
