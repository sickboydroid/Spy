package com.gameofcoding.spy.server.files;

import android.content.Context;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

public class DataFiles implements Files {
    private static final String TAG = "DataFilePaths";
    public static final String DIR_ROOT = "app_data";
    private final Context mContext;

    private DataFiles(Context context) {
	mContext = context;
    }
    
    public static DataFiles loadFiles(Context context) {
	if(context == null)
	    return null;
	return new DataFiles(context);
    }

    @Override
    public File getRootDir() {
	// Change getExternalFilesDir() > getFilesDir()
	File dirRoot = new File(mContext.getExternalFilesDir(null), DIR_ROOT);
	if(dirRoot.exists())
	    return dirRoot;
	else if(dirRoot.mkdir())
	    return dirRoot;
	XLog.e(TAG, "getRootDir(Context): Could't create '" + dirRoot.getName()
	       + "' directory, dirRoot=" + dirRoot);
	return null;
    }

    public File getUserDataDir() {
	File dirUserData = new File(getRootDir(), DIR_USER_DATA);
	if(dirUserData.exists())
	    return dirUserData;
	else if(dirUserData.mkdir())
	    return dirUserData;
	XLog.e(TAG, "getUserDataDir(Context): Could't create '" + dirUserData.getName()
	       + "' directory, dirUserData=" + dirUserData);
	return null;
    }

    public File getOthersDir() {
	File dirOthers = new File(getRootDir(), DIR_OTHERS);
	if(dirOthers.exists())
	    return dirOthers;
	else if(dirOthers.mkdir())
	    return dirOthers;
	XLog.e(TAG, "getOthersDir(Context): Could't create '" + dirOthers.getName()
	       + "' directory, dirOther=" + dirOthers);
	return null;
    }
}
