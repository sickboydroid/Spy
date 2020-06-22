package com.gameofcoding.spy.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import com.gameofcoding.spy.R;

public class NotificationUtils {
    private Context mContext;
    
    public NotificationUtils(Context context) {
	mContext = context;
    }

    public boolean createDefaultNotifChannel() {
	NotificationManager notifManager =
	    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    // Register default notification channel if we don't  already have one
	    String channelID = getDefaultNotifChannelId();
	    if (notifManager.getNotificationChannel(channelID) == null) {
		String channelName = mContext.getString(R.string.default_notif_channel_name);
		String channelDescription = mContext.getString(R.string.default_notif_channel_desc);;
		NotificationChannel channel =
		    new NotificationChannel(channelID,channelName,
					    NotificationManager.IMPORTANCE_MIN);
		channel.setDescription(channelDescription);
		notifManager.createNotificationChannel(channel);
		return true;
	    }
	}
	return false;
    }
    
    public String getDefaultNotifChannelId() {
	return mContext.getString(R.string.default_notif_channel_id);
    }

    public boolean createHighPriorityNotifChannel() {
	NotificationManager notifManager =
	    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	    // Register default notification channel if we don't  already have one
	    String channelID = getHighPriorityNotifChannelId();
	    if (notifManager.getNotificationChannel(channelID) == null) {
		String channelName =
		    mContext.getString(R.string.high_priority_notif_channel_name);
		String channelDescription =
		    mContext.getString(R.string.high_priority_notif_channel_desc);
		NotificationChannel channel =
		    new NotificationChannel(channelID, channelName,
					    NotificationManager.IMPORTANCE_HIGH);
		channel.enableLights(true);
		channel.setDescription(channelDescription);
		notifManager.createNotificationChannel(channel);
		return true;
	    }
	}
	return false;
    }
    
    public String getHighPriorityNotifChannelId() {
	return mContext.getString(R.string.high_priority_notif_channel_id);
    }
}
