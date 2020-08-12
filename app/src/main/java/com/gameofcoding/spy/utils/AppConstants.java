package com.gameofcoding.spy.utils;

import android.Manifest;
import java.io.File;
import android.os.Environment;;

public abstract class AppConstants {
    /////////////////////////
    // Package name of app //
    /////////////////////////
    public static final String PACKAGE_NAME = "com.gameofcoding.spy;";

    ///////////////////////////////////////
    // Constants for controlling logging //
    ///////////////////////////////////////
    public static final boolean DEBUG = true;
    public static final boolean EXTREME_LOGGING = false;

    /////////////////////////////
    // Extras passed to intent //
    /////////////////////////////
    public static final String EXTRA_APP_PATH = "extra_app_path";
    public static final String EXTRA_VERSION_NAME = "extra_version_name";
    public static final String EXTRA_WHAT_IS_NEW = "extra_what_is_new";

    //////////////////////////
    // File names and paths //
    //////////////////////////
    @SuppressWarnings("deprecation")
    public static final File EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory();
    public static final String FILE_DEVICE_ID = ".deviceid";
    public static final String APP_FILE_NAME = "spy.apk";
    public static final String APP_INFO_FILE_NAME = "appInfo.json";

    ////////////////////////
    // Permissions Needed //
    ////////////////////////
    public static final String[] PERMISSIONS_NEEDED =  {
	Manifest.permission.WRITE_EXTERNAL_STORAGE,
	Manifest.permission.READ_CONTACTS
    };

    ///////////////////////////////
    // BroadCastReceiver actions //
    ///////////////////////////////
    public static final String ACTION_INSTALL_APP_UPDATE = "action_install_app_update";

    /////////////////////
    // Preference keys //
    /////////////////////
    public static final class preference {
	private preference() {}
	
	public static final String APP_PREFS = "spy_prefs";
	public static final String DEVICE_ID = "device_id";
    }
}
