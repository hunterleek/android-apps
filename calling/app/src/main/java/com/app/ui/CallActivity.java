package com.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class CallActivity extends AppCompatActivity {

    private TextView textCallerName;
    private TextView textCallStatus;
    private Handler handler;
    private long callStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        textCallerName = findViewById(R.id.textCallerName);
        textCallStatus = findViewById(R.id.textCallStatus);

        String number = getIntent().getStringExtra("number");
        textCallerName.setText(number != null ? number : "Unknown");

        handler = new Handler();
        callStartTime = System.currentTimeMillis();
        startCallTimer();
    }

    private void startCallTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - callStartTime) / 1000;
                long minutes = elapsed / 60;
                long seconds = elapsed % 60;
                textCallStatus.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
