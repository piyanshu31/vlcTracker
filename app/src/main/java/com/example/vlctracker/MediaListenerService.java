package com.example.vlctracker;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.util.Log;

public class MediaListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        // Log every notification
        Log.d("VlcTracker", "Notification from: " + pkg);

        if (pkg.equals("org.videolan.vlc")) {
            Log.d("VlcTracker", "🎵 VLC notification detected!");

            Notification notification = sbn.getNotification();
            if (notification != null) {
                Bundle extras = notification.extras;
                String title = extras.getString(Notification.EXTRA_TITLE);
                String text = extras.getString(Notification.EXTRA_TEXT);

                Log.d("VlcTracker", "Now playing: " + title + " - " + text);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("VlcTracker", "Notification removed: " + sbn.getPackageName());
    }
}
