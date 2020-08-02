package com.gameofcoding.spy.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings.Secure;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
   
    public void showToast(String msg) {
	showToast(getContext(), msg);
    }

    public void showToast(int id) {
	showToast(getContext(), id);
    }

    @SuppressLint("HardwareIds")
    public String generateDeviceId() {
	SharedPreferences prefs = mContext.getSharedPreferences(AppConstants.preference.DEVICE_ID,
								Context.MODE_PRIVATE);
	if(prefs.contains(AppConstants.preference.DEVICE_ID))
	    return prefs.getString(AppConstants.preference.DEVICE_ID, "def_ID");

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

	// Save id
	prefs.edit()
	    .putString(AppConstants.preference.DEVICE_ID, deviceId)
	    .apply();
	return deviceId;
    }

    /**
     * Runs linux commands using android's built in shell.
     *
     * @param cmd Command that you want to execute.
     *
     * @return Output of given command.
     *
     * @throws IOException it could cause while reading output of command.
     */
    public static String shell(String cmd) throws IOException {
	final Process proc = Runtime.getRuntime().exec(cmd);
	// Reads output of command
        final BufferedReader brStdOutput  = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	// Reads error output (if error occured) of command
        final BufferedReader brErrOutput  = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
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

    public boolean hasActiveInternetConnection() throws IOException, MalformedURLException {
	// Check if data is on
	ConnectivityManager cm =
	    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	if (cm == null)
	    return false;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	    NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
	    if (capabilities == null)
		return false;
	    if (!(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
		  || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
		  || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))) {
		return false;
	    }
	} else {
	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    if (activeNetwork == null)
		return false;
        }
	
	// Do ping test
	HttpURLConnection urlc = (HttpURLConnection)
            (new URL("https://clients3.google.com/generate_204").openConnection());
	return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
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

    private Context getContext() {
	if(mContext == null)
	    XLog.e(TAG, "getContext(): Passed Context is null");
	return mContext;
    }
}
