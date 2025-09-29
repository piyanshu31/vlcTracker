package com.vlctracker;

import org.freedesktop.dbus.connections.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

public class VLCTracker {
    private final DBHelper db;
    private final DBusConnection conn;
    private final long pollInterval;
    private Timestamp sessionStart = null;

    public VLCTracker() throws DBusException {
        db = new DBHelper();

        // Load config
        Properties props = new Properties();
        try (InputStream in = VLCTracker.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        }
        pollInterval = Long.parseLong(props.getProperty("poll.interval", "1000"));

        conn = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION);
    }

    private String getVLCPlaybackStatus() {
        try {
            var obj = conn.getRemoteObject("org.mpris.MediaPlayer2.vlc",
                    "/org/mpris/MediaPlayer2",
                    org.freedesktop.dbus.interfaces.Properties.class);
            Map<String, Variant<?>> props = obj.GetAll("org.mpris.MediaPlayer2.Player");
            return props.get("PlaybackStatus").getValue().toString();
        } catch (Exception e) {
            return "Stopped";
        }
    }

    private void startSession() {
        if (sessionStart == null) {
            sessionStart = Timestamp.from(Instant.now());
            System.out.println("Session started at " + sessionStart);
        }
    }

    private void endSession() {
        if (sessionStart != null) {
            Timestamp end = Timestamp.from(Instant.now());
            db.insertSession(sessionStart, end);
            System.out.println("Session saved: " + sessionStart + " → " + end);
            sessionStart = null;
        }
    }

    public void run() {
        System.out.println("VLC Tracker running...");
        while (true) {
            String status = getVLCPlaybackStatus();
            boolean playing = "Playing".equals(status);

            if (playing && ScreenHelper.isScreenActive()) {
                startSession();
            } else {
                endSession();
            }

            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        VLCTracker tracker = new VLCTracker();
        tracker.run();
    }
}
