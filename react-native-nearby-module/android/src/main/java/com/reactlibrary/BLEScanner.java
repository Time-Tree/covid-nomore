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
public class BLEScanner {
    private static String TAG = "BLEScanner";
    private boolean isStarted = false;
    private boolean isScanning = false;
    private DBUtil dbHelper;
    private HashMap<String, Long> foundDevices = new HashMap<>();
    private HashMap<String, Integer> signalStrengths = new HashMap<String, Integer>();
    private BleManager bleManager = null;
    private Application currentApplication = null;
    private int pendingConnections = 0;
    private final String appIdentifier = "a9ecdb59-974e-43f0-9d93-27d5dcb060d6";

    public BLEScanner(Context context) {
        this.dbHelper = DBUtil.getInstance(context);
        bleManager = BleManager.getInstance();
    }

    public void setCurrentApplication(Application application) {
        currentApplication = application;
        bleManager.init(application);
        bleManager.enableLog(true).setReConnectCount(5, 300);
        setScanSettings();
    }

    private void setScanSettings() {
        UUID[] services = {UUID.fromString(appIdentifier)};
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setServiceUuids(services).build();
        bleManager.initScanRule(scanRuleConfig);
    }

    public void startScanner() {
        if (isStarted)
            return;
        dbHelper.addEvent("BLE_SCANNER", "Scanning started");
        isStarted = true;
        startScan();
    }

    public void stopScanner() {
        bleManager.cancelScan();
        bleManager.disconnectAllDevice();
        dbHelper.addEvent("BLE_SCANNER", "Scanning stopped");
        isStarted = false;
        isScanning = false;
        foundDevices.clear();
    }

    public void startScan() {
        Log.i(TAG, "start");
        if (!isStarted)
            return;
        if (currentApplication == null)
            return;
        if (isScanning)
            return;
        bleManager.scan(scanCallback);

    }

    BleScanCallback scanCallback = new BleScanCallback() {

        @Override
        public void onScanStarted(boolean success) {
            Log.i(TAG, "onScanStarted " + success);
            isScanning = success;
        }

        @Override
        public void onScanning(BleDevice bleDevice) {
            Log.i(TAG, "onScanning " + bleDevice.getName() + " " + bleDevice.getMac());
        }

        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            if (!isStarted)
                return;
            Log.i(TAG, "onScanFinished " + scanResultList.size());
            for (BleDevice bleDevice : scanResultList) {
                Log.i(TAG, "onScanFinished " + bleDevice.getName() + " " + bleDevice.getMac());
                Long storedDevice = foundDevices.get(bleDevice.getMac());
                if (storedDevice != null) {
                    Log.i(TAG, "already found " + bleDevice.getMac());
                } else {
                    pendingConnections++;
                    bleManager.connect(bleDevice.getMac(), connectCallback);
                    Log.i(TAG, "-----> deviceRSSI " + bleDevice.getRssi());
                    signalStrengths.put(bleDevice.getMac(), bleDevice.getRssi());
                }
            }
            Log.i(TAG, "onScanFinished pendingConnections: " + pendingConnections);
            if (pendingConnections == 0) {
                isScanning = false;
                startScan();
            }
        }
    };

    private BleGattCallback connectCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            Log.i(TAG, "onStartConnect ");
        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            Log.i(TAG, "onConnectFail " + bleDevice.getName() + " " + bleDevice.getMac());
            Log.i(TAG, "onConnectFail exception " + exception.getDescription() + " " + exception.getCode());
            checkPendingConections();
        }

        @Override
        public void onConnectSuccess(final BleDevice bleDevice, BluetoothGatt gatt, int status) {
            Log.i(TAG, "-----> onConnectSuccess " + bleDevice.getName() + " " + bleDevice.getMac());
            BluetoothGattService service = gatt.getService(UUID.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6"));
            if (service != null) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.i(TAG, "characteristic " + characteristic.getUuid());
                    String uuid = characteristic.getUuid().toString();
                    String message = "";
                    if (bleDevice.getName() != null) {
                        message += "NM: " + bleDevice.getName() + ",";
                    }
                    message += " ID: " + uuid;
                    String macAddress = bleDevice.getMac();
                    Integer deviceRSSI = signalStrengths.get(macAddress);
                    if (deviceRSSI != null) {
                        message += ", RSSI: " + deviceRSSI;
                        signalStrengths.remove(macAddress);
                    }
                    dbHelper.addEvent("BLE_FOUND", message);
                    foundDevices.put(macAddress, Calendar.getInstance().getTimeInMillis());

                    ContentValues values = new ContentValues();
                    values.put("token", uuid);
                    values.put("rssi", deviceRSSI);
                    values.put("characteristicData", 0);
                    dbHelper.addHandshake(values);
                    bleManager.read(bleDevice, appIdentifier, appIdentifier, new BleReadCallback() {
                        @Override
                        public void onReadSuccess(byte[] data) {
                            try {
                                Log.i(TAG, "onReadSuccess");
                                String uniqueIdentifier = new String(data, "UTF-8");
                                String message = "Characteristic data found. ID: " + uniqueIdentifier;
                                dbHelper.addEvent("BLE_DATA_FOUND", message);
                                dbHelper.updateCharacteristicData(uniqueIdentifier);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                Log.i(TAG, "onReadSuccess catch" + e.getMessage());
                            }
                            bleManager.disconnect(bleDevice);
                            checkPendingConections();
                        }

                        @Override
                        public void onReadFailure(BleException exception) {
                            Log.i(TAG, "onReadFailure: " + exception.getDescription() + " " + exception.getCode());
                            bleManager.disconnect(bleDevice);
                            checkPendingConections();
                        }
                    });
                }
            }
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            Log.i(TAG, "onDisConnected " + device.getName() + " " + device.getMac());
            checkPendingConections();
        }
    };

    synchronized void checkPendingConections() {
        if (pendingConnections > 0) {
            pendingConnections--;
        }
        if (pendingConnections == 0) {
            isScanning = false;
            startScan();
        }
    }
}
