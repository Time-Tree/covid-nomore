package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.RequiresApi;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_SIGNED;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScannerHelper {
    private final String TAG = "BLEScannerHelper";

    private static final String CCC_DESCRIPTOR_UUID = "a9ecdb59-974e-43f0-9d93-27d5dcb060d6";

    public static final int GATT_SUCCESS = 0;
    public static final int GATT_CONN_L2C_FAILURE = 1;
    public static final int GATT_CONN_TIMEOUT = 8;
    public static final int GATT_READ_NOT_PERMITTED = 2;
    public static final int GATT_WRITE_NOT_PERMITTED = 3;
    public static final int GATT_INSUFFICIENT_AUTHENTICATION = 5;
    public static final int GATT_REQUEST_NOT_SUPPORTED = 6;
    public static final int GATT_INSUFFICIENT_ENCRYPTION = 15;
    public static final int GATT_CONN_TERMINATE_PEER_USER = 19;
    public static final int GATT_CONN_TERMINATE_LOCAL_HOST = 22;
    public static final int GATT_CONN_LMP_TIMEOUT = 34;
    public static final int BLE_HCI_CONN_TERMINATED_DUE_TO_MIC_FAILURE = 61;
    public static final int GATT_CONN_FAIL_ESTABLISH = 62;
    public static final int GATT_NO_RESOURCES = 128;
    public static final int GATT_INTERNAL_ERROR = 129;
    public static final int GATT_BUSY = 132;
    public static final int GATT_ERROR = 133;
    public static final int GATT_AUTH_FAIL = 137;
    public static final int GATT_CONN_CANCEL = 256;
    public static final int DEVICE_TYPE_UNKNOWN = 0;
    public static final int DEVICE_TYPE_CLASSIC = 1;
    public static final int DEVICE_TYPE_LE = 2;
    public static final int DEVICE_TYPE_DUAL = 3;
    public static final int BOND_NONE = 10;
    public static final int BOND_BONDING = 11;
    public static final int BOND_BONDED = 12;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONNECTING = 3;
    private static final int MAX_TRIES = 2;
    private static final int DIRECT_CONNECTION_DELAY_IN_MS = 100;
    private static final int CONNECTION_TIMEOUT_IN_MS = 35000;
    private static final int TIMEOUT_THRESHOLD_SAMSUNG = 4500;
    private static final int TIMEOUT_THRESHOLD_DEFAULT = 25000;
    private static final long DELAY_AFTER_BOND_LOST = 1000L;
    private static final int MAX_NOTIFYING_CHARACTERISTICS = 15;

    // Member variables
    private final Context context;
    private final Handler callbackHandler;
    private final BluetoothDevice device;
    private final InternalCallback listener;
    private final Queue<Runnable> commandQueue;
    private boolean commandQueueBusy;
    private boolean isRetrying;
    private boolean bondLost = false;
    private boolean manuallyBonding = false;
    private volatile BluetoothGatt bluetoothGatt;
    private int state;
    private int nrTries;
    private byte[] currentWriteBytes;
    private final Set<UUID> notifyingCharacteristics = new HashSet<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private Runnable discoverServicesRunnable;
    private long connectTimestamp;
    private String cachedName;

    /**
     * This abstract class is used to implement BluetoothGatt callbacks.
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            long timePassed = SystemClock.elapsedRealtime() - connectTimestamp;
            cancelConnectionTimer();
            final int previousState = state;
            state = newState;

            if (status == GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        successfullyConnected(device.getBondState(), timePassed);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        successfullyDisconnected(previousState);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Log.i(TAG, "peripheral is disconnecting");
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Log.i(TAG, "peripheral is connecting");
                    default:
                        Log.e(TAG, "unknown state received");
                        break;
                }
            } else {
                connectionStateChangeUnsuccessful(status, previousState, newState, timePassed);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != GATT_SUCCESS) {
                Log.e(TAG, "-------> service discovery failed due to internal error , disconnecting"
                        + statusToString(status));
                disconnect();
                return;
            }

            final List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, "-------> discovered %d services for " + services.size() + getName());
//            requestConnectionPriority(CONNECTION_PRIORITY_HIGH);
            for(BluetoothGattService service: services) {
                Log.i(TAG, "-------> service UUID" + service.getUuid());
            }
            BluetoothGattService service = getService(UUID.fromString("a9ecdb59-974e-43f0-9d93-27d5dcb060d6"));
            Log.i(TAG, "-------> discovered service " + service);
            if(service != null) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                Log.i(TAG, "-------> characteristics " + characteristics.size());
                for(BluetoothGattCharacteristic characteristic: characteristics) {
                    Log.i(TAG, "-------> characteristic UUID " + characteristic.getUuid());

                }
//                setNotify(getCharacteristic(BTS_SERVICE_UUID, BATTERY_LEVEL_CHARACTERISTIC_UUID), true);
            }


            if (listener != null) {
                listener.connected(BLEScannerHelper.this);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
            if (status != GATT_SUCCESS) {
                Log.e(TAG, "failed to write <> to descriptor of characteristic: <> for device: , "
                        + bytes2String(currentWriteBytes) + parentCharacteristic.getUuid() + getAddress());
            }

            // Check if this was the Client Configuration Descriptor
            if (descriptor.getUuid().equals(UUID.fromString(CCC_DESCRIPTOR_UUID))) {
                if (status == GATT_SUCCESS) {
                    byte[] value = descriptor.getValue();
                    if (value != null) {
                        if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                || Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                            // Notify set to on, add it to the set of notifying characteristics
                            notifyingCharacteristics.add(parentCharacteristic.getUuid());
                            if (notifyingCharacteristics.size() > MAX_NOTIFYING_CHARACTERISTICS) {
                                Log.e(TAG,
                                        "too many (%d) notifying characteristics. The maximum Android can handle is %d"
                                                + notifyingCharacteristics.size() + MAX_NOTIFYING_CHARACTERISTICS);
                            }
                        } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                            // Notify was turned off, so remove it from the set of notifying characteristics
                            notifyingCharacteristics.remove(parentCharacteristic.getUuid());
                        } else {
                            Log.e(TAG, "unexpected CCC descriptor value");
                        }
                    }
                }
            }
            completedCommand();
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            if (status != GATT_SUCCESS) {
                Log.e(TAG, "reading descriptor <> failed for device " + descriptor.getUuid() + getAddress());
            }

            final byte[] value = copyOf(descriptor.getValue());
            completedCommand();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            final byte[] value = copyOf(characteristic.getValue());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
                final int status) {
            if (status != GATT_SUCCESS) {
                if (status == GATT_AUTH_FAIL || status == GATT_INSUFFICIENT_AUTHENTICATION) {
                    // Characteristic encrypted and needs bonding,
                    // So retry operation after bonding completes
                    // This only seems to happen on Android 5/6/7
                    Log.w(TAG, "-------> read needs bonding, bonding in progress");
                    return;
                } else {
                    Log.e(TAG,
                            "-------> read failed for characteristic: , status %d" + characteristic.getUuid() + status);
                    completedCommand();
                    return;
                }
            }

            final byte[] value = copyOf(characteristic.getValue());
            completedCommand();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
                final int status) {
            if (status != GATT_SUCCESS) {
                if (status == GATT_AUTH_FAIL || status == GATT_INSUFFICIENT_AUTHENTICATION) {
                    // Characteristic encrypted and needs bonding,
                    // So retry operation after bonding completes
                    // This only seems to happen on Android 5/6/7
                    Log.i(TAG, "-------> write needs bonding, bonding in progress");
                    return;
                } else {
                    Log.e(TAG, "-------> writing <> to characteristic <> failed, status "
                            + bytes2String(currentWriteBytes) + characteristic.getUuid() + statusToString(status));
                }
            }

            final byte[] value = copyOf(currentWriteBytes);
            currentWriteBytes = null;
            completedCommand();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, final int status) {
            completedCommand();
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, final int mtu, final int status) {
            completedCommand();
        }
    };

    private void successfullyConnected(int bondstate, long timePassed) {
        Log.i(TAG, "connected to   in %.1fs" + getName() + bondStateToString(bondstate) + timePassed / 1000.0f);

        if (bondstate == BOND_NONE || bondstate == BOND_BONDED) {
            delayedDiscoverServices(getServiceDiscoveryDelay(bondstate));
        } else if (bondstate == BOND_BONDING) {
            // Apparently the bonding process has already started, so let it complete. We'll
            // do discoverServices once bonding finished
            Log.i(TAG, "waiting for bonding to complete");
        }
    }

    private void delayedDiscoverServices(final long delay) {
        Log.d(TAG, "-------> delayedDiscoverServices " + delay);
        discoverServicesRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "-------> discovering services of  with delay of %d ms" + getName() + delay);
                if (!bluetoothGatt.discoverServices()) {
                    Log.e(TAG, "discoverServices failed to start");
                }
                discoverServicesRunnable = null;
            }
        };
        mainHandler.postDelayed(discoverServicesRunnable, delay);
    }

    private long getServiceDiscoveryDelay(int bondstate) {
        long delayWhenBonded = 0;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            delayWhenBonded = 1000L;
        }
        Log.d(TAG, "-------> getServiceDiscoveryDelay " + bondstate);
        return bondstate == BOND_BONDED ? delayWhenBonded : 0;
    }

    private void successfullyDisconnected(int previousState) {
        if (previousState == BluetoothProfile.STATE_CONNECTED
                || previousState == BluetoothProfile.STATE_DISCONNECTING) {
            Log.i(TAG, "disconnected  on request" + getName());
        } else if (previousState == BluetoothProfile.STATE_CONNECTING) {
            Log.i(TAG, "cancelling connect attempt");
        }

        if (bondLost) {
            completeDisconnect(false, GATT_SUCCESS);
            if (listener != null) {
                // Consider the loss of the bond a connection failure so that a connection retry
                // will take place
                callbackHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listener.connectFailed(BLEScannerHelper.this, GATT_SUCCESS);
                    }
                }, DELAY_AFTER_BOND_LOST); // Give the stack some time to register the bond loss internally. This is
                // needed on most phones...
            }
        } else {
            completeDisconnect(true, GATT_SUCCESS);
        }
    }

    private void connectionStateChangeUnsuccessful(int status, int previousState, int newState, long timePassed) {
        // Check if service discovery completed
        if (discoverServicesRunnable != null) {
            // Service discovery is still pending so cancel it
            mainHandler.removeCallbacks(discoverServicesRunnable);
            discoverServicesRunnable = null;
        }
        boolean servicesDiscovered = !getServices().isEmpty();

        // See if the initial connection failed
        if (previousState == BluetoothProfile.STATE_CONNECTING) {
            boolean isTimeout = timePassed > getTimoutThreshold();
            String msg = isTimeout ? "TIMEOUT" : "ERROR";
            Log.i(TAG, "connection failed with status  " + statusToString(status) + msg);
            final int adjustedStatus = (status == GATT_ERROR && isTimeout) ? GATT_CONN_TIMEOUT : status;
            completeDisconnect(false, adjustedStatus);
            if (listener != null) {
                listener.connectFailed(BLEScannerHelper.this, adjustedStatus);
            }
        } else if (previousState == BluetoothProfile.STATE_CONNECTED && newState == BluetoothProfile.STATE_DISCONNECTED
                && !servicesDiscovered) {
            // We got a disconnection before the services were even discovered
            Log.i(TAG, "peripheral  disconnected with status  before completing service discovery" + getName()
                    + statusToString(status));
            completeDisconnect(false, status);
            if (listener != null) {
                listener.connectFailed(BLEScannerHelper.this, status);
            }
        } else {
            // See if we got connection drop
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "peripheral  disconnected with status " + getName() + statusToString(status));
            } else {
                Log.i(TAG, "unexpected connection state change for  status " + getName() + statusToString(status));
            }
            completeDisconnect(true, status);
        }
    }

    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null)
                return;
            final BluetoothDevice receivedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (receivedDevice == null)
                return;

            // Ignore updates for other devices
            if (!receivedDevice.getAddress().equalsIgnoreCase(getAddress()))
                return;

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.ERROR);
                handleBondStateChange(bondState, previousBondState);
            }
        }
    };

    private void handleBondStateChange(int bondState, int previousBondState) {
        switch (bondState) {
            case BOND_BONDING:
                Log.d(TAG, "starting bonding with  " + getName() + getAddress());
                break;
            case BOND_BONDED:
                // Bonding succeeded
                Log.d(TAG, "bonded with  " + getName() + getAddress());

                // If bonding was started at connection time, we may still have to discover the
                // services
                if (bluetoothGatt.getServices().isEmpty()) {
                    delayedDiscoverServices(0);
                }

                // If bonding was triggered by a read/write, we must retry it
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    if (commandQueueBusy && !manuallyBonding) {
                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "retrying command after bonding");
                                retryCommand();
                            }
                        }, 50);
                    }
                }

                // If we are doing a manual bond, complete the command
                if (manuallyBonding) {
                    manuallyBonding = false;
                    completedCommand();
                }
                break;
            case BOND_NONE:
                if (previousBondState == BOND_BONDING) {
                    Log.e(TAG, "bonding failed for , disconnecting device" + getName());
                } else {
                    Log.e(TAG, "bond lost for " + getName());
                    bondLost = true;

                    // Cancel the discoverServiceRunnable if it is still pending
                    if (discoverServicesRunnable != null) {
                        mainHandler.removeCallbacks(discoverServicesRunnable);
                        discoverServicesRunnable = null;
                    }
                }
                disconnect();
                break;
        }
    }

    private final BroadcastReceiver pairingRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null)
                return;

            // Skip other devices
            if (!device.getAddress().equalsIgnoreCase(getAddress()))
                return;

            final int variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
            Log.d(TAG, "pairing request received " + ", pairing variant: " + pairingVariantToString(variant) + " ("
                    + variant + ")");

            if (variant == PAIRING_VARIANT_PIN) {
                // String pin = listener.getPincode(BLEScannerHelper.this);
                // if (pin != null) {
                // Log.d(TAG, "Setting PIN code for this peripheral using " + pin);
                // device.setPin(pin.getBytes());
                // abortBroadcast();
                // }
            }
        }
    };

    BLEScannerHelper(Context context, BluetoothDevice device, InternalCallback listener, Handler callbackHandler) {
        if (context == null || device == null || listener == null) {
            Log.e(TAG, "cannot create BLEScannerHelper because of null values");
        }
        this.context = context;
        this.device = device;
        this.listener = listener;
        this.callbackHandler = (callbackHandler != null) ? callbackHandler : new Handler(Looper.getMainLooper());
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.state = BluetoothProfile.STATE_DISCONNECTED;
        this.commandQueueBusy = false;
    }

    void connect() {
        // Make sure we are disconnected before we start making a connection
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Connect to device with autoConnect = false
                    Log.i(TAG, "connect to   using TRANSPORT_LE" + getName() + getAddress());
                    registerBondingBroadcastReceivers();
                    state = BluetoothProfile.STATE_CONNECTING;
                    bluetoothGatt = connectGattHelper(device, false, bluetoothGattCallback);
                    connectTimestamp = SystemClock.elapsedRealtime();
                    startConnectionTimer(BLEScannerHelper.this);
                }
            }, DIRECT_CONNECTION_DELAY_IN_MS);
        } else {
            Log.e(TAG, "peripheral  not yet disconnected, will not connect" + getName());
        }
    }

    void autoConnect() {
        // Note that this will only work for devices that are known! After turning BT
        // on/off Android doesn't know the device anymore!
        // https://stackoverflow.com/questions/43476369/android-save-ble-device-to-reconnect-after-app-close
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Connect to device with autoConnect = true
                    Log.i(TAG, "autoConnect to   using TRANSPORT_LE" + getName() + getAddress());
                    registerBondingBroadcastReceivers();
                    state = BluetoothProfile.STATE_CONNECTING;
                    bluetoothGatt = connectGattHelper(device, true, bluetoothGattCallback);
                    connectTimestamp = SystemClock.elapsedRealtime();
                }
            });
        } else {
            Log.e(TAG, "peripheral  not yet disconnected, will not connect" + getName());
        }
    }

    private void registerBondingBroadcastReceivers() {
        context.registerReceiver(bondStateReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        context.registerReceiver(pairingRequestBroadcastReceiver,
                new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
    }

    public boolean createBond() {
        // Check if we have a Gatt object
        if (bluetoothGatt == null) {
            // No gatt object so no connection issued, do create bond immediately
            return device.createBond();
        }

        // Enqueue the bond command because a connection has been issued or we are
        // already connected
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                manuallyBonding = true;
                if (!device.createBond()) {
                    Log.e(TAG, "bonding failed for " + getAddress());
                    completedCommand();
                } else {
                    Log.d(TAG, "manually bonding " + getAddress());
                    nrTries++;
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue bonding command");
        }
        return result;
    }

    public boolean requestConnectionPriority(final int priority) {

        // Enqueue the request connection priority command and complete is immediately
        // as there is no callback for it
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (!bluetoothGatt.requestConnectionPriority(priority)) {
                        Log.e(TAG, "could not set connection priority");
                    } else {
                        Log.d(TAG, "requesting connection priority %d" + priority);
                    }
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue request connection priority command");
        }
        return result;
    }

    private boolean createBond(int transport) {
        Log.d(TAG, "bonding using TRANSPORT_LE");
        boolean result = false;
        try {
            Method bondMethod = device.getClass().getMethod("createBond", int.class);
            if (bondMethod != null) {
                result = (boolean) bondMethod.invoke(device, transport);
            }
        } catch (Exception e) {
            Log.e(TAG, "could not invoke createBond method");
        }
        return result;
    }

    public void cancelConnection() {
        // Check if we have a Gatt object
        if (bluetoothGatt == null) {
            return;
        }

        // Check if we are not already disconnected or disconnecting
        if (state == BluetoothProfile.STATE_DISCONNECTED || state == BluetoothProfile.STATE_DISCONNECTING) {
            return;
        }

        // Cancel the connection timer
        cancelConnectionTimer();

        // Check if we are in the process of connecting
        if (state == BluetoothProfile.STATE_CONNECTING) {
            // Cancel the connection by calling disconnect
            disconnect();

            // Since we will not get a callback on onConnectionStateChange for this, we
            // complete the disconnect ourselves
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    completeDisconnect(true, GATT_SUCCESS);
                }
            }, 50);
        } else {
            // Cancel active connection
            disconnect();
        }
    }

    private void disconnect() {
        if (state == BluetoothProfile.STATE_CONNECTED || state == BluetoothProfile.STATE_CONNECTING) {
            this.state = BluetoothProfile.STATE_DISCONNECTING;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (bluetoothGatt != null) {
                        Log.i(TAG, "force disconnect  " + getName() + getAddress());
                        bluetoothGatt.disconnect();
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.disconnected(BLEScannerHelper.this, GATT_CONN_TERMINATE_LOCAL_HOST);
            }
        }
    }

    void disconnectWhenBluetoothOff() {
        bluetoothGatt = null;
        completeDisconnect(true, GATT_SUCCESS);
    }

    private void completeDisconnect(boolean notify, final int status) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        commandQueue.clear();
        commandQueueBusy = false;
        try {
            context.unregisterReceiver(bondStateReceiver);
            context.unregisterReceiver(pairingRequestBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            // In case bluetooth is off, unregisering broadcast receivers may fail
        }
        bondLost = false;
        if (listener != null && notify) {
            listener.disconnected(BLEScannerHelper.this, status);
        }
    }

    public String getAddress() {
        return device.getAddress();
    }

    public int getType() {
        return device.getType();
    }

    public String getName() {
        String name = device.getName();
        if (name != null) {
            // Cache the name so that we even know it when bluetooth is switched off
            cachedName = name;
        }
        return cachedName;
    }

    public int getBondState() {
        return device.getBondState();
    }

    @SuppressWarnings("WeakerAccess")
    public List<BluetoothGattService> getServices() {
        return bluetoothGatt.getServices();
    }

    public BluetoothGattService getService(UUID serviceUUID) {
        if (bluetoothGatt != null) {
            return bluetoothGatt.getService(serviceUUID);
        } else {
            return null;
        }
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattService service = getService(serviceUUID);
        if (service != null) {
            return service.getCharacteristic(characteristicUUID);
        } else {
            return null;
        }
    }

    public int getState() {
        return state;
    }

    public boolean isNotifying(BluetoothGattCharacteristic characteristic) {
        return notifyingCharacteristics.contains(characteristic.getUuid());
    }

    private boolean isConnected() {
        return bluetoothGatt != null && state == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        // Check if gatt object is valid
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is 'null', ignoring read request");
            return false;
        }

        // Check if characteristic is valid
        if (characteristic == null) {
            Log.e(TAG, "characteristic is 'null', ignoring read request");
            return false;
        }

        // Check if this characteristic actually has READ property
        if ((characteristic.getProperties() & PROPERTY_READ) == 0) {
            Log.e(TAG, "characteristic does not have read property");
            return false;
        }

        // Enqueue the read command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (!bluetoothGatt.readCharacteristic(characteristic)) {
                        Log.e(TAG, "readCharacteristic failed for characteristic: " + characteristic.getUuid());
                        completedCommand();
                    } else {
                        Log.d(TAG, "reading characteristic <>" + characteristic.getUuid());
                        nrTries++;
                    }
                } else {
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue read characteristic command");
        }
        return result;
    }

    public boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic, final byte[] value,
            final int writeType) {
        // Check if gatt object is valid
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is 'null', ignoring read request");
            return false;
        }

        // Check if characteristic is valid
        if (characteristic == null) {
            Log.e(TAG, "characteristic is 'null', ignoring write request");
            return false;
        }

        // Check if byte array is valid
        if (value == null) {
            Log.e(TAG, "value to write is 'null', ignoring write request");
            return false;
        }

        // Copy the value to avoid race conditions
        final byte[] bytesToWrite = copyOf(value);

        // Check if this characteristic actually supports this writeType
        int writeProperty;
        switch (writeType) {
            case WRITE_TYPE_DEFAULT:
                writeProperty = PROPERTY_WRITE;
                break;
            case WRITE_TYPE_NO_RESPONSE:
                writeProperty = PROPERTY_WRITE_NO_RESPONSE;
                break;
            case WRITE_TYPE_SIGNED:
                writeProperty = PROPERTY_SIGNED_WRITE;
                break;
            default:
                writeProperty = 0;
                break;
        }
        if ((characteristic.getProperties() & writeProperty) == 0) {
            Log.e(TAG, "characteristic <> does not support writeType " + characteristic.getUuid()
                    + writeTypeToString(writeType));
            return false;
        }

        // Enqueue the write command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    currentWriteBytes = bytesToWrite;
                    characteristic.setValue(bytesToWrite);
                    characteristic.setWriteType(writeType);
                    if (!bluetoothGatt.writeCharacteristic(characteristic)) {
                        Log.e(TAG, "writeCharacteristic failed for characteristic: " + characteristic.getUuid());
                        completedCommand();
                    } else {
                        Log.d(TAG, "writing <> to characteristic <>" + bytes2String(bytesToWrite)
                                + characteristic.getUuid());
                        nrTries++;
                    }
                } else {
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue write characteristic command");
        }
        return result;
    }

    public boolean readDescriptor(final BluetoothGattDescriptor descriptor) {
        // Check if gatt object is valid
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is 'null', ignoring read request");
            return false;
        }

        // Check if descriptor is valid
        if (descriptor == null) {
            Log.e(TAG, "descriptor is 'null', ignoring read request");
            return false;
        }

        // Enqueue the read command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (!bluetoothGatt.readDescriptor(descriptor)) {
                        Log.e(TAG, "readDescriptor failed for characteristic: " + descriptor.getUuid());
                        completedCommand();
                    } else {
                        nrTries++;
                    }
                } else {
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue read descriptor command");
        }
        return result;
    }

    public boolean writeDescriptor(final BluetoothGattDescriptor descriptor, final byte[] value) {
        // Check if gatt object is valid
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is 'null', ignoring write descriptor request");
            return false;
        }

        // Check if characteristic is valid
        if (descriptor == null) {
            Log.e(TAG, "descriptor is 'null', ignoring write request");
            return false;
        }

        // Check if byte array is valid
        if (value == null) {
            Log.e(TAG, "value to write is 'null', ignoring write request");
            return false;
        }

        // Copy the value to avoid race conditions
        final byte[] bytesToWrite = copyOf(value);

        // Enqueue the write command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    currentWriteBytes = bytesToWrite;
                    descriptor.setValue(bytesToWrite);
                    if (!bluetoothGatt.writeDescriptor(descriptor)) {
                        Log.e(TAG, "writeDescriptor failed for descriptor: " + descriptor.getUuid());
                        completedCommand();
                    } else {
                        Log.d(TAG, "writing <> to descriptor <>" + bytes2String(bytesToWrite) + descriptor.getUuid());
                        nrTries++;
                    }
                } else {
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue write descriptor command");
        }
        return result;
    }

    public boolean setNotify(final BluetoothGattCharacteristic characteristic, final boolean enable) {
        // Check if gatt object is valid
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is 'null', ignoring set notify request");
            return false;
        }

        // Check if characteristic is valid
        if (characteristic == null) {
            Log.e(TAG, "characteristic is 'null', ignoring setNotify request");
            return false;
        }

        // Get the Client Configuration Descriptor for the characteristic
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
        if (descriptor == null) {
            Log.e(TAG, "could not get CCC descriptor for characteristic " + characteristic.getUuid());
            return false;
        }

        // Check if characteristic has NOTIFY or INDICATE properties and set the correct
        // byte value to be written
        byte[] value;
        int properties = characteristic.getProperties();
        if ((properties & PROPERTY_NOTIFY) > 0) {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else if ((properties & PROPERTY_INDICATE) > 0) {
            value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else {
            Log.e(TAG, "characteristic  does not have notify or indicate property" + characteristic.getUuid());
            return false;
        }
        final byte[] finalValue = enable ? value : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

        // Queue Runnable to turn on/off the notification now that all checks have been
        // passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (!isConnected()) {
                    completedCommand();
                    return;
                }

                // First set notification for Gatt object
                if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
                    Log.e(TAG, "setCharacteristicNotification failed for characteristic: " + characteristic.getUuid());
                }

                // Then write to descriptor
                currentWriteBytes = finalValue;
                descriptor.setValue(finalValue);
                boolean result;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result = bluetoothGatt.writeDescriptor(descriptor);
                } else {
                    // Up to Android 6 there is a bug where Android takes the writeType of the
                    // parent characteristic instead of always WRITE_TYPE_DEFAULT
                    // See:
                    // https://android.googlesource.com/platform/frameworks/base/+/942aebc95924ab1e7ea1e92aaf4e7fc45f695a6c%5E%21/#F0
                    final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
                    final int originalWriteType = parentCharacteristic.getWriteType();
                    parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    result = bluetoothGatt.writeDescriptor(descriptor);
                    parentCharacteristic.setWriteType(originalWriteType);
                }
                if (!result) {
                    Log.e(TAG, "writeDescriptor failed for descriptor: " + descriptor.getUuid());
                    completedCommand();
                } else {
                    nrTries++;
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue setNotify command");
        }

        return result;
    }

    public boolean clearServicesCache() {
        boolean result = false;
        try {
            Method refreshMethod = bluetoothGatt.getClass().getMethod("refresh");
            if (refreshMethod != null) {
                result = (boolean) refreshMethod.invoke(bluetoothGatt);
            }
        } catch (Exception e) {
            Log.e(TAG, "could not invoke refresh method");
        }
        return result;
    }

    public boolean readRemoteRssi() {
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (!bluetoothGatt.readRemoteRssi()) {
                        Log.e(TAG, "readRemoteRssi failed");
                        completedCommand();
                    }
                } else {
                    Log.e(TAG, "cannot get rssi, peripheral not connected");
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue setNotify command");
        }

        return result;
    }

    public boolean requestMtu(final int mtu) {
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (!bluetoothGatt.requestMtu(mtu)) {
                        Log.e(TAG, "requestMtu failed");
                        completedCommand();
                    }
                } else {
                    Log.e(TAG, "cannot request MTU, peripheral not connected");
                    completedCommand();
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "could not enqueue setNotify command");
        }

        return result;
    }

    /**
     * The current command has been completed, move to the next command in the queue
     * (if any)
     */
    private void completedCommand() {
        isRetrying = false;
        commandQueue.poll();
        commandQueueBusy = false;
        nextCommand();
    }

    /**
     * Retry the current command. Typically used when a read/write fails and
     * triggers a bonding procedure
     */
    private void retryCommand() {
        commandQueueBusy = false;
        Runnable currentCommand = commandQueue.peek();
        if (currentCommand != null) {
            if (nrTries >= MAX_TRIES) {
                // Max retries reached, give up on this one and proceed
                Log.d(TAG, "max number of tries reached, not retrying operation anymore");
                commandQueue.poll();
            } else {
                isRetrying = true;
            }
        }
        nextCommand();
    }

    private void nextCommand() {
        synchronized (this) {
            // If there is still a command being executed, then bail out
            if (commandQueueBusy)
                return;

            // Check if there is something to do at all
            final Runnable bluetoothCommand = commandQueue.peek();
            if (bluetoothCommand == null)
                return;

            // Check if we still have a valid gatt object
            if (bluetoothGatt == null) {
                Log.e(TAG, "gatt is 'null' for peripheral , clearing command queue" + getAddress());
                commandQueue.clear();
                commandQueueBusy = false;
                return;
            }

            // Execute the next command in the queue
            commandQueueBusy = true;
            if (!isRetrying) {
                nrTries = 0;
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothCommand.run();
                    } catch (Exception ex) {
                        Log.e(TAG, "command exception for device " + getName());
                        completedCommand();
                    }
                }
            });
        }
    }

    private String bondStateToString(final int state) {
        switch (state) {
            case BOND_NONE:
                return "BOND_NONE";
            case BOND_BONDING:
                return "BOND_BONDING";
            case BOND_BONDED:
                return "BOND_BONDED";
            default:
                return "UNKNOWN";
        }
    }

    private String stateToString(final int state) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "CONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "CONNECTING";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "DISCONNECTING";
            default:
                return "DISCONNECTED";
        }
    }

    private String writeTypeToString(final int writeType) {
        switch (writeType) {
            case WRITE_TYPE_DEFAULT:
                return "WRITE_TYPE_DEFAULT";
            case WRITE_TYPE_NO_RESPONSE:
                return "WRITE_TYPE_NO_RESPONSE";
            case WRITE_TYPE_SIGNED:
                return "WRITE_TYPE_SIGNED";
            default:
                return "unknown writeType";
        }
    }

    private static String statusToString(final int error) {
        switch (error) {
            case GATT_SUCCESS:
                return "SUCCESS";
            case GATT_CONN_L2C_FAILURE:
                return "GATT CONN L2C FAILURE";
            case GATT_CONN_TIMEOUT:
                return "GATT CONN TIMEOUT"; // Connection timed out
            case GATT_CONN_TERMINATE_PEER_USER:
                return "GATT CONN TERMINATE PEER USER";
            case GATT_CONN_TERMINATE_LOCAL_HOST:
                return "GATT CONN TERMINATE LOCAL HOST";
            case BLE_HCI_CONN_TERMINATED_DUE_TO_MIC_FAILURE:
                return "BLE HCI CONN TERMINATED DUE TO MIC FAILURE";
            case GATT_CONN_FAIL_ESTABLISH:
                return "GATT CONN FAIL ESTABLISH";
            case GATT_CONN_LMP_TIMEOUT:
                return "GATT CONN LMP TIMEOUT";
            case GATT_CONN_CANCEL:
                return "GATT CONN CANCEL ";
            case GATT_BUSY:
                return "GATT BUSY";
            case GATT_ERROR:
                return "GATT ERROR"; // Device not reachable
            case GATT_AUTH_FAIL:
                return "GATT AUTH FAIL"; // Device needs to be bonded
            case GATT_NO_RESOURCES:
                return "GATT NO RESOURCES";
            case GATT_INTERNAL_ERROR:
                return "GATT INTERNAL ERROR";
            default:
                return "UNKNOWN (" + error + ")";
        }
    }

    private static final int PAIRING_VARIANT_PIN = 0;
    private static final int PAIRING_VARIANT_PASSKEY = 1;
    private static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    private static final int PAIRING_VARIANT_CONSENT = 3;
    private static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    private static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    private static final int PAIRING_VARIANT_OOB_CONSENT = 6;

    private String pairingVariantToString(final int variant) {
        switch (variant) {
            case PAIRING_VARIANT_PIN:
                return "PAIRING_VARIANT_PIN";
            case PAIRING_VARIANT_PASSKEY:
                return "PAIRING_VARIANT_PASSKEY";
            case PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                return "PAIRING_VARIANT_PASSKEY_CONFIRMATION";
            case PAIRING_VARIANT_CONSENT:
                return "PAIRING_VARIANT_CONSENT";
            case PAIRING_VARIANT_DISPLAY_PASSKEY:
                return "PAIRING_VARIANT_DISPLAY_PASSKEY";
            case PAIRING_VARIANT_DISPLAY_PIN:
                return "PAIRING_VARIANT_DISPLAY_PIN";
            case PAIRING_VARIANT_OOB_CONSENT:
                return "PAIRING_VARIANT_OOB_CONSENT";
            default:
                return "UNKNOWN";
        }
    }

    private static String bytes2String(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    interface InternalCallback {
        void connected(BLEScannerHelper device);

        void connectFailed(BLEScannerHelper device, final int status);

        void disconnected(BLEScannerHelper device, final int status);

        // String getPincode(BLEScannerHelper device);

    }

    /////////////////

    private BluetoothGatt connectGattHelper(BluetoothDevice remoteDevice, boolean autoConnect,
            BluetoothGattCallback bluetoothGattCallback) {

        if (remoteDevice == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || !autoConnect) {
            return connectGattCompat(bluetoothGattCallback, remoteDevice, autoConnect);
        }

        try {
            Object iBluetoothGatt = getIBluetoothGatt(getIBluetoothManager());

            if (iBluetoothGatt == null) {
                Log.e(TAG, "could not get iBluetoothGatt object");
                return connectGattCompat(bluetoothGattCallback, remoteDevice, true);
            }

            BluetoothGatt bluetoothGatt = createBluetoothGatt(iBluetoothGatt, remoteDevice);

            if (bluetoothGatt == null) {
                Log.e(TAG, "could not create BluetoothGatt object");
                return connectGattCompat(bluetoothGattCallback, remoteDevice, true);
            }

            boolean connectedSuccessfully = connectUsingReflection(remoteDevice, bluetoothGatt, bluetoothGattCallback,
                    true);

            if (!connectedSuccessfully) {
                Log.i(TAG, "connection using reflection failed, closing gatt");
                bluetoothGatt.close();
            }

            return bluetoothGatt;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | NoSuchFieldException exception) {
            Log.e(TAG, "error during reflection");
            return connectGattCompat(bluetoothGattCallback, remoteDevice, true);
        }
    }

    private BluetoothGatt connectGattCompat(BluetoothGattCallback bluetoothGattCallback, BluetoothDevice device,
            boolean autoConnect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return device.connectGatt(context, autoConnect, bluetoothGattCallback, TRANSPORT_LE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Try to call connectGatt with TRANSPORT_LE parameter using reflection
            try {
                Method connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class,
                        BluetoothGattCallback.class, int.class);
                try {
                    return (BluetoothGatt) connectGattMethod.invoke(device, context, autoConnect, bluetoothGattCallback,
                            TRANSPORT_LE);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        // Fallback on connectGatt without TRANSPORT_LE parameter
        return device.connectGatt(context, autoConnect, bluetoothGattCallback);
    }

    @SuppressWarnings("SameParameterValue")
    private boolean connectUsingReflection(BluetoothDevice device, BluetoothGatt bluetoothGatt,
            BluetoothGattCallback bluetoothGattCallback, boolean autoConnect)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        setAutoConnectValue(bluetoothGatt, autoConnect);
        Method connectMethod = bluetoothGatt.getClass().getDeclaredMethod("connect", Boolean.class,
                BluetoothGattCallback.class);
        connectMethod.setAccessible(true);
        return (Boolean) (connectMethod.invoke(bluetoothGatt, true, bluetoothGattCallback));
    }

    private BluetoothGatt createBluetoothGatt(Object iBluetoothGatt, BluetoothDevice remoteDevice)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor bluetoothGattConstructor = BluetoothGatt.class.getDeclaredConstructors()[0];
        bluetoothGattConstructor.setAccessible(true);
        if (bluetoothGattConstructor.getParameterTypes().length == 4) {
            return (BluetoothGatt) (bluetoothGattConstructor.newInstance(context, iBluetoothGatt, remoteDevice,
                    TRANSPORT_LE));
        } else {
            return (BluetoothGatt) (bluetoothGattConstructor.newInstance(context, iBluetoothGatt, remoteDevice));
        }
    }

    private Object getIBluetoothGatt(Object iBluetoothManager)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (iBluetoothManager == null) {
            return null;
        }

        Method getBluetoothGattMethod = getMethodFromClass(iBluetoothManager.getClass(), "getBluetoothGatt");
        return getBluetoothGattMethod.invoke(iBluetoothManager);
    }

    private Object getIBluetoothManager()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            return null;
        }

        Method getBluetoothManagerMethod = getMethodFromClass(bluetoothAdapter.getClass(), "getBluetoothManager");
        return getBluetoothManagerMethod.invoke(bluetoothAdapter);
    }

    private Method getMethodFromClass(Class<?> cls, String methodName) throws NoSuchMethodException {
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
    }

    private void setAutoConnectValue(BluetoothGatt bluetoothGatt, boolean autoConnect)
            throws NoSuchFieldException, IllegalAccessException {
        Field autoConnectField = bluetoothGatt.getClass().getDeclaredField("mAutoConnect");
        autoConnectField.setAccessible(true);
        autoConnectField.setBoolean(bluetoothGatt, autoConnect);
    }

    private void startConnectionTimer(final BLEScannerHelper peripheral) {
        cancelConnectionTimer();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "connection timout, disconnecting " + peripheral.getName());
                disconnect();
                completeDisconnect(true, GATT_CONN_TIMEOUT);
                timeoutRunnable = null;
            }
        };

        mainHandler.postDelayed(timeoutRunnable, CONNECTION_TIMEOUT_IN_MS);
    }

    private void cancelConnectionTimer() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private int getTimoutThreshold() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equals("samsung")) {
            return TIMEOUT_THRESHOLD_SAMSUNG;
        } else {
            return TIMEOUT_THRESHOLD_DEFAULT;
        }
    }

    private byte[] copyOf(byte[] source) {
        if (source == null)
            return new byte[0];
        final int sourceLength = source.length;
        final byte[] copy = new byte[sourceLength];
        System.arraycopy(source, 0, copy, 0, sourceLength);
        return copy;
    }
}
