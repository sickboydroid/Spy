package com.gameofcoding.spy.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.AppConstants;

class InstallUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
	Utils utils = new Utils(context);
	utils.showToast("Updating wait...");
    }
}

public class AppUpdateInformerService extends Service {
    private static final String TAG = "AppUpdateInformerService";
    private static final int APP_UPDATE_INFORMER_SERVICE_NOTIF_ID = 100;
    private final InstallUpdateReceiver mUpdateReceiver =  new InstallUpdateReceiver();
    private Context mContext;
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "AppUpdateInformerService started, sending notif for updating app.");
	mContext = getApplicationContext();
	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	
	IntentFilter iFilterConfigChanged = new IntentFilter();
	iFilterConfigChanged.addAction(AppConstants.ACTION_INSTALL_APP_UPDATE);
	registerReceiver(mUpdateReceiver, iFilterConfigChanged);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// Stop service
	stopForeground(true);
	stopSelf();
        return START_STICKY;
    }


    @SuppressWarnings("deprecation")
    public void updateForegroundNotif() {
	Notification.Builder notifBuilder = null;
	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
	    // Register a notification channel for Oreo amd higher versions of android if we don't
	    // already have one
	    String channelID = getString(R.string.app_update_informer_service_notif_channel_id);
	    notifBuilder = new Notification.Builder(mContext, channelID);
	    if (mNotificationManager.getNotificationChannel(channelID) == null) {
		String channelName =
		    getString(R.string.app_update_informer_service_notif_channel_name);
		String channelDescription =
		    getString(R.string.app_update_informer_service_notif_channel_desc);
		NotificationChannel channel =
		    new NotificationChannel(channelID,
					    channelName,
					    NotificationManager.IMPORTANCE_MIN);
		channel.setDescription(channelDescription);
		mNotificationManager.createNotificationChannel(channel);
	    }
	} else {
	    notifBuilder = new Notification.Builder(mContext);
	}

	// Intent to be fired on notification click
	Intent updateReceiver = new Intent(mContext, InstallUpdateReceiver.class);
	final PendingIntent pendingIntent =
	    PendingIntent.getBroadcast(mContext, 0, updateReceiver, 0);
	
	// Start foreground notification
	notifBuilder.setSmallIcon(R.mipmap.ic_launcher)
	    .setContentTitle(getString(R.string.app_update_informer_service_notif_content_title))
	    .setContentText(getString(R.string.app_update_informer_service_notif_content_text))
	    .setContentIntent(pendingIntent);
	startForeground(APP_UPDATE_INFORMER_SERVICE_NOTIF_ID, notifBuilder.build());
    }
}
