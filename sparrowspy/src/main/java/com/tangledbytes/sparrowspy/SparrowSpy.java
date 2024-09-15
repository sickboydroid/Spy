package com.tangledbytes.sparrowspy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.tangledbytes.sparrowspy.events.SparrowActions;
import com.tangledbytes.sparrowspy.services.sparrowservice.SparrowService;
import com.tangledbytes.sparrowspy.ui.LicenseDialogFragment;
import com.tangledbytes.sparrowspy.utils.Constants;

public class SparrowSpy {
    private static final String TAG = "SparrowSpy";
    public static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private final AppCompatActivity mActivity;
    private final PermissionManager mPermissionManager;

    private final BroadcastReceiver onLicenseAcceptedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(SparrowActions.ACTION_LICENSE_ACCEPTED)) {
                Log.i(TAG, "License accepted, starting SparrowService...");
                startSparrowService();
            }
        }
    };
    private SparrowSpy(AppCompatActivity activity) {
        mActivity = activity;
        mPermissionManager = new PermissionManager(activity);
    }

    public static SparrowSpy init(AppCompatActivity activity) {
        SparrowSpy spy = new SparrowSpy(activity);
        spy.verifyLicenseAgreement(spy::startSparrowService);
        return spy;

    }

    private void startSparrowService() {
        if (!mPermissionManager.hasAllPermissions()) {
            Log.d(TAG, "All permissions are not granted, prompting for permission grant...");
            mPermissionManager.grantPermissions();
            return;
        }
        Intent intentCollectorService = new Intent(mActivity, SparrowService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity.startForegroundService(intentCollectorService);
        } else mActivity.startService(intentCollectorService);
    }

    public void handlePermissionResult() {
        mPermissionManager.handlePermissionResult();
        startSparrowService();
    }

    public interface OnLicenseAgreementAcceptedListener {
        void onLicenseAgreementAccepted();
    }

    public void verifyLicenseAgreement(OnLicenseAgreementAcceptedListener listener) {
        if (hasUserAcceptedLicense()) {
            listener.onLicenseAgreementAccepted(); // License already accepted, notify listener
            return;
        }
        displayLicenseAgreementDialog(listener);
    }

    private boolean hasUserAcceptedLicense() {
        SharedPreferences preferences = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constants.LICENSE_ACCEPTED_KEY, false);
    }

    private void displayLicenseAgreementDialog(OnLicenseAgreementAcceptedListener listener) {
        mActivity.registerReceiver(onLicenseAcceptedListener, new IntentFilter(SparrowActions.ACTION_LICENSE_ACCEPTED));
        LicenseDialogFragment dialogFragment = new LicenseDialogFragment();
        dialogFragment.show(mActivity.getSupportFragmentManager(), "LicenseDialogFragment");
    }

    public SharedPreferences getSparrowPreferences() {
        return mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void onDestroy() {
        mActivity.unregisterReceiver(onLicenseAcceptedListener);
    }
}
