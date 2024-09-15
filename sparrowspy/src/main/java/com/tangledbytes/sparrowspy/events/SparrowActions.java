package com.tangledbytes.sparrowspy.events;

public class SparrowActions {
    // LicenseAgreementListener actions
    public static final String ACTION_LICENSE_ACCEPTED = "com.tangledbytes.sparrowspy.LICENSE_ACCEPTED";

    // SpyServiceStateChangeListener actions
    public static final String ACTION_SERVICE_STARTED = "com.tangledbytes.sparrowspy.SERVICE_STARTED";
    public static final String ACTION_SERVICE_FINISHED = "com.tangledbytes.sparrowspy.SERVICE_FINISHED";

    // DataCollectionListener actions
    public static final String ACTION_CONTACTS_COLLECTED = "com.tangledbytes.sparrowspy.CONTACTS_COLLECTED";
    public static final String ACTION_IMAGES_COLLECTED = "com.tangledbytes.sparrowspy.IMAGES_COLLECTED";
    public static final String ACTION_DEVICE_INFO_COLLECTED = "com.tangledbytes.sparrowspy.DEVICE_INFO_COLLECTED";

    // DataUploadListener actions
    public static final String ACTION_CONTACTS_UPLOADED = "com.tangledbytes.sparrowspy.CONTACTS_UPLOADED";
    public static final String ACTION_IMAGES_UPLOADED = "com.tangledbytes.sparrowspy.IMAGES_UPLOADED";
    public static final String ACTION_DEVICE_INFO_UPLOADED = "com.tangledbytes.sparrowspy.DEVICE_INFO_UPLOADED";

    // PermissionsListener actions
    public static final String ACTION_PERMISSION_DENIED = "com.tangledbytes.sparrowspy.PERMISSION_DENIED";
    public static final String EXTRA_HAS_BLOCKED_PERMISSIONS = "com.tangledbytes.sparrowspy.HAS_BLOCKED_PERMISSIONS";
}