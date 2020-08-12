package com.gameofcoding.spy.utils;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <b>NOTE: Don't do any logging in this class as it may send application in infinite loop</b>
*/
final class LogWriter {
    public static final String LOG_FILE_NAME = "appLog.log";

    private static final String SEPARATOR = " ";
    private static File mLogFile;

    public LogWriter(File dir) {
	mLogFile = new File(dir, LOG_FILE_NAME);
    }

    public void addLog(int priority, String tag, String msg) {
	addLog(priority, tag, msg, null);
    }

    public void addLog(int priority, String tag, String msg, Throwable tr) {
	StringBuilder logLine = new StringBuilder();
	try {
	    // Parse log date and time
	    DateFormat dateFormat = new SimpleDateFormat("dd-MM HH:mm:ss.SSS", Locale.getDefault());
	    logLine.append(dateFormat.format(new Date()));

	    // Parse priority
	    String prioritySymbol = null;
	    switch (priority) {
	    case XLog.VERBOSE:
		prioritySymbol = "V";
		break;
	    case XLog.ERROR:
		prioritySymbol = "E";
		break;
	    case XLog.INFO:
		prioritySymbol = "I";
		break;
	    case XLog.WARN:
		prioritySymbol = "W";
		break;
	    case XLog.DEBUG:
		prioritySymbol = "D";
		break;
	    }
	   logLine.append(SEPARATOR);
	     logLine.append(prioritySymbol);

	    // Parse tag
	    if(tag.length() > 20)
		tag = tag.substring(0, 9) + ".." + tag.substring(tag.length() - 9, tag.length());
	    else if(tag.length() < 20) {
		while(tag.length() != 20)
		    tag += " ";
	    }
	    logLine.append(SEPARATOR);
	    logLine.append(tag);

	    if(AppConstants.EXTREME_LOGGING) {
		// Parse class name, method name, file name and line number
		logLine.append(SEPARATOR);
		StackTraceElement stackElement = getStackTraceElement();
		String className = stackElement.getClassName();
		String methodName = stackElement.getMethodName();
		String fileName = stackElement.getFileName();
		int lineNumber = stackElement.getLineNumber();
		if(className.contains("."))
		    className = className.substring(className.lastIndexOf(".") + 1);
		logLine.append(className);
		logLine.append(".");
		logLine.append(methodName);
		logLine.append("(");
		logLine.append(fileName);
		logLine.append(":");
		logLine.append(lineNumber);
		logLine.append(")");
		logLine.append(" ");
	    }

	    // Parse message
	    logLine.append(SEPARATOR);
	    logLine.append(msg);

	    // Parse exception, if has
	    if (tr != null) {
		logLine.append("\n-> ");
		String stackTrace = Log.getStackTraceString(tr);
		if(stackTrace.isEmpty())
		    logLine.append("UnknownHostexception ... (Suppressed)");
		else
		    logLine.append(Log.getStackTraceString(tr));
	    }

	    // Add new line so that future logs would be added in new line
	    logLine.append("\n");

	    // Add above log to previously stored logs in external storage
	    FileUtils.write(mLogFile, logLine.toString(), true);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static StackTraceElement getStackTraceElement() {
	return ___01234567890_qwerty_uiop_ASDF_ghjkl_0987654321();
    }

    /*
     * Name of this method should be unique from all the other methods
     * Returns the StackTraceElement of the line which called XLog.x(String, String);
     */
    private static StackTraceElement ___01234567890_qwerty_uiop_ASDF_ghjkl_0987654321() {
	StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
	for(int i = 0; i < stacks.length; i++) {
	    StackTraceElement stack = stacks[i];
	    if(stack.getMethodName().equals("___01234567890_qwerty_uiop_ASDF_ghjkl_0987654321")) {
		return stacks[i + 6];
	    }
	}
	return null;
    }
}
