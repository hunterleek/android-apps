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
    private TextView textCount;
    private List<Media> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textCount = findViewById(R.id.textCount);
        recycler = findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoAdapter(mediaList);
        recycler.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, PERMISSION);
        } else {
            loadAllMedia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION) loadAllMedia();
    }

    private void loadAllMedia() {
        mediaList.clear();
        loadBucket(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false);
        loadBucket(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true);
        adapter.notifyDataSetChanged();
        textCount.setText(mediaList.size() + " items");
    }

    private void loadBucket(Uri uri, boolean isVideo) {
        String col = MediaStore.MediaColumns.DATA;
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, new String[]{col}, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (c != null) {
            int idx = c.getColumnIndexOrThrow(col);
            while (c.moveToNext()) {
                String path = c.getString(idx);
                if (path != null && new File(path).exists()) {
                    mediaList.add(new Media(path, isVideo));
                }
            }
            c.close();
        }
    }

    static class Media {
        String path;
        boolean video;
        Media(String p, boolean v) { path = p; video = v; }
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.VH> {
        List<Media> data;
        PhotoAdapter(List<Media> data) { this.data = data; }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int p) {
            Media m = data.get(p);
            Glide.with(MainActivity.this).load(m.path).centerCrop().into(h.img);
            h.badge.setVisibility(m.video ? View.VISIBLE : View.GONE);
            h.itemView.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, m.path, Toast.LENGTH_SHORT).show();
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView badge;
            VH(View v) {
                super(v);
                img = v.findViewById(R.id.imageThumbnail);
                badge = v.findViewById(R.id.videoBadge);
            }
        }
    }
}
