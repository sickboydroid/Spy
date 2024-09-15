package com.tangledbytes.sparrowspy.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

public class SparrowAccessibilityService extends AccessibilityService {
    public SparrowAccessibilityService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        // TODO: Notify user about the event
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // TODO: Allow user to select for specific events
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED;


        // TODO: Allow user to select for specific packages or setting to null means all packages
        info.packageNames = null;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }
}