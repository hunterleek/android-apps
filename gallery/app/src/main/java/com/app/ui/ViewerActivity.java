package com.app.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<File> mediaFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        viewPager = findViewById(R.id.viewPager);
        ImageButton btnShare = findViewById(R.id.btnShare);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnFavorite = findViewById(R.id.btnFavorite);
        ImageButton btnDelete = findViewById(R.id.btnDelete);

        mediaFiles = new ArrayList<>();
        loadMediaFiles();

        btnShare.setOnClickListener(v -> shareMedia());
        btnEdit.setOnClickListener(v -> editMedia());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnDelete.setOnClickListener(v -> deleteMedia());
    }

    private void loadMediaFiles() {
        // Load media files from intent or storage
    }

    private void shareMedia() {
        Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
    }

    private void editMedia() {
        Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        Toast.makeText(this, "Favorite toggled", Toast.LENGTH_SHORT).show();
    }

    private void deleteMedia() {
        Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();
    }
}
