package com.tangledbytes.sparrowspy.server;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.tangledbytes.sparrowspy.utils.Constants;

public class FirebaseManager {
    private static final String ANDROID_LOCALHOST = "10.0.2.2";
    public static FirebaseStorage getFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if(Constants.Debug.DEBUG && Constants.Debug.USE_FIREBASE_EMULATOR)
            storage.useEmulator(ANDROID_LOCALHOST, Constants.Debug.FBE_STORAGE_PORT);
        return storage;
    }
}
