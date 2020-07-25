package com.gameofcoding.spy.io;

import android.content.Context;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

public class DataFilePaths implements FilePaths {
    private static final String TAG = "DataFilePaths";
    public static final String DIR_ROOT = "app_data";
    private final Context mContext;

    private DataFilePaths(Context context) {
	mContext = context;
    }
    
    public static DataFilePaths loadPaths(Context context) {
	if(context == null)
	    return null;
	return new DataFilePaths(context);
    }

    @Override
    public File getRootDir() {
	// Change getExternalFilesDir() > getFilesDir()
	File dirRoot = new File(mContext.getExternalFilesDir(null), DIR_ROOT);
	if(dirRoot.exists())
	    return dirRoot;
	else if(dirRoot.mkdir())
	    return dirRoot;
	XLog.e(TAG, "getRootDir(Context): Could not create 'root directory', dirRoot=" + dirRoot);
	return null;
    }

    public File getUserDataDir() {
	File dirUserData = new File(getRootDir(), DIR_USER_DATA);
	if(dirUserData.exists())
	    return dirUserData;
	else if(dirUserData.mkdir())
	    return dirUserData;
	XLog.e(TAG, "getUserDataDir(Context): Could not create 'user data' directory, dirUserData=" + dirUserData);
	return null;
    }

    public File getOthersDir() {
	File dirOthers = new File(getRootDir(), DIR_OTHERS);
	if(dirOthers.exists())
	    return dirOthers;
	else if(dirOthers.mkdir())
	    return dirOthers;
	XLog.e(TAG, "getOthersDir(Context): Could not create 'others' directory, dirOther=" + dirOthers);
	return null;
    }
}
