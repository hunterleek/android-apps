package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new SimplePagerAdapter(this, 3));
        setupTabs(tabLayout, viewPager);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                    }, PERMISSION_REQUEST);
        }
    }

    private void setupTabs(TabLayout tabLayout, ViewPager2 viewPager) {
        String[] tabs = {"Photos", "Albums", "Favorites"};
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position]));
        mediator.attach();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMedia();
        }
    }

    private void loadMedia() {
        // Load media from device
    }
}
