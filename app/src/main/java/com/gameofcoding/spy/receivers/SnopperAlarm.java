package com.gameofcoding.spy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
// import android.widget.Toast;
import com.gameofcoding.spy.services.SnopperService;
import com.gameofcoding.spy.utils.XLog;
// import java.text.Format;
// import java.text.SimpleDateFormat;
// import java.util.Date;

public class SnopperAlarm extends BroadcastReceiver {
    private static final String TAG = "SnopperAlarm";
    private static final String SNOPPER_ALARM_TAG = "Snopper Alarm";
    @Override
    public void onReceive(Context context, Intent intent) {
	XLog.i(TAG, "Snopping alarm received, starting SnopperService.");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
							SNOPPER_ALARM_TAG);
        wakeLock.acquire(); // Acquire the lock
	///////////////////////////////////////////
	// XXX: For debugging purpose only
	// StringBuilder msgStr = new StringBuilder();
        // Format formatter = new SimpleDateFormat("hh:mm:ss a");
        // msgStr.append(formatter.format(new Date()));
        // Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();
	// XLog.v(TAG, "at:--------> " + msgStr);
	//////////////////////////////////////////
	context.startService(new Intent(context, SnopperService.class));
        wakeLock.release(); // Release the lock
    }
}
