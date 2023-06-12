package com.tangledbytes.sparrowspy.utils;

import android.content.SharedPreferences;

import com.tangledbytes.sparrowspy.events.Events;

public abstract class SpyState {
    public static class Listeners {
        public static Events.SpyServiceStateChangeListener spyServiceStateChangeListener;
        public static Events.DataCollectionListener dataCollectionListener;
        public static Events.DataUploadListener dataUploadListener;
        public static Events.PermissionsListener permissionsListener;
    }
}
