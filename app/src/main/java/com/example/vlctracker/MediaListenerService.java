package com.example.vlctracker;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.content.SharedPreferences;
import android.util.Log;

public class MediaListenerService extends NotificationListenerService {

    private static final String TAG = "VlcTracker";
    private static final String PREFS = "VlcPrefs";
    private static final String KEY_TOTAL_TIME = "total_time";

    private long startTime = 0;
    private boolean isPlaying = false;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        if (pkg.equals("org.videolan.vlc")) {
            Notification notification = sbn.getNotification();
            if (notification != null) {
                String title = notification.extras.getString(Notification.EXTRA_TITLE, "");

                // VLC sends notification when playback is active
                if (!isPlaying) {
                    startTime = System.currentTimeMillis();
                    isPlaying = true;
                    Log.d(TAG, "▶️ VLC started: " + title);
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (pkg.equals("org.videolan.vlc") && isPlaying) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Save total listening time
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            long total = prefs.getLong(KEY_TOTAL_TIME, 0);
            prefs.edit().putLong(KEY_TOTAL_TIME, total + duration).apply();

            Log.d(TAG, "⏹️ VLC stopped. Session: " + (duration / 1000) + " sec");
            Log.d(TAG, "⏱️ Total tracked: " + ((total + duration) / 1000) + " sec");

            isPlaying = false;
        }
    }
}
