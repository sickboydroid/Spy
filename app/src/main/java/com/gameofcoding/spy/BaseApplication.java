package com.gameofcoding.spy;

import android.app.Application;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

public class BaseApplication extends Application {
    private final String TAG = "BaseApplication";
    @Override
    public void onCreate() {
	super.onCreate();
	XLog.init(new File(AppConstants.LOG_FILE_PATH));

	// Handle all app exceptions here
	final Thread.UncaughtExceptionHandler defHandler = Thread.getDefaultUncaughtExceptionHandler();
	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable tr) {
		    try {
			XLog.e(TAG, "Uncaught exception.", tr);
			XLog.e(TAG, "Killing app forcebly");
			// Stop looping of app crash  by rethrowing the exception to default handler
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
