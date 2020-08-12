package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.text.Spanned;
import android.text.Html;
import android.widget.TextView;
import android.os.Build;

/**
 * Activity which is showed in the form of a dialog.
 * User installs the update from this activity.
 */
public class AppUpdateInstallerActivity extends Activity {
    private static final String TAG = "AppUpdateInstallerActivity";
    /*
     * Broadcast action. Send when app gets updated or fails to update
     */
    private final String PACKAGE_INSTALLED_ACTION = "com.gameofcoding.spy.package_installed";
    private final Context mContext = this;

    /*
     * Broadcast receiver for receiving statsu of app update.
     */
    class StatusReciever extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    Bundle extras = intent.getExtras();
	    if (PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
		int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
		String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
		switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This app isn't privileged to install apps, so the user has to confirm the
		    // install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    context.startActivity(confirmIntent);
                    break;
                case PackageInstaller.STATUS_SUCCESS:
		    // App successfully updated
                    Toast.makeText(context, R.string.app_update_success, Toast.LENGTH_SHORT).show();
		    XLog.i(TAG, "App successfully updated!");
                    break;
                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
		    // Failed due to some internal failure
                    Toast.makeText(context, R.string.app_update_failed, Toast.LENGTH_SHORT).show();
		    XLog.i(TAG, "Failed to update app, status " + status + ", message='"
			   + message + "'");
                    break;
                default:
		    XLog.i(TAG, "Unrecognised status receiver from installer: " + status);
		}
                finish();
	    }
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	XLog.v(TAG, "AppUpdateInstalleractivity started.");
        setContentView(R.layout.activity_app_update_installer);

	// Initialize views
        TextView tvWhatIsNew = findViewById(R.id.what_is_new);
	Button btnCancel = findViewById(R.id.cancel);
	Button btnInstallAppUpdate = findViewById(R.id.update_app);

	// Register receiver for receiving installation status
	IntentFilter filter = new IntentFilter();
	filter.addAction(PACKAGE_INSTALLED_ACTION);
	registerReceiver(new StatusReciever(), filter);

	// Load apk path from intent
        Bundle extras = getIntent().getExtras();
	String apkPath = null;
        String versionName = null;
        String[] whatIsNew = null;
        if(getIntent().hasExtra(AppConstants.EXTRA_APP_PATH)) 
            apkPath = extras.getString(AppConstants.EXTRA_APP_PATH);
        if(getIntent().hasExtra(AppConstants.EXTRA_VERSION_NAME))
            versionName = extras.getString(AppConstants.EXTRA_VERSION_NAME);
        if(getIntent().hasExtra(AppConstants.EXTRA_WHAT_IS_NEW))
            whatIsNew = extras.getStringArray(AppConstants.EXTRA_WHAT_IS_NEW);

	// Check whether the app exists or not
	final File apkFile = new File(apkPath);
	if(!apkFile.exists()) {
	    XLog.e(TAG, "Provided apk file doesn't exist, file " + apkFile);
	    finish();
	    return;
	}

        // Format and show information about latest version in text view
        tvWhatIsNew.setText(formatWhatIsNew(whatIsNew, versionName));
        
	// Watch for button clicks
	btnCancel.setOnClickListener((View view) -> {
		finish();
	    });
	btnInstallAppUpdate.setOnClickListener((View view) -> {
		// Check whether the app exists or not
		if(!apkFile.exists()) {
		    XLog.w(TAG, "Apk file does not exist, aborting installtion. apkFile: "
			   + apkFile.getAbsolutePath());
		    finish();
		    return;
		}

		// Prompt user to install app
		XLog.d(TAG, "Prompting for installation");
		PackageInstaller.Session session = null;
		try {
		    PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
		    PackageInstaller.SessionParams params =
			new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
		    int sessionId = packageInstaller.createSession(params);
		    session = packageInstaller.openSession(sessionId);
		    addApkToInstallSession(apkFile, session);

		    // Create an install status receiver.
		    Intent intent = new Intent(PACKAGE_INSTALLED_ACTION);
		    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

		    // Commit the session (this will start the installation workflow).
		    session.commit(pendingIntent.getIntentSender());
		} catch (Exception e) {
		    if (session != null)
			session.abandon();
		    XLog.e(TAG, "Exception occurred while installing update for application", e);
		}
	    });
    }

    private void addApkToInstallSession(File apkFile, PackageInstaller.Session session)
	throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
	     InputStream is = new FileInputStream(apkFile) ) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Spanned formatWhatIsNew(String[] whatIsNew, String latestVersionName) {
        String versionComparison = null;
        try {
            String currentVersionName = new Utils(this).getVersionName();
            versionComparison = String.format(getString(R.string.version_comparison),
                                              "<b>" + currentVersionName + "</b>",
                                              "<b>" + latestVersionName + "</b>");
        } catch(PackageManager.NameNotFoundException e) {
            XLog.e(TAG, "Exception occurred while getting version name of application", e);
            versionComparison = "(NaN -> NaN)";
        }
        final StringBuilder htmlWhatIsNew = new StringBuilder();
        // Append 'what is new' title
        htmlWhatIsNew
                .append("<b>")
                .append(getString(R.string.title_what_is_new))
                .append("</b> <i>")
                .append(versionComparison)
                .append("</i><br>");
        for(int i = 0; i < whatIsNew.length; i++) {
            // Append line number
            htmlWhatIsNew
                    .append("<b>")
                    .append(i + 1)
                    .append(": </b>");
            // Append line
            htmlWhatIsNew
                    .append(whatIsNew[i])
                    .append("<br>");
        }
        Spanned spannedWhatIsNew = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannedWhatIsNew = Html.fromHtml(htmlWhatIsNew.toString(), Html.FROM_HTML_MODE_LEGACY);
        else
            spannedWhatIsNew = Html.fromHtml(htmlWhatIsNew.toString());
        return spannedWhatIsNew;
    }
}
