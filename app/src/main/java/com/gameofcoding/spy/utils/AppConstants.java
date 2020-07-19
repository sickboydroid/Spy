package com.gameofcoding.spy.utils;

import android.Manifest;

public abstract class AppConstants {
    public static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final String[] PERMISSIONS_NEEDED =  {
	Manifest.permission.WRITE_EXTERNAL_STORAGE,
	Manifest.permission.READ_CONTACTS
    };
    public static final String ACTION_INSTALL_APP_UPDATE = "action_install_app_update";
    public static abstract class preference {
	public static final String APP_PREFS = "spy_prefs";
	public static final String DEVICE_ID = "device_id";
    }
}
