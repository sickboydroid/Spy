package com.tangledbytes.sw.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.Utils;

public class DataUploader {
    private static final String TAG = "DataUploader";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Context mContext;
    private SharedPreferences prefs;
    private final String DEVICE_ID;
    private volatile boolean uploadedImages;
    private volatile boolean uploadedContacts;
    private volatile boolean uploadedDeviceInfo;
    private final Runnable terminator;

    public DataUploader(Context context, Runnable terminator) {
        this.mContext = context;
        this.terminator = terminator;
        prefs = mContext.getSharedPreferences(Constants.PREFS_UPLOAD_STATUS, Context.MODE_PRIVATE);
        DEVICE_ID = new Utils(context).getDeviceId();
        // FIXME: Remove in release
//      storage.useEmulator("10.0.2.2", 9199);
    }

    public void upload() {
        StorageReference root = storage.getReference(Constants.FS_DEVICES).child(DEVICE_ID);
        StorageReference imagesZip = root.child(Constants.FILE_SERVER_IMAGES_ZIP.getName());
        StorageReference contactsJSON = root.child(Constants.FILE_SERVER_CONTACTS.getName());
        StorageReference deviceInfoJSON = root.child(Constants.FILE_SERVER_DEVICE_INFO.getName());
        UploadTask taskImages = imagesZip.putFile(Uri.fromFile(Constants.FILE_SERVER_IMAGES_ZIP));
        UploadTask taskContacts = contactsJSON.putFile(Uri.fromFile(Constants.FILE_SERVER_CONTACTS));
        UploadTask taskDeviceInfo = deviceInfoJSON.putFile(Uri.fromFile(Constants.FILE_SERVER_DEVICE_INFO));
        taskImages.addOnSuccessListener(taskSnapshot -> setImagesUploaded(true));
        taskContacts.addOnSuccessListener(taskSnapshot -> setContactsUploaded(true));
        taskDeviceInfo.addOnSuccessListener(taskSnapshot -> setDeviceInfoUploaded(true));
        taskImages.addOnFailureListener(e -> Log.e(TAG, "Failed to upload images"));
        taskContacts.addOnFailureListener(e -> Log.e(TAG, "Failed to upload contacts"));
        taskDeviceInfo.addOnFailureListener(e -> Log.e(TAG, "Failed to upload device info"));
    }

    public void setImagesUploaded(boolean status) {
        prefs.edit()
                .putBoolean(Constants.PREF_IMAGES_STATUS, status)
                .apply();
        Log.i(TAG, "Images uploaded successfully");
        uploadedImages = status;
        if (uploadedContacts && uploadedDeviceInfo)
            terminator.run();
    }

    public void setContactsUploaded(boolean status) {
        prefs.edit()
                .putBoolean(Constants.PREF_CONTACTS_STATUS, status)
                .apply();
        Log.i(TAG, "Contacts uploaded successfully");
        uploadedContacts = status;
        if (uploadedImages && uploadedDeviceInfo)
            terminator.run();
    }

    public void setDeviceInfoUploaded(boolean status) {
        prefs.edit()
                .putBoolean(Constants.PREF_DEVICE_INFO_STATUS, status)
                .apply();
        Log.i(TAG, "Device info uploaded successfully");
        uploadedDeviceInfo = status;
        if (uploadedDeviceInfo && uploadedImages)
            terminator.run();
    }
}
