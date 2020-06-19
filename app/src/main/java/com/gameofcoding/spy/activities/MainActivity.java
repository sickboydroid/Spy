package com.gameofcoding.spy.activities;

import android.app.Activity;
import android.os.Bundle;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.utils.XLog;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	XLog.v(TAG, "App Started through main activity");
	// final Utils util = new Utils(this);
	// new Thread(new Runnable() {
	// 	public void run() {
	// 	    try {
	// 	    } catch(Exception e) {
	// 		throw new RuntimeException(e);
	// 	    }
	// 	}
	//     }).start();
    }
}
