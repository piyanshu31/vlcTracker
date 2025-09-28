package com.example.vlctracker;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Handler;

public class MediaListenerService extends NotificationListenerService {

    private static int listeningTime = 0; // in seconds
    private static boolean isPlaying = false;
    private Handler handler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                listeningTime++;
                handler.postDelayed(this, 1000); // count every second
            }
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("org.videolan.vlc")) {
            isPlaying = true;
            handler.postDelayed(timerRunnable, 1000);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("org.videolan.vlc")) {
            isPlaying = false;
            handler.removeCallbacks(timerRunnable);
        }
    }

    public static int getListeningTime() {
        return listeningTime;
    }
}
