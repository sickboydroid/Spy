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

public class UploaderService extends Service {
    private static final String TAG = "UploaderService";
    private static final int UPLOADER_SERVICE_NOTIF_ID = 1;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "UploaderService has been started, preparing for uploading data.");
	mContext = getApplicationContext();
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
