package com.tangledbytes.sw.collectors;

import android.os.Build;
import android.util.Log;

import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DeviceInfoCollector extends Collector {
    private static final String TAG = "DeviceInfoCollector";
    @Override
    public void collect() {
        try {
            JSONObject hardwareInfo = getHardwareInfo();
            JSONObject installedApps = getInstalledApps();
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("hardware-info", hardwareInfo);
            deviceInfo.put("installed-apps", installedApps);
            FileUtils.write(Constants.FILE_SERVER_DEVICE_INFO, deviceInfo.toString());
        } catch (IOException | JSONException e) {
            Log.wtf(TAG, "Failed to get device info",e);
        }
    }

    private JSONObject getInstalledApps() {
        return null;
    }

    private JSONObject getHardwareInfo() throws JSONException {
        JSONObject hardwareInfo = new JSONObject();
        hardwareInfo.put("model", Build.MODEL);
        hardwareInfo.put("manufacturer", Build.MANUFACTURER);
        hardwareInfo.put("version", Build.VERSION.SDK_INT);
        return hardwareInfo;
    }
}
