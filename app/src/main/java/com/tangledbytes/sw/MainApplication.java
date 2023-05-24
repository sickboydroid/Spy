package com.tangledbytes.sw;

import android.app.Application;
import android.os.Environment;

import com.tangledbytes.sw.utils.Constants;

import java.io.File;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Constants.init(new File(Environment.getExternalStorageDirectory(), "sparrow"));
        Constants.init(new File(getFilesDir(), "sparrow"));
    }
}