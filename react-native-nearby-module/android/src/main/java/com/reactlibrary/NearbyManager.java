package com.reactlibrary;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.google.android.gms.nearby.messages.Strategy.TTL_SECONDS_INFINITE;

public class NearbyManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String androidId;
    static String TAG = "NearbyManager";
    private GoogleApiClient _googleAPIClient;
    private Message _publishedMessage;
    private volatile Boolean _isPublishing = false;
    private volatile Boolean _isSubscribing = false;
    private Boolean _isBLEOnly = false;
    private NearbySql dbHelper;

    private Context mContext;
    private static NearbyManager sInstance;

    public static NearbyManager getInstance(Context context) {
        if (sInstance == null) {
            // Always pass in the Application Context
            sInstance = new NearbyManager(context);
        }
        return sInstance;
    }

    private NearbyManager(Context context) {
        mContext = context;
        connect(true);
        dbHelper = new NearbySql(context);
        androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        Log.i(TAG, "onCreate NEARBY MANAGEEERRR");
    }

    private MessageListener _messageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            super.onFound(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Found: " + messageAsString);
            createEvent("NEARBY_FOUND", messageAsString);
        }

        @Override
        public void onLost(Message message) {
            super.onLost(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Lost: " + messageAsString);
            // createEvent("NEARBY_LOST", messageAsString);
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            Log.d(TAG, "Distance Changed: " + message.toString() + " " + distance.getMeters() + "m");
            createEvent("DISTANCE_CHANGED", message.toString() + " " + distance.getMeters() + "m");
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            Log.d(TAG, "Distance Changed: " + message.toString() + " " + bleSignal.getRssi() + " rssi");
            createEvent("DISTANCE_CHANGED", message.toString() + " " + bleSignal.getRssi() + " rssi");
        }
    };

    private String getPublishedMessageString() {
        return _publishedMessage == null ? null : new String(_publishedMessage.getContent());
    }

    private synchronized GoogleApiClient getGoogleAPIInstance() {
        if (_googleAPIClient == null) {
            _googleAPIClient = new GoogleApiClient.Builder(this.mContext).addApi(Nearby.MESSAGES_API)
                    // TODO: Add more functionality (Currently only: Messages API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        }
        return _googleAPIClient;
    }

    private PublishOptions createPublishOptions() {
        Strategy pubSubStrategy = new Strategy.Builder().setTtlSeconds(60)
                .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build();

        PublishOptions options = new PublishOptions.Builder().setStrategy(pubSubStrategy)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        _isPublishing = false;
                        super.onExpired();
                    }
                }).build();
        return options;
    }

    private SubscribeOptions createSubscribeOptions() {
        Strategy pubSubStrategy = new Strategy.Builder().setTtlSeconds(TTL_SECONDS_INFINITE).build();

        SubscribeOptions options = new SubscribeOptions.Builder().setStrategy(pubSubStrategy)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        _isSubscribing = false;
                        super.onExpired();
                    }
                }).build();
        return options;
    }

    private boolean isMinimumAndroidVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    private boolean isGooglePlayServicesAvailable(boolean showErrorDialog) {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        // int availability = googleApi.isGooglePlayServicesAvailable(this.context);
        // boolean result = availability == ConnectionResult.SUCCESS;
        // if (!result && showErrorDialog &&
        // googleApi.isUserResolvableError(availability)) {
        // // TODO:
        // // googleApi.getErrorDialog(getCurrentActivity(), availability,
        // // PLAY_SERVICES_RESOLUTION_REQUEST).show();
        // }
        return true;
    }

    public void connect(boolean bleOnly) {
        if (!isMinimumAndroidVersion()) {
            createEvent("NEARBY_ERROR",
                    "Current Android version is too low: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        if (!isGooglePlayServicesAvailable(true)) {
            createEvent("NEARBY_ERROR", "Google Play Services is not available on this device.");
            return;
        }
        _isBLEOnly = bleOnly;
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            Log.w(TAG, "Google API Client is already connected.");
            createEvent("NEARBY_CONNECTED", "Already connected.");
            return;
        }
        client.connect();
    }

    public void disconnect() {
        GoogleApiClient client = getGoogleAPIInstance();
        client.disconnect();
        Log.d(TAG, "Google API Client disconnected.");
        createEvent("NEARBY_DISCONNECTED", "Google API Client is disconnected.");
    }

    public boolean isConnected() {
        return getGoogleAPIInstance().isConnected();
    }

    public boolean isPublishing() {
        return _isPublishing;
    }

    public void publish(Integer code) {
        String message = androidId + '-' + code;
        Log.i(TAG, "Attempting to publish: " + message);
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            final Message publishMessage = new Message(message.getBytes());
            _publishedMessage = publishMessage;
            PublishOptions options = createPublishOptions();
            Log.i(TAG, "Publishing message: " + new String(publishMessage.getContent()));
            Nearby.Messages.publish(client, publishMessage, options).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Published message successfully.");
                        _isPublishing = true;
                        createEvent("PUBLISH_SUCCESS", new String(publishMessage.getContent()));
                    } else {
                        Log.e(TAG, "Publish failed.");
                        Log.e(TAG, status.getStatusMessage());
                        _isPublishing = false;
                        createEvent("PUBLISH_FAILED", "Publish failed: " + status.getStatusMessage());
                    }
                }
            });
        } else {
            Log.e(TAG, "Google API Client not connected. Call " + TAG + ".connect() before publishing.");
            createEvent("PUBLISH_FAILED", "Google API Client not connected. Call .connect() before publishing.");
        }
    }

    public void unpublish() {
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected() && (_publishedMessage != null || _isPublishing)) {
            Nearby.Messages.unpublish(client, _publishedMessage);
            _publishedMessage = null;
            _isPublishing = false;
            Log.i(TAG, "Unpublished message.");
        }
        createEvent("NEARBY_UNPUBLISH", "");
    }

    public boolean isSubscribing() {
        return _isSubscribing;
    }

    public void subscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            boolean hasBLE = this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

            if (hasBLE) {
                Log.e(TAG, "STRATEGY BLE");
                createEvent("STRATEGY", "BLE");
            } else {
                createEvent("STRATEGY", "UltraSonic");
            }

            SubscribeOptions options = createSubscribeOptions();
            Nearby.Messages.subscribe(client, _messageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Subscribe success.");
                                _isSubscribing = true;
                                createEvent("SUBSCRIBE_SUCCESS", "");
                            } else {
                                Log.e(TAG, "Subscribe failed");
                                Log.e(TAG, status.getStatusMessage());
                                _isSubscribing = false;
                                createEvent("SUBSCRIBE_FAILED", status.getStatusMessage());
                            }
                        }
                    });
        } else {
            Log.e(TAG, "Google API Client not connected. Call " + TAG + ".connect() before subscribing.");
            createEvent("NEARBY_ERROR", "Google API Client not connected. Call .connect() before subscribing.");
        }
    }

    public void unsubscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected())
            Nearby.Messages.unsubscribe(client, _messageListener);
        Log.i(TAG, "Unsubscribe listener.");
        _isSubscribing = false;
        createEvent("NEARBY_UNSUBSCRIBE", "");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (getGoogleAPIInstance().isConnected()) {
            Log.d(TAG, "Google API Client connected.");
            createEvent("NEARBY_CONNECTED", "Google API connected.");
        } else {
            Log.e(TAG, "Google API Client not connected.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "CONNECTION_SUSPENDED " + i);
        createEvent("CONNECTION_SUSPENDED", "Google Client connection suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "NEARBY_ERROR " + connectionResult.toString());
        createEvent("NEARBY_ERROR", "Google Client connection failed: " + connectionResult.getErrorMessage());
        Log.e(TAG, "connectionResult.hasResolution() = " + connectionResult.hasResolution());
        if (connectionResult.hasResolution()) {
            try {
                PendingIntent pi = connectionResult.getResolution();
                Log.d(TAG, "Attempting to launch permission modal after failure.");
                pi.send();
            } catch (PendingIntent.CanceledException exception) {
                Log.e(TAG, exception.getMessage());
            }
        }
    }

    public void checkAndConnect() {
        if (isConnected() == false) {
            connect(_isBLEOnly);
        }
    }

    public void createEvent(String eventType, String message) {
        try {
            addEvent(eventType, message, getFormattedDate(), Calendar.getInstance().getTimeInMillis());
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    public String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        return formattedDate;
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

    public void removeEvents() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(NearbyEventContract.EventEntry.TABLE_NAME, null, null);
            db.close();
        } catch (Exception e) {
            // do something
        }
    }
}