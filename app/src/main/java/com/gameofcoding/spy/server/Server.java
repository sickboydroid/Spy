package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Main server class. This class handles all type of stdin and stdout process
 */
public class Server {
    private static String TAG = "Server";
    public static enum Dir {
	MAIN,
	APP_DATA,
	DEVICE
    };
    private String mDeviceGitBranchName;
    private RepoManager mGitRepo;

    Server(RepoManager gitRepo, String deviceGitBranchName) {
	mGitRepo = gitRepo;
	mDeviceGitBranchName = deviceGitBranchName;
    }

    public Directory openDir(Dir dir) throws GitAPIException {
	XLog.v(TAG, "openDir(Directory): Opening dir [dir=" + dir + "]");
	boolean hasCheckedOut = false;
	switch(dir) {
	case MAIN:
	    hasCheckedOut = mGitRepo.checkout(RepoManager.LOCAL_BRANCH_SUFFIX + RepoManager.BRANCH_MASTER);
	case APP_DATA:
	    hasCheckedOut = mGitRepo.checkout(RepoManager.REMOTE_BRANCH_SUFFIX + RepoManager.BRANCH_APP_DATA);
	case DEVICE:
	    hasCheckedOut = mGitRepo.checkout(mDeviceGitBranchName);
	}
	if(hasCheckedOut)
	    return new Directory(mGitRepo);
	else
	    return null;
    }

    public boolean reloadData() throws GitAPIException {
	// Pull master and app_data
	openDir(Dir.MAIN);
	mGitRepo.pullRemoteBranch(RepoManager.BRANCH_MASTER);
	openDir(Dir.APP_DATA);
	mGitRepo.pullRemoteBranch(RepoManager.BRANCH_APP_DATA);
	return true;
    }
	
    public static class Directory {
	private static final String TAG = "Directory";
	private RepoManager mGitRepo;
	    
	private Directory(RepoManager gitRepo) {
	    mGitRepo = gitRepo;
	}
	    
	public boolean saveChanges() throws GitAPIException {
	    // Commit changes
	    return mGitRepo.commit("spy_server");
	}
	    
	public boolean uploadData() throws GitAPIException {
	    // Commit and push changes
	    if(saveChanges())
		if(mGitRepo.push())
		    return true;
	    return false;
	}
    }
}
