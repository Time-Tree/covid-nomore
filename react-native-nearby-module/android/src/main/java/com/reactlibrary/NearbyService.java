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


public class NearbyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    private ArrayList<String> messages;
    private Boolean _isBLEOnly = false;

    private final IBinder myBinder = new NearbyBinder();

    public class NearbyBinder extends Binder {
        NearbyService getService() {
            return NearbyService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate NEARBY SERVICE");
        super.onCreate();
        connect(true);
        createBackgroundNotificationChannel();
        androidId = Secure.getString(getContentResolver(),
                Secure.ANDROID_ID);
        notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFICATION_CHANNEL_ID, buildForegroundNotification("CovidNoMore", "Background Service", true));
        messages = new ArrayList<String>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    private Timer timer;
    private TimerTask timerTask = new TimerTask() {
        public void run() {
            code = 1000 + new Random().nextInt(9000);
            Log.i(TAG, "New generated code = " + code);
            unpublish();
//          TODO: verify if it is connected and subscribed
            publish(code);
        }
    };

    public void startTimer() {
        timer = new Timer();
        timer.schedule(timerTask, 1000, 10000);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private MessageListener _messageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            super.onFound(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Found: " + messageAsString);
            addMessage("MESSAGE_FOUND = " + messageAsString);
        }

        @Override
        public void onLost(Message message) {
            super.onLost(message);
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Message Lost: " + messageAsString);
            addMessage("MESSAGE_LOST = " + messageAsString);
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            Log.d(TAG, "Distance Changed: " + message.toString() + " " + distance.getMeters() + "m");
            addMessage("Distance Changed: " + message.toString() + " " + distance.getMeters() + "m");
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            Log.d(TAG, "Distance Changed: " + message.toString() + " " + bleSignal.getRssi() + " rssi");
            addMessage("Distance Changed: " + message.toString() + " " + bleSignal.getRssi() + " rssi");
        }
    };

    private String getPublishedMessageString() {
        return _publishedMessage == null ? null : new String(_publishedMessage.getContent());
    }

    private synchronized GoogleApiClient getGoogleAPIInstance() {
        if (_googleAPIClient == null) {
            _googleAPIClient = new GoogleApiClient.Builder(this)
                    .addApi(Nearby.MESSAGES_API)
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
//            TODO:
//            googleApi.getErrorDialog(getCurrentActivity(), availability, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        }
        return result;
    }

    public void connect(boolean bleOnly) {
        if (!isMinimumAndroidVersion()) {
            addMessage("CONNECTION_FAILED: Current Android version is too low: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        if (!isGooglePlayServicesAvailable(true)) {
            addMessage("CONNECTION_FAILED: Google Play Services is not available on this device.");
            return;
        }
        _isBLEOnly = bleOnly;
        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            Log.w(TAG, "Google API Client is already connected.");
            addMessage("Already connected.");
            return;
        }
        client.connect();
    }

    public void disconnect() {
        GoogleApiClient client = getGoogleAPIInstance();
        client.disconnect();
        Log.d(TAG, "Google API Client disconnected.");
        addMessage("Google API Client is disconnected.");
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
                        addMessage("PUBLISH_SUCCESS " + publishMessage);
                    } else {
                        Log.e(TAG, "Publish failed.");
                        Log.e(TAG, status.getStatusMessage());
                        _isPublishing = false;
                        addMessage("Publish failed: " + status.getStatusMessage());
                    }
                }
            });
        } else {
            Log.e(TAG, "Google API Client not connected. Call " + TAG + ".connect() before publishing.");
            addMessage("Google API Client not connected. Call .connect() before publishing.");
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
            boolean hasBLE = getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

            if (hasBLE) {
                Log.e(TAG, "STRATEGY BLE");
                addMessage("STRATEGY = BLE");
            } else {
                addMessage("STRATEGY = UltraSonic");
            }

            SubscribeOptions options = createSubscribeOptions();
            Nearby.Messages.subscribe(client, _messageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Subscribe success.");
                                _isSubscribing = true;
                                messages.add("SUBSCRIBE_SUCCESS");
                            } else {
                                Log.e(TAG, "Subscribe failed");
                                Log.e(TAG, status.getStatusMessage());
                                _isSubscribing = false;
                                messages.add("Subscribe failed: " + status.getStatusMessage());
                            }
                        }
                    });
        } else {
            Log.e(TAG, "Google API Client not connected. Call " + TAG + ".connect() before subscribing.");
            addMessage("Google API Client not connected. Call .connect() before subscribing.");
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
            addMessage("Google API connected.");
        } else {
            Log.e(TAG, "Google API Client not connected.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "CONNECTION_SUSPENDED " + i);
        addMessage("Google Client connection suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "CONNECTION_FAILED " + connectionResult.toString());
        addMessage("Google Client connection failed: " + connectionResult.getErrorMessage());
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

    public void addMessage(String message) {
        String formattedDate = "[ " + getFormattedDate() + " ] ";
        messages.add(formattedDate + message);
    }

    public String getFormattedDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SS");
        String formattedDate = df.format(c);
        Log.e(TAG, "formattedDate ===>  " + formattedDate);
        return formattedDate;
    }

    public ArrayList<String> getMessages() {
        ArrayList<String> oldMessages = new ArrayList<String>(messages);
        messages.clear();
        return oldMessages;
    }


    private Notification buildForegroundNotification(String title, String text, boolean nonRemovable) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);

        b.setOngoing(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
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
