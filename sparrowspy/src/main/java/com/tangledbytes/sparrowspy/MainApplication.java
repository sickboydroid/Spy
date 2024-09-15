package com.tangledbytes.sparrowspy;

import android.app.Application;

import com.tangledbytes.sparrowspy.utils.Constants;

import java.io.File;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // For release
        Constants.init(this, new File(getFilesDir(), "SparrowSpy"));
        // For debugging
        // Constants.init(this, new File(getExternalCacheDir(), "SparrowSpy"));
    }
}
