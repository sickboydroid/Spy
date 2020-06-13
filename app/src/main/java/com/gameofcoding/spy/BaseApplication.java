package com.gameofcoding.spy;

import android.app.Application;

import java.io.File;

import com.gameofcoding.spy.utils.XLog;

public class BaseApplication extends Application {
    private final String TAG = "BaseApplication";
    @Override
    public void onCreate() {
	super.onCreate();
	XLog.init(new File("/sdcard/SickBoyDir/temp"));
	final Thread.UncaughtExceptionHandler defHandler = Thread
	    .getDefaultUncaughtExceptionHandler();
	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable tr) {
		    try {
			XLog.e(TAG, "Uncaught exception.", tr);
			XLog.e(TAG, "Killing app forcebly");
			// Stop looping of app by rethrowing the excep to default handler
			if (defHandler != null) {
			    defHandler.uncaughtException(th, tr);
			}
			else {
			    System.exit(2);
			}
		    }
		    catch(Throwable trowable) {
			System.exit(2);
		    }
		}
	    });
    }
}
