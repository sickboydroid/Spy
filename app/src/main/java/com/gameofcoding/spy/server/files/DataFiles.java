package com.gameofcoding.spy.server.files;

import android.content.Context;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

/**
 * Helps in managing paths of different files within data files.
*/
public class DataFiles implements Files {
    private static final String TAG = "DataFilePaths";
    private static final String DIR_ROOT = "appData";
    private static final String DIR_USER_DATA = "userData";
    private static final String DIR_OTHERS = "others";
    private final Context mContext;

    /** @hide */
    private DataFiles(Context context) {
	mContext = context;
    }

    /**
     * Returns the object of this class
     */
    public static DataFiles loadFiles(Context context) {
	if(context == null)
	    return null;
	return new DataFiles(context);
    }

    /**
     * Returns the root directory of data files.
     */
    @Override
    public File getRootDir() {
	File dirRoot = new File(Utils.getFilesDir(mContext), DIR_ROOT);
	if(dirRoot.exists())
	    return dirRoot;
	else if(dirRoot.mkdir())
	    return dirRoot;
	XLog.e(TAG, "getRootDir(Context): Could't create '" + dirRoot.getName()
	       + "' directory, dirRoot=" + dirRoot);
	return null;
    }

    /**
     * Returns the directory user data gets stored
     */
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

    /**
     * Returns the diretory where data (other than user's) is stored.
     */
    public File getOthersDir() {
	File dirOthers = new File(getRootDir(), DIR_OTHERS);
	if(dirOthers.exists())
	    return dirOthers;
	else if(dirOthers.mkdir())
	    return dirOthers;
	XLog.e(TAG, "getOthersDir(Context): Could't create '" + dirOthers.getName()
	       + "' directory, dirOthers=" + dirOthers);
	return null;
    }
}
