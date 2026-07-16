package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION = 102;
    private RecyclerView recycler;
    private PhotoAdapter adapter;
    private List<Photo> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyclerView);
        adapter = new PhotoAdapter(photos);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        recycler.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, PERMISSION);
        } else {
            loadMedia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION) loadMedia();
    }

    private void loadMedia() {
        photos.clear();
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
        } catch (Exception e) {
            Toast.makeText(this, "Cannot load media: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (c != null) {
            int dataIdx = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int mimeIdx = c.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            while (c.moveToNext()) {
                String path = c.getString(dataIdx);
                String mime = c.getString(mimeIdx);
                if (path != null && new File(path).exists()) {
                    photos.add(new Photo(path, mime));
                }
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }

    static class Photo {
        String path, mime;
        Photo(String p, String m) { path = p; mime = m; }
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.VH> {
        List<Photo> data;
        PhotoAdapter(List<Photo> data) { this.data = data; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Photo ph = data.get(p);
            Glide.with(MainActivity.this).load(ph.path).centerCrop().into(h.img);
            h.itemView.setOnClickListener(v -> Toast.makeText(MainActivity.this, ph.path, Toast.LENGTH_SHORT).show());
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.imageThumbnail); }
        }
    }
}
