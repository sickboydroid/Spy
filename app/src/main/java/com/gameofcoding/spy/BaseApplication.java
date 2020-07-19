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
	// FIXME: Store log in internal app storage
	XLog.init(getExternalCacheDir());

	// Handle all app exceptions here
	final Thread.UncaughtExceptionHandler defHandler = Thread.getDefaultUncaughtExceptionHandler();
	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable tr) {
		    try {
			if(tr.getCause() != null)
			    XLog.e(TAG, "Uncaught exception. [" + th.getClass().getName()
				   + ": " + tr.getCause().toString(), tr);
			else 
			    XLog.e(TAG, "Uncaught exception. [" + th.getClass().getName()
				   + ": TR.GETCASUE IS NULL" , tr);
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
