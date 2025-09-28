package com.example.vlctracker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerTextView = findViewById(R.id.timerTextView);

        // Display the current listening time
        timerTextView.setText("Listening time: " + MediaListenerService.getListeningTime() + " seconds");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optionally, update the timer every second
    }
}
