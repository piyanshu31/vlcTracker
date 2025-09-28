package com.example.vlctracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaListenerService extends NotificationListenerService {
    private static final String TAG = "MediaListenerService";

    private MediaSessionManager mediaSessionManager;
    private MediaController vlcController;
    private boolean isPlaying = false;
    private boolean screenOn = true;
    private boolean counting = false;
    private long startMillis = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        // Listen for active session changes (requires Notification access)
        mediaSessionManager.addOnActiveSessionsChangedListener(
            this::onActiveSessionsChanged,
            new ComponentName(this, MediaListenerService.class)
        );

        // Listen for screen on/off/user present
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);

        // Initial probe
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(
                new ComponentName(this, MediaListenerService.class)
        );
        onActiveSessionsChanged(controllers);

        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        try {
            mediaSessionManager.removeOnActiveSessionsChangedListener(this::onActiveSessionsChanged);
        } catch (Exception ignored) {}
        try {
            if (vlcController != null) vlcController.unregisterCallback(controllerCallback);
        } catch (Exception ignored) {}
        try { unregisterReceiver(screenReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public void onListenerConnected() {
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(
                new ComponentName(this, MediaListenerService.class)
        );
        onActiveSessionsChanged(controllers);
        Log.i(TAG, "Listener connected");
    }

    private final MediaController.Callback controllerCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            if (state != null) {
                boolean nowPlaying = (state.getState() == PlaybackState.STATE_PLAYING);
                if (nowPlaying != isPlaying) {
                    isPlaying = nowPlaying;
                    maybeToggleCounting();
                }
            }
        }
    };

    private void onActiveSessionsChanged(List<MediaController> controllers) {
        // find VLC controller (package name contains "vlc")
        if (vlcController != null) {
            try { vlcController.unregisterCallback(controllerCallback); } catch (Exception ignored) {}
            vlcController = null;
        }
        for (MediaController mc : controllers) {
            String pkg = mc.getPackageName();
            if (pkg != null && pkg.toLowerCase().contains("vlc")) {
                vlcController = mc;
                try { vlcController.registerCallback(controllerCallback); } catch (Exception ignored) {}
                PlaybackState s = vlcController.getPlaybackState();
                isPlaying = s != null && s.getState() == PlaybackState.STATE_PLAYING;
                break;
            }
        }
        maybeToggleCounting();
    }

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(a)) {
                screenOn = false;
                maybeToggleCounting();
            } else if (Intent.ACTION_SCREEN_ON.equals(a) || Intent.ACTION_USER_PRESENT.equals(a)) {
                screenOn = true;
                maybeToggleCounting();
            }
        }
    };

    private void maybeToggleCounting() {
        boolean shouldCount = isPlaying && screenOn; // count only when playing AND screen ON
        if (shouldCount && !counting) {
            counting = true;
            startMillis = System.currentTimeMillis();
            Log.i(TAG, "Start counting at " + startMillis);
        } else if (!shouldCount && counting) {
            counting = false;
            long endMillis = System.currentTimeMillis();
            long durationSec = (endMillis - startMillis) / 1000;
            appendLog(startMillis, endMillis, durationSec);
            // update total in SharedPreferences
            long prev = getSharedPreferences("vlc_tracker", MODE_PRIVATE).getLong("total_seconds", 0L);
            getSharedPreferences("vlc_tracker", MODE_PRIVATE)
                    .edit().putLong("total_seconds", prev + durationSec).apply();
            Log.i(TAG, "Stopped counting: " + durationSec + "s (total " + (prev + durationSec) + "s)");
        }
    }

    private void appendLog(long sMillis, long eMillis, long durSec) {
        try {
            String startIso = fmt(sMillis);
            String endIso = fmt(eMillis);
            String line = startIso + "," + endIso + "," + durSec + "\n";
            FileOutputStream fos = openFileOutput("vlc_listen_log.csv", MODE_APPEND);
            fos.write(line.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing log", e);
        }
    }

    private String fmt(long ms) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        return sdf.format(new Date(ms));
    }
}
