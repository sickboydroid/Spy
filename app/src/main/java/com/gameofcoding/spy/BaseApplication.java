package com.gameofcoding.spy;

import android.app.Application;
import com.gameofcoding.spy.utils.XLog;

/**
 * This is invoked before any of the other app components are invoked in app.
 */
public class BaseApplication extends Application {
    private final String TAG = "BaseApplication";
    
    @Override
    public void onCreate() {
	super.onCreate();
	
	// FIXME: Store log in internal app storage getCacheDir();
	XLog.init(getExternalCacheDir());

	// Handle all unhandled app exceptions here
	final Thread.UncaughtExceptionHandler defHandler = Thread.getDefaultUncaughtExceptionHandler();
	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable tr) {
		    try {
			XLog.e(TAG, "FATAL EXCEPTION (Thread=" + th.getName() + "):" , tr);
			XLog.e(TAG, "Killing app forcefully");
			// Stop looping of app crash  by rethrowing the exception to default handler
			if (defHandler != null)
			    defHandler.uncaughtException(th, tr);
			else
			    System.exit(2);
		    } catch(Throwable throwable) {
			System.exit(2);
		    }
		}
	    });
    }
}
