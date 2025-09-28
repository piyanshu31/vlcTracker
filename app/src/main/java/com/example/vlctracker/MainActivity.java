package com.example.vlctracker;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    TextView tvStatus, tvTotal, tvLogs;
    Button btnOpenNotif, btnRefresh, btnLoadLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvLogs = findViewById(R.id.tvLogs);
        btnOpenNotif = findViewById(R.id.btnOpenNotif);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLoadLogs = findViewById(R.id.btnLoadLogs);

        btnOpenNotif.setOnClickListener(v -> {
            // Open Android's Notification access settings so user can enable the app
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        btnRefresh.setOnClickListener(v -> refreshStatus());
        btnLoadLogs.setOnClickListener(v -> loadLogs());

        refreshStatus();
    }

    void refreshStatus() {
        boolean enabled = NotificationManagerCompat.getEnabledListenerPackages(this)
                .contains(getPackageName());
        tvStatus.setText(enabled ? "Notification access: ENABLED" : "Notification access: DISABLED");

        long total = getSharedPreferences("vlc_tracker", MODE_PRIVATE).getLong("total_seconds", 0);
        tvTotal.setText("Total listening seconds: " + total);
    }

    void loadLogs() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("vlc_listen_log.csv")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            tvLogs.setText(sb.toString());
        } catch (Exception e) {
            tvLogs.setText("No logs yet.");
        }
    }
}
