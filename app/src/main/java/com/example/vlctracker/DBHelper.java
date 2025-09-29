package com.vlctracker;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBHelper {
    private Connection conn;

    public DBHelper() {
        try {
            // Load config
            Properties props = new Properties();
            try (InputStream in = DBHelper.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }

            String dbPath = props.getProperty("db.path",
                    System.getProperty("user.home") + "/.vlc_listen_tracker/listen.db");

            String dir = new java.io.File(dbPath).getParent();
            new java.io.File(dir).mkdirs();

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDB() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "start_time TIMESTAMP," +
                    "end_time TIMESTAMP" +
                    ")");
        }
    }

    public void insertSession(Timestamp start, Timestamp end) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sessions (start_time, end_time) VALUES (?, ?)")) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
