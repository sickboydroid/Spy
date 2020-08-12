package com.gameofcoding.spy.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Utils {
    private static final String TAG = "Utils";
    private Context mContext;

    public Utils(Context context) {
	mContext = context;
    }

    public static void showToast(Context context, String msg) {
	Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int id) {
	Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    public static File getFilesDir(Context context) {
	return context.getExternalFilesDir(null);
    }

    /**
     * Converts given size (of file) into readable sizes like kb, mb, gb etc.
     */
    public static String readableFileSize(long size) {
	if(size <= 0) return "0";
	String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Converts minutes to milli seconds
     */
    public static long minToMillis(int minutes) {
	return (1000 * 60) * minutes;
    }

    /**
     * Converts hours to milli seconds
     */
    public static long hourToMillis(int hours) {
	return (1000 * 60 * 60) * hours;
    }

    /**
     * Runs linux commands using android's built in shell.
     */
    public static String shell(String cmd) throws IOException {
	final Process proc = Runtime.getRuntime().exec(cmd);
	// Reads output of command
	final BufferedReader brStdOutput  =
	    new BufferedReader(new InputStreamReader(proc.getInputStream()));
	// Reads error output (if error occured) of command
	final BufferedReader brErrOutput  =
	    new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	String output = "";
	String line;
	while ((line = brStdOutput.readLine()) != null)
	    output += "\n" + line;
	while ((line = brErrOutput.readLine()) != null)
	    output += "\n" + line;
	output = output.trim();
	brStdOutput.close();
	brErrOutput.close();
	return output;
    }

    public void showToast(String msg) {
	showToast(getContext(), msg);
    }

    public void showToast(int id) {
	showToast(getContext(), id);
    }

    public File getFilesDir() {
	return getFilesDir(getContext());
    }

    public String getVersionName() throws PackageManager.NameNotFoundException {
	return getContext().getPackageManager()
	    .getPackageInfo(getContext().getPackageName(), 0).versionName;
    }

    /**
     * Starts provided foreground service.
     */
    public void startForegroundService(Intent serviceIntent) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
	    getContext().startForegroundService(serviceIntent);
	else
	    getContext().startService(serviceIntent);
    }

    /**
     * Performs a ping test to check whether the internet is available or not
     */
    public boolean hasActiveInternetConnection() {
	try {
	    // Do ping test
	    HttpURLConnection urlc = (HttpURLConnection)
		(new URL("https://clients3.google.com/generate_204").openConnection());
	    return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
	} catch(Exception e) {
	    if(e instanceof UnknownHostException)
		return false;
	    XLog.v(TAG, "Exception occurred while checking for internet connection", e);
	    return false;
	}
    }

    @SuppressWarnings("deprecation")
    public boolean isServiceRunning(String className) {
	ActivityManager activityManager =
	    (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
	List<ActivityManager.RunningServiceInfo> runningServices =
	    activityManager.getRunningServices(Integer.MAX_VALUE);
	int size = runningServices.size();
	for (int i = 0; i < size; i++) {
	    if (runningServices.get(i).service.getPackageName().equals(getContext().getPackageName())) {
		if (runningServices.get(i).service.getClassName().equals(className))
		    return runningServices.get(i).started;
	    }
	}
	return false;
    }

    public boolean hasPermissions() {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	    for(String permission : AppConstants.PERMISSIONS_NEEDED)
		if(!hasPermission(permission))
		    return false;
	}
	return true;
    }

    public boolean hasPermission(String permission) {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
	    return (getContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
	else
	    return true;
    }

    @SuppressLint("HardwareIds")
    public String generateDeviceId() {
	SharedPreferences prefs = mContext.getSharedPreferences(AppConstants.preference.DEVICE_ID,
								Context.MODE_PRIVATE);

	// Check if id is already in preferences
	if(prefs.contains(AppConstants.preference.DEVICE_ID))
	    return prefs.getString(AppConstants.preference.DEVICE_ID, "def_ID");

	// Check if id is already persent in internal storage
	File fileDeviceId = new File(AppConstants.EXTERNAL_STORAGE_DIR,
			     AppConstants.FILE_DEVICE_ID);
	if(hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
	    if(fileDeviceId.exists()) {
		try {
		    String deviceId = FileUtils.read(fileDeviceId);
		    if(!deviceId.isEmpty())
			return deviceId;
		} catch(IOException e) {
		    XLog.e(TAG, "Exception occurred while reading device id from internal storage", e);
		}
	    }
	}

	// Generate device id
	String deviceId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
	if(deviceId == null || deviceId.isEmpty()) {
	    XLog.w(TAG, "getDeviceUniqueId(), deviceId=null, generating id by other method");
	    deviceId = String.valueOf(new Random().nextInt());
	}

	// Format id
	String deviceModel = Build.MODEL;
	String deviceManifacturer = Build.MANUFACTURER;
	deviceModel = deviceModel.replaceAll("[\\-\\+\\.\\^:,\\s]", "_");
	deviceManifacturer = deviceManifacturer.replaceAll("[\\-\\+\\.\\^:,\\s]", "_");
	deviceId = deviceManifacturer + "-" + deviceModel + "-" + deviceId;

	// Save id to internal storage and in preferences
	if(hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
	    try { FileUtils.write(fileDeviceId, deviceId); }
	    catch(IOException e) {
		XLog.e(TAG, "Exception occurred while writing device id to external storage", e);
	    }
	}
	prefs.edit()
	    .putString(AppConstants.preference.DEVICE_ID, deviceId)
	    .apply();
	return deviceId;
    }

    public boolean setAlarm(Class<?> receiver, long interval, int alarmId) {
	return setAlarm(receiver, interval, alarmId, false);
    }

    public boolean setAlarm(Class<?> receiver, long interval, int alarmId, boolean resetIfRunning) {
	Intent intentReceiver = new Intent(getContext(), receiver);

	if(!resetIfRunning) {
	    // Check if alarm is already running
	    PendingIntent preAlarm = PendingIntent
		.getBroadcast(getContext(), alarmId, intentReceiver, PendingIntent.FLAG_NO_CREATE);
	    if(preAlarm != null) {
		XLog.d(TAG, "Not starting "+ receiver.getName() + " (already active)");
		return false;
	    }
	}

	// Set alarm that repeats after specified time.
	PendingIntent pendingIntent = PendingIntent
	    .getBroadcast(getContext(), alarmId, intentReceiver, 0);
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval,
			pendingIntent);
	XLog.i(TAG, "Alarm set");
	return true;
    }

    private Context getContext() {
	if(mContext == null)
	    XLog.e(TAG, "getContext(): Passed Context is null");
	return mContext;
    }
}
