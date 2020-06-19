package com.gameofcoding.spy.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarm;
import com.gameofcoding.spy.utils.XLog;

public class UploaderService extends Service {
    private static final String TAG = "UploaderService";
    private static final int UPLOADER_SERVICE_NOTIF_ID = 1;
    private Context mContext;
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "UploaderService has been started, preparing for uploading data.");
	mContext = getApplicationContext();
    	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// XXX: Only for debugging
	updateForegroundNotif("Checking app update...");
	synchronized(this){try{wait(8000);}catch(Exception e) {throw new RuntimeException(e);}}

	// TODO:
	// 1: Check app update
	// 2: if(update) send update Notif
	// 3: Check if changes has been done
	// 5: if(Changes) Upload changes
	// 6: if(Changes & uploaded unsuccessfully) return error
	// 7: Pull new changes
	// 8: Goto 1 and 2
	// 9: If all went right then stop alarm

	// Stopping alarm
	XLog.i(TAG, "Uploaded and downloaded successfully!, stopping 'UploaderAlarm' alarm.");
	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
	PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(mContext, 0, new Intent(mContext, UploaderAlarm.class), 0);
	alarmManager.cancel(pendingIntent);

	// Stop service
	stopForeground(true);
	stopSelf();
        return START_STICKY;
    }


    @SuppressWarnings("deprecation")
    public void updateForegroundNotif(String contentText) {
	Notification.Builder notifBuilder = null;
	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
	    // Register a notification channel for Oreo amd higher versions of android if we don't
	    // already have one
	    String channelID = getString(R.string.uploaderservice_notif_channel_id);
	    notifBuilder = new Notification.Builder(mContext, channelID);
	    if (mNotificationManager.getNotificationChannel(channelID) == null) {
		String channelName = getString(R.string.uploaderservice_notif_channel_name);
		String channelDescription = getString(R.string.uploaderservice_notif_channel_desc);
		NotificationChannel channel =
		    new NotificationChannel(channelID, channelName,
					    NotificationManager.IMPORTANCE_MIN);
		channel.setDescription(channelDescription);
		mNotificationManager.createNotificationChannel(channel);
	    }
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
