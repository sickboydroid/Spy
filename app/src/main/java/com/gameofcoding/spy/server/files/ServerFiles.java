package com.gameofcoding.spy.server.files;

import android.content.Context;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.server.ServerManager;
import java.io.File;

public class ServerFiles implements Files {
    private static final String TAG = "ServerFiles";
    public static final String DIR_COMMANDS = "cmds";
    public static String ROOT_DIR_NAME = "serverRootDir";
    private static ServerManager mServerManager;

    private ServerFiles() {
    }

    public static ServerFiles loadFiles(Context context) {
	if(context == null)
	    return null;
	// TODO: Change getExternalFilesDir() > getFilesdir()
	File serverRootDir = new File(context.getExternalFilesDir(null), ROOT_DIR_NAME);
	mServerManager = new ServerManager(serverRootDir, new Utils(context).generateDeviceId());
	if(mServerManager.loadServer(true) != null)
	    return new ServerFiles();
	XLog.e(TAG, "loadFiles(Context): Server dir could not be loaded, aborting and returning null.");
	return null;
    }

    @Override
    public File getRootDir() {
	if(mServerManager == null)
	    return null;
	return mServerManager.getServerRootDir();
    }

    public File getCommandsDir() {
    	File dirCommands = new File(getRootDir(), DIR_COMMANDS);
	if(dirCommands.exists())
	    return dirCommands;
	else if(dirCommands.mkdir())
	    return dirCommands;
	XLog.e(TAG, "getCommandsDir(Context): Could not create 'commands' directory, dirCommands=" + dirCommands);
	return null;
    }
}
