package com.app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.app.R;
import com.app.adapter.FileAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textNumber;
    private static final int PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textNumber = findViewById(R.id.textNumber);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        MaterialButton btnCall = findViewById(R.id.btnCall);

        setupDialer();
        viewPager.setAdapter(new SimplePagerAdapter(this, 3));
        setupTabs(tabLayout, viewPager);

        btnCall.setOnClickListener(v -> makeCall());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST);
        }
    }

    private void setupDialer() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9, R.id.btnStar, R.id.btnHash
        };

        for (int id : buttonIds) {
            MaterialButton btn = findViewById(id);
            btn.setOnClickListener(v -> {
                String current = textNumber.getText().toString();
                String digit = ((MaterialButton) v).getText().toString().split(" ")[0];
                textNumber.setText(current + digit);
            });
        }
    }

    private void setupTabs(TabLayout tabLayout, ViewPager2 viewPager) {
        String[] tabs = {"Recent", "Contacts", "Voicemail"};
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position]));
        mediator.attach();
    }

    private void makeCall() {
        String number = textNumber.getText().toString().trim();
        if (!number.isEmpty()) {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("number", number);
            startActivity(intent);
        }
    }
}
