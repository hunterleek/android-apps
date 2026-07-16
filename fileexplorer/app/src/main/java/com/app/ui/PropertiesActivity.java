package com.app.ui;

import com.app.R;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.app.model.StoragePartition;
import java.io.File;

public class PropertiesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        String path = getIntent().getStringExtra("file_path");
        if (path == null) {
            Toast.makeText(this, "No file", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        File file = new File(path);
        TextView textName = findViewById(R.id.textName);
        TextView rowSize = findViewById(R.id.rowSize);
        TextView rowType = findViewById(R.id.rowType);
        TextView rowPath = findViewById(R.id.rowPath);
        TextView rowModified = findViewById(R.id.rowModified);
        if (textName != null) textName.setText(file.getName());
        if (rowSize != null) rowSize.setText(file.isDirectory() ? "-" : StoragePartition.formatSize(file.length()));
        if (rowType != null) rowType.setText(file.isDirectory() ? "Folder" : "File");
        if (rowPath != null) rowPath.setText(file.getAbsolutePath());
        if (rowModified != null) rowModified.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(file.lastModified())));
    }
}
