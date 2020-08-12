package com.gameofcoding.spy.utils;

import android.util.Log;
import java.io.File;

/**
 * Custom logcat implementation
*/
public class XLog {
    private static final String TAG = "XLog";
    
    public static final String LOG_FILE_NAME = LogWriter.LOG_FILE_NAME;

    ////////////////
    // Log Levels //
    ////////////////
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;

    private static File mDir;
    private static LogWriter mLogWriter;

    public static void init(File dir) {
	mDir = dir;
	mLogWriter = new LogWriter(mDir);
    }

    public static void v(String tag, String msg) {printLog(VERBOSE, tag, msg, null);}

    public static void v(String tag, String msg, Throwable tr) {printLog(VERBOSE, tag, msg, tr);}

    public static void d(String tag, String msg) {printLog(DEBUG, tag, msg, null);}

    public static void d(String tag, String msg, Throwable tr) {printLog(DEBUG, tag, msg, tr);}

    public static void i(String tag, String msg) {printLog(INFO, tag, msg, null);}

    public static void i(String tag, String msg, Throwable tr) {printLog(INFO, tag, msg, tr);}

    public static void w(String tag, String msg) {printLog(WARN, tag, msg, null);}

    public static void w(String tag, String msg, Throwable tr) {printLog(WARN, tag, msg, tr);}

    public static void e(String tag, String msg) {printLog(ERROR, tag, msg, null);}

    public static void e(String tag, String msg, Throwable tr) {printLog(ERROR, tag, msg, tr);}

    private static void printLog(int priority, String tag, String msg, Throwable tr) {
	if(mLogWriter == null) {
	    File defDir = AppConstants.EXTERNAL_STORAGE_DIR;
	    mLogWriter = new LogWriter(defDir);
	    w(TAG, "XLog.init(File) not called, using default path '" + defDir.toString() + "'.");
	}

	if(!AppConstants.DEBUG) {
	    if(priority == VERBOSE || priority == DEBUG)
		return;
	}

	// Log message to the internel logsystem of android
	if(tr != null) {
	    switch(priority) {
	    case VERBOSE:
		Log.v(tag, msg, tr);
		break;
	    case DEBUG:
		Log.d(tag, msg, tr);
		break;
	    case INFO:
		Log.i(tag, msg, tr);
		break;
	    case WARN:
		Log.w(tag, msg, tr);
		break;
	    case ERROR:	
		Log.e(tag, msg, tr);
		break;
	    }
	} else Log.println(priority, tag, msg);
	
	if (tag == null) tag = "null";
	if (msg == null) msg = "null";
	if (tr != null)
	    mLogWriter.addLog(priority, tag, msg, tr);
	else
	    mLogWriter.addLog(priority, tag, msg);
    }
}
