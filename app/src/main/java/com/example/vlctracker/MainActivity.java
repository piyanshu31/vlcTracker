package com.example.vlctracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "VlcPrefs";
    private static final String KEY_TOTAL_TIME = "total_time";

    private TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeView = findViewById(R.id.timeView);
        updateTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTime();
    }

    private void updateTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long total = prefs.getLong(KEY_TOTAL_TIME, 0);
        long seconds = total / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        String display = String.format("Total VLC Listening Time:\n%02d:%02d:%02d",
                hours, minutes % 60, seconds % 60);
        timeView.setText(display);
    }
}
