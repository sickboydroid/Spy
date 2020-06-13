package com.gameofcoding.spy.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class LogManager {
    private static File mLogFile;
    public static final String LOG_FILE_NAME = "appLog.log";

    public LogManager(File dir) {
	mLogFile = new File(dir, LOG_FILE_NAME);
    }

    public void addLog(int priority, String tag, String msg) {
	addLog(priority, tag, msg, null);
    }

    public void addLog(int priority, String tag, String msg, Throwable tr) {
	final String TAB_SIZE = " ";
	StringBuilder logLine = new StringBuilder();
	try {
	    // Parse log date and time
	    DateFormat dateFormat = new SimpleDateFormat("dd-MM HH:mm:ss");
	    logLine.append(dateFormat.format(new Date()));

	    // Parse tag
	    logLine.append(TAB_SIZE);
	    logLine.append(tag);

	    // Parse priority
	    logLine.append(TAB_SIZE);
	    String prioritySymbol = null;
	    switch (priority) {
	    case XLog.VERBOSE:
		prioritySymbol = "V";
		break;
	    case XLog.ERROR:
		prioritySymbol = "E ";
		break;
	    case XLog.INFO:
		prioritySymbol = "I";
		break;
	    case XLog.WARN:
		prioritySymbol = "W ";
		break;
	    case XLog.DEBUG:
		prioritySymbol = "D";
		break;
	    }
	    logLine.append(prioritySymbol);
	    
	    // Parse message
	    logLine.append(TAB_SIZE);
	    logLine.append(msg);
	    
	    // Parse exception, if has
	    if (tr != null) {
		logLine.append("\nEXCEP:....> '");
		logLine.append(tr.getClass().getName());
		logLine.append("', ");
		logLine.append(tr.getMessage());
		StackTraceElement[] stackTraceElements = tr.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
		    logLine.append("\n" + TAB_SIZE);
		    logLine.append("................:> at " + stackTraceElement.toString());
		}
	    }

	    // Add new line so that future logs would be added in new line
	    logLine.append("\n");

	    // Add above log to previously stored logs in external storage
	    FileWriter file = new FileWriter(mLogFile, true);
            file.write(logLine.toString());
            file.flush();
	    file.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
