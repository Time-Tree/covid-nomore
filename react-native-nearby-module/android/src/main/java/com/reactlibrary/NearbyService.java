package com.reactlibrary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.google.android.gms.nearby.messages.Strategy.TTL_SECONDS_INFINITE;

public class NearbyService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String androidId;
    static String TAG = "NearbyService";
    static String NOTIFICATION_CHANNEL = "CovidNoMore";
    static Integer NOTIFICATION_CHANNEL_ID = 123456;
    static NotificationManagerCompat notificationManager;
    public Integer code = 0;
    private GoogleApiClient _googleAPIClient;
    private Message _publishedMessage;
    private volatile Boolean _isPublishing = false;
    private volatile Boolean _isSubscribing = false;
    private ArrayList<JSONObject> events;
    private Boolean _isBLEOnly = false;

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
        connect(true);
        createBackgroundNotificationChannel();
        androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFICATION_CHANNEL_ID,
                buildForegroundNotification("CovidNoMore", "Background Service", true));
        events = new ArrayList<JSONObject>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    private Timer timer;

    public void startTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        TimerTask timerTask = new TimerTask() {
            public void run() {
                code = 1000 + new Random().nextInt(9000);
                Log.i(TAG, "New generated code = " + code);
                unpublish();
                checkAndConnect();
                publish(code);
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 1000, 30000);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private MessageListener _messageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            super.onFound(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Found: " + messageAsString);
            createEvent("MESSAGE_FOUND", messageAsString);
        }

        @Override
        public void onLost(Message message) {
            super.onLost(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Lost: " + messageAsString);
            createEvent("MESSAGE_LOST", messageAsString);
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
            _googleAPIClient = new GoogleApiClient.Builder(this).addApi(Nearby.MESSAGES_API)
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
        int availability = googleApi.isGooglePlayServicesAvailable(this);
        boolean result = availability == ConnectionResult.SUCCESS;
        if (!result && showErrorDialog && googleApi.isUserResolvableError(availability)) {
            // TODO:
            // googleApi.getErrorDialog(getCurrentActivity(), availability,
            // PLAY_SERVICES_RESOLUTION_REQUEST).show();
        }
        return result;
    }

    public void connect(boolean bleOnly) {
        if (!isMinimumAndroidVersion()) {
            createEvent("CONNECTION_FAILED",
                    "Current Android version is too low: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        if (!isGooglePlayServicesAvailable(true)) {
            createEvent("CONNECTION_FAILED", "Google Play Services is not available on this device.");
            return;
        }
        _isBLEOnly = bleOnly;
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            Log.w(TAG, "Google API Client is already connected.");
            createEvent("CONNECTED", "Already connected.");
            return;
        }
        client.connect();
    }

    public void disconnect() {
        GoogleApiClient client = getGoogleAPIInstance();
        client.disconnect();
        Log.d(TAG, "Google API Client disconnected.");
        createEvent("DISCONNECTED", "Google API Client is disconnected.");
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
    }

    public boolean isSubscribing() {
        return _isSubscribing;
    }

    public void subscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            boolean hasBLE = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

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
            createEvent("CONNECTION_FAILED", "Google API Client not connected. Call .connect() before subscribing.");
        }
    }

    public void unsubscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected())
            Nearby.Messages.unsubscribe(client, _messageListener);
        Log.i(TAG, "Unsubscribe listener.");
        _isSubscribing = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (getGoogleAPIInstance().isConnected()) {
            Log.d(TAG, "Google API Client connected.");
            createEvent("CONNECTED", "Google API connected.");
            this.subscribe();
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
        Log.e(TAG, "CONNECTION_FAILED " + connectionResult.toString());
        createEvent("CONNECTION_FAILED", "Google Client connection failed: " + connectionResult.getErrorMessage());
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
            JSONObject json = new JSONObject();
            json.put("timestamp", Calendar.getInstance().getTimeInMillis());
            json.put("formatDate", getFormattedDate());
            json.put("eventType", eventType);
            json.put("message", message);
            events.add(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    public ArrayList<JSONObject> getEvents() {
        ArrayList<JSONObject> oldEvents = new ArrayList<JSONObject>(events);
        events.clear();
        return oldEvents;
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
