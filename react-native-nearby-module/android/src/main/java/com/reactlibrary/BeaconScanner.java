package com.reactlibrary;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class BeaconScanner {
    private static String TAG = "BeaconScanner";
    private boolean isStarted = false;
    private boolean isScanning = false;
    private DBUtil dbHelper;
    private HTTPUtil httpUtil;
    private HashMap<String, Long> foundDevices = new HashMap<>();
    private HashMap<String, Integer> signalStrengths = new HashMap<String, Integer>();
    private BleManager bleManager = null;
    private Application currentApplication = null;
    private int pendingConnections = 0;
    private final String appIdentifier = "A9ECDB59974E43F09D9327D5DCB060D6";

    public BeaconScanner(Context context) {
        this.dbHelper = DBUtil.getInstance(context);
        this.httpUtil = new HTTPUtil(context);
        bleManager = BleManager.getInstance();
    }

    public void setCurrentApplication(Application application) {
        currentApplication = application;
        bleManager.init(application);
        bleManager.enableLog(false);
        setScanSettings();
    }

    private void setScanSettings() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setScanTimeOut(8000).build();
        bleManager.initScanRule(scanRuleConfig);
    }

    public void startScanner() {
        Log.d(TAG, "Scanning started");
        dbHelper.addEvent("BEACON_SCANNER", "Scanning started");
        isStarted = true;
        startScan();
    }

    public void stopScanner() {
        Log.i(TAG, "Scanner stopped");
        bleManager.cancelScan();
        bleManager.disconnectAllDevice();
        bleManager.destroy();
        dbHelper.addEvent("BEACON_SCANNER", "Scanning stopped");
        isStarted = false;
        isScanning = false;
    }

    public void startScan() {
        if (!isStarted) return;
        if (currentApplication == null) return;
        if (isScanning) return;
        Log.d(TAG, "Scan loop start");
        bleManager.scan(scanCallback);
    }

    BleScanCallback scanCallback = new BleScanCallback() {
        @Override
        public void onScanStarted(boolean success) {
            Log.d(TAG, "onScanStarted " + success);
            isScanning = true;
        }

        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            isScanning = true;
            Boolean found = false;
            for (BleDevice bleDevice : scanResultList) {
                String hexRecord = bytesToHex(bleDevice.getScanRecord());
                String UUID = hexRecord.substring(12, 44);
                String major = hexRecord.substring(44, 48);
                String minor = hexRecord.substring(48, 52);
                String TX = hexRecord.substring(52, 56);
                String data = hexRecord.substring(44, 56);
                Integer rssi = bleDevice.getRssi();
                String macAddress = bleDevice.getDevice().getAddress();
                if (UUID.equals(appIdentifier)) {
                    String output =  macAddress + "," + data + "," + rssi;
                    Log.i(TAG, "BEACON_SCANNER FOUND " + output);
                    dbHelper.addEvent("BEACON_SCANNER", "BEACON_FOUND " + output);
                    httpUtil.ping("B", output);
                    found = true;
                }
            }
            isScanning = false;
            if (!found) {
//                dbHelper.addEvent("BEACON_SCANNER", "Not found in loop");
                Log.d(TAG, "Not found in loop");
            }
            Log.d(TAG, "Scan loop stop");
            startScan();
        }

        @Override
        public void onLeScan(BleDevice bleDevice) {
//            String hexRecord = bytesToHex(bleDevice.getScanRecord());
//            String UUID = hexRecord.substring(12, 44);
//            String major = hexRecord.substring(44, 48);
//            String minor = hexRecord.substring(48, 52);
//            String TX = hexRecord.substring(52, 56);
//            String data = hexRecord.substring(44, 56);
//            Integer rssi = bleDevice.getRssi();
//            if (UUID.equals(appIdentifier)) {
//                String macAddress = bleDevice.getDevice().getAddress();
//                Log.i(TAG, "SYNC DATA - BEACON " + macAddress + " RSSI: " + rssi + " data: " + data);
//                httpUtil.ping("B", macAddress);
//            }
//            Log.i(TAG, "LE SCAN");
        }

        @Override
        public void onScanning(BleDevice bleDevice) {
//            Log.d(TAG, "onScanning found");
//            Log.d(TAG, "Mac: " + bleDevice.getMac());
//            Log.d(TAG, "Name: " + bleDevice.getName());
//            Log.d(TAG, "Type: " + bleDevice.getDevice().getType());
//            Log.d(TAG, "Address: " + bleDevice.getDevice().getAddress());
//            Log.d(TAG, "Key: " + bleDevice.getKey());
        }
    };

    private String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
