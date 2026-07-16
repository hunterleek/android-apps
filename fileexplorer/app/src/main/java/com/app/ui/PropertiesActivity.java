package com.app.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.app.R;
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
        TextView textPath = findViewById(R.id.textPath);
        TextView textSize = findViewById(R.id.textSize);
        TextView textDate = findViewById(R.id.textDate);
        TextView textType = findViewById(R.id.textType);
        if (textName != null) textName.setText(file.getName());
        if (textPath != null) textPath.setText(file.getAbsolutePath());
        if (textSize != null) textSize.setText(file.isDirectory() ? "-" : StoragePartition.formatSize(file.length()));
        if (textDate != null) textDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(file.lastModified())));
        if (textType != null) textType.setText(file.isDirectory() ? "Folder" : "File");
    }
}
