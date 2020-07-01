package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEAdvertiser {
    private String TAG = "BLEAdvertiser";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mGattServer;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertisingCallback;
    private DBUtil dbHelper;
    private boolean advertising;
    private Context context;
    private final String appIdentifier = "a9ecdb59-974e-43f0-9d93-27d5dcb060d6";
    private String uniqueIdentifier;

    public BLEAdvertiser(Context context) {
        super();
        this.context = context;
        this.dbHelper = DBUtil.getInstance(context);
        this.advertising = false;
        this.uniqueIdentifier = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.w(TAG, "BlePeripheral onConnectionStateChange ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.v(TAG, "Connected to device: " + device.getName() + " " + device.getAddress());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.v(TAG, "Disconnected from device: " + device.getName() + " " + device.getAddress());
                }
            } else {
                Log.e(TAG, "Error when connecting: " + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.w(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            dbHelper.addEvent("BLE_ADVERTISER", "Device " + device.getAddress() + " tried to read my characteristic");
            Log.w(TAG, "Value: " + Arrays.toString(characteristic.getValue()));

            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.w(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset,
                    value);
            String message = Base64.encodeToString(value, Base64.DEFAULT);
            Log.w(TAG, "Characteristic Write request: " + message);
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }
        }

    };

    private String convertIdToUUID(String id) {
        return id.substring(0, 8) + '-' + id.substring(8, 12) + '-' + id.substring(12) + "-0000-000000000000";
    }

    private boolean addService() {
        Log.w(TAG, "BlePeripheral addService: " + this.appIdentifier);
        mGattServer.clearServices();
        UUID SERVICE_UUID = UUID.fromString(this.appIdentifier);
        BluetoothGattService mGattService = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        String characteristicId = this.convertIdToUUID(this.uniqueIdentifier);
        UUID CHARACTERISTIC_UUID = UUID.fromString(characteristicId);

        BluetoothGattCharacteristic mGattCharacteristic = new BluetoothGattCharacteristic(CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor bluetoothGattDescriptor = new BluetoothGattDescriptor(
                UUID.fromString(convertIdToUUID(uniqueIdentifier)), BluetoothGattCharacteristic.PERMISSION_READ);
        mGattCharacteristic.addDescriptor(bluetoothGattDescriptor);
        mGattCharacteristic.setValue(this.uniqueIdentifier);
        mGattService.addCharacteristic(mGattCharacteristic);

        return mGattServer.addService(mGattService);
    }

    public void startAdvertising() {
        Log.d(TAG, "startAdvertising");
        if (advertising) {
            Log.d(TAG, "Advertising cannot start, already running");
            return;
        }
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "BLUETOOTH_UNAVAILABLE: Bluetooth is not available.");
            dbHelper.addEvent("BLE_ADVERTISER_ERROR", "Start advertising failed: Bluetooth is not available.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "BLUETOOTH_DISABLED: Bluetooth is disabled.");
            dbHelper.addEvent("BLE_ADVERTISER_ERROR", "Start advertising failed: Bluetooth is disabled.");
            return;
        }
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Log.e(TAG, "BLE_UNSUPPORTED: Bluetooth LE Advertising not supported on this device.");
            dbHelper.addEvent("BLE_ADVERTISER_ERROR",
                    "Start advertising failed: Bluetooth LE Advertising not supported on this device.");
            return;
        }

        advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mGattServer = mBluetoothManager.openGattServer(this.context, mGattServerCallback);

        if (mGattServer != null && advertiser != null) {

            boolean addedFlag = this.addService();
            if (!addedFlag) {
                Log.e(TAG, "add service failed");
                dbHelper.addEvent("BLE_ADVERTISER_ERROR", "Start advertising failed: Add service failed.");
            } else {
                AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setConnectable(true)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build();

                AdvertiseData data = new AdvertiseData.Builder().setIncludeDeviceName(false)
                        .addServiceUuid(new ParcelUuid(UUID.fromString(this.appIdentifier))).build();

                advertisingCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        advertising = true;
                        Log.w(TAG, "Started Advertising " + settingsInEffect);
                        dbHelper.addEvent("BLE_ADVERTISER", "Start advertising success with UUID: " + uniqueIdentifier);
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        advertising = false;
                        Log.e(TAG, "Advertising onStartFailure: " + errorCode);
                        dbHelper.addEvent("BLE_ADVERTISER", "Advertising onStartFailure: " + errorCode);
                        super.onStartFailure(errorCode);
                    }
                };
                advertiser.startAdvertising(settings, data, advertisingCallback);
            }
        }
    }

    public void stopAdvertising() {
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && advertiser != null
                && advertisingCallback != null) {
            advertising = false;
            advertiser.stopAdvertising(advertisingCallback);
        }
        dbHelper.addEvent("BLE_ADVERTISER", "Advertising stopped");
    }

    public boolean isAdvertising() {
        return this.advertising;
    }
}