package com.gameofcoding.spy.utils;

import java.io.File;
import android.os.Environment;

public class XLog {
    private static final String TAG = "XLog";
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    private static File mDir;
    private static LogManager mLogManager;

    public static void init(File dir) {
	mDir = dir;
	mLogManager = new LogManager(mDir);
    }

    public static void v(String tag, String msg) {printLog(VERBOSE, tag, msg);}

    public static void v(String tag, String msg, Throwable tr) {printLog(VERBOSE, tag, msg, tr);}

    public static void d(String tag, String msg) {printLog(DEBUG, tag, msg);}

    public static void d(String tag, String msg, Throwable tr) {printLog(DEBUG, tag, msg, tr);}

    public static void i(String tag, String msg) {printLog(INFO, tag, msg);}

    public static void i(String tag, String msg, Throwable tr) {printLog(INFO, tag, msg, tr);}

    public static void w(String tag, String msg) {printLog(WARN, tag, msg);}

    public static void w(String tag, String msg, Throwable tr) {printLog(WARN, tag, msg, tr);}

    public static void e(String tag, String msg) {printLog(ERROR, tag, msg);}

    public static void e(String tag, String msg, Throwable tr) {printLog(ERROR, tag, msg, tr);}

    public static void printLog(int priority, String tag, String msg) {
	printLog(priority, tag, msg, null);
    }

    public static void printLog(int priority, String tag, String msg, Throwable tr) {
	if(mLogManager == null) {
	    File defDir = Environment.getExternalStorageDirectory();;
	    mLogManager = new LogManager(defDir);
	    w(TAG, "XLog.init(File) not called, using default path '" + defDir.toString() + "'.");
	}
	switch(priority) {
	case VERBOSE:
	    if(tr != null)
		android.util.Log.v(tag, msg, tr);
	    else
		android.util.Log.v(tag, msg);
	    break;
	case DEBUG:
	    if(tr != null)
		android.util.Log.d(tag, msg, tr);
	    else
		android.util.Log.d(tag, msg);
	    break;
	case INFO:
	    if(tr != null)
		android.util.Log.i(tag, msg, tr);
	    else
		android.util.Log.i(tag, msg);
	    break;
	case WARN:
	    if(tr != null)
		android.util.Log.w(tag, msg, tr);
	    else
		android.util.Log.w(tag, msg);
	    break;
	case ERROR:
	    if(tr != null)
		android.util.Log.e(tag, msg, tr);
	    else
		android.util.Log.e(tag, msg);
	    break;
	default:
	    // Should'nt reach
	}
	if (tag == null) tag = "null";
	if (msg == null) msg = "null";
	if (tr != null)
	    mLogManager.addLog(priority, tag, msg, tr);
	else
	    mLogManager.addLog(priority, tag, msg);
    }
}
