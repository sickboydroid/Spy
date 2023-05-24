package com.tangledbytes.sw.collectors;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.FileUtils;
import com.tangledbytes.sw.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceInfoCollector extends Collector {
    private static final String TAG = "DeviceInfoCollector";
    private final Context mContext;

    public DeviceInfoCollector(Context context) {
        mContext = context;
    }
    @Override
    public void collect() {
        try {
            JSONObject hardwareInfo = getHardwareInfo();
            JSONArray installedApps = getInstalledApps();
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("hardware-info", hardwareInfo);
            deviceInfo.put("installed-apps", installedApps);
            FileUtils.write(Constants.FILE_SERVER_DEVICE_INFO, deviceInfo.toString());
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to get device info", e);
        }
    }

    private JSONArray getInstalledApps() throws JSONException, ErrnoException {
        JSONArray installedApps = new JSONArray();
        final PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            File sourceDir = new File(packageInfo.sourceDir);
            JSONObject app = new JSONObject();
            app.put("app_name", packageInfo.loadLabel(pm));
            app.put("package", packageInfo.packageName);
            app.put("last_update", Utils.formatEpochTime(sourceDir.lastModified()));
            installedApps.put(app);
        }
        return installedApps;
    }

    private JSONObject getHardwareInfo() throws JSONException {
        JSONObject hardwareInfo = new JSONObject();
        hardwareInfo.put("model", Build.MODEL);
        hardwareInfo.put("manufacturer", Build.MANUFACTURER);
        hardwareInfo.put("version", Build.VERSION.SDK_INT);
        return hardwareInfo;
    }
}
