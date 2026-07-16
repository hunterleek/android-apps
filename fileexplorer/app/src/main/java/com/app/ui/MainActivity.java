package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.app.adapter.FileAdapter;
import com.app.model.FileItem;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int PERMISSION_STORAGE = 1002;

    private RecyclerView recyclerView;
    private TextView textEmpty, textPath;
    private FileAdapter adapter;
    private List<FileItem> fileList = new ArrayList<>();
    private File currentDirectory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean showHiddenFiles = true;
    private List<FileItem> clipboard = new ArrayList<>();
    private boolean clipboardCut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        setupNavigation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                    .setTitle("All Files Permission")
                    .setMessage("This file explorer needs access to all files on your device.")
                    .setPositiveButton("Grant", (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (d, w) -> loadDirectory(getRootDir()))
                    .setCancelable(false)
                    .show();
            } else {
                loadDirectory(getRootDir());
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE);
            } else {
                loadDirectory(getRootDir());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() && currentDirectory == null) {
            loadDirectory(getRootDir());
        }
    }

    private File getRootDir() {
        File root = Environment.getExternalStorageDirectory();
        if (root == null || !root.exists()) root = new File("/storage/emulated/0");
        return root;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        textEmpty = findViewById(R.id.textEmpty);
        textPath = findViewById(R.id.textPath);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_search) {
                Toast.makeText(this, "Search is not implemented yet", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_select_all) {
                selectAll();
            } else if (id == R.id.menu_sort) {
                sortFiles();
            }
            return true;
        });
    }

    private void setupRecyclerView() {
        adapter = new FileAdapter(this, fileList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showNewMenu());
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_internal) loadDirectory(getRootDir());
            else if (id == R.id.nav_sdcard) loadDirectory(new File("/storage/sdcard1"));
            else if (id == R.id.nav_downloads) loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            else if (id == R.id.nav_documents) loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            else if (id == R.id.nav_images) loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            else if (id == R.id.nav_videos) loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
            else if (id == R.id.nav_audio) loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
            else if (id == R.id.nav_show_hidden) {
                showHiddenFiles = !showHiddenFiles;
                item.setChecked(showHiddenFiles);
                adapter.setShowHiddenFiles(showHiddenFiles);
                if (currentDirectory != null) loadDirectory(currentDirectory);
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_STORAGE) {
            loadDirectory(getRootDir());
        }
    }

    public void loadDirectory(File dir) {
        if (dir == null) return;
        currentDirectory = dir;
        textPath.setText(dir.getAbsolutePath());
        fileList.clear();

        File[] files = dir.listFiles();
        if (files != null) {
            List<File> temp = new ArrayList<>();
            for (File f : files) {
                if (!showHiddenFiles && f.getName().startsWith(".")) continue;
                temp.add(f);
            }
            Collections.sort(temp, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File f : temp) fileList.add(new FileItem(f));
        }

        adapter.notifyDataSetChanged();
        textEmpty.setVisibility(fileList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(fileList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onFileClick(FileItem item) {
        File f = item.getFile();
        if (f.isDirectory()) {
            loadDirectory(f);
        } else {
            openFile(f);
        }
    }

    @Override
    public void onFileLongClick(FileItem item) {
        item.setSelected(!item.isSelected());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onOptionsClick(FileItem item, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Copy");
        popup.getMenu().add(0, 2, 0, "Cut");
        popup.getMenu().add(0, 3, 0, "Rename");
        popup.getMenu().add(0, 4, 0, "Delete");
        popup.getMenu().add(0, 5, 0, "Share");
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == 1) copyFiles(Collections.singletonList(item));
            else if (id == 2) cutFiles(Collections.singletonList(item));
            else if (id == 3) renameFile(item);
            else if (id == 4) deleteFile(item);
            else if (id == 5) shareFile(item);
            return true;
        });
        popup.show();
    }

    private void showNewMenu() {
        new AlertDialog.Builder(this)
            .setTitle("Create")
            .setItems(new String[]{"New Folder", "Paste"}, (d, which) -> {
                if (which == 0) newFolder();
                else if (which == 1) pasteFiles();
            })
            .show();
    }

    private void newFolder() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_new_folder, null);
        EditText edit = v.findViewById(R.id.editName);
        new AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setView(v)
            .setPositiveButton("Create", (d, w) -> {
                String name = edit.getText().toString().trim();
                if (!name.isEmpty()) {
                    File f = new File(currentDirectory, name);
                    if (f.mkdirs()) loadDirectory(currentDirectory);
                    else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void renameFile(FileItem item) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_new_folder, null);
        EditText edit = v.findViewById(R.id.editName);
        edit.setText(item.getFile().getName());
        new AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(v)
            .setPositiveButton("Rename", (d, w) -> {
                String name = edit.getText().toString().trim();
                if (!name.isEmpty()) {
                    File dest = new File(item.getFile().getParentFile(), name);
                    if (item.getFile().renameTo(dest)) loadDirectory(currentDirectory);
                    else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteFile(FileItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Delete \"" + item.getName() + "\"?")
            .setPositiveButton("Delete", (d, w) -> {
                if (item.getFile().delete()) loadDirectory(currentDirectory);
                else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void copyFiles(List<FileItem> items) {
        clipboard.clear();
        clipboard.addAll(items);
        clipboardCut = false;
        Toast.makeText(this, "Copied " + items.size() + " item(s)", Toast.LENGTH_SHORT).show();
    }

    private void cutFiles(List<FileItem> items) {
        clipboard.clear();
        clipboard.addAll(items);
        clipboardCut = true;
        Toast.makeText(this, "Cut " + items.size() + " item(s)", Toast.LENGTH_SHORT).show();
    }

    private void pasteFiles() {
        if (clipboard.isEmpty()) {
            Toast.makeText(this, "Clipboard empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int moved = 0;
        for (FileItem item : clipboard) {
            File src = item.getFile();
            File dest = new File(currentDirectory, src.getName());
            if (clipboardCut) {
                if (src.renameTo(dest)) moved++;
            } else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.copy(src.toPath(), dest.toPath());
                    } else {
                        copyFileLegacy(src, dest);
                    }
                    moved++;
                } catch (Exception e) {}
            }
        }
        if (clipboardCut) clipboard.clear();
        loadDirectory(currentDirectory);
        Toast.makeText(this, "Pasted " + moved + " item(s)", Toast.LENGTH_SHORT).show();
    }

    private void copyFileLegacy(File src, File dest) throws Exception {
        java.io.InputStream in = new java.io.FileInputStream(src);
        java.io.OutputStream out = new java.io.FileOutputStream(dest);
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        in.close();
        out.close();
    }

    private void selectAll() {
        boolean all = true;
        for (FileItem i : fileList) if (!i.isSelected()) { all = false; break; }
        for (FileItem i : fileList) i.setSelected(!all);
        adapter.notifyDataSetChanged();
    }

    private void sortFiles() {
        Collections.sort(fileList, Comparator.comparing(a -> a.getName().toLowerCase()));
        adapter.notifyDataSetChanged();
    }

    private void openFile(File f) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", f);
            intent.setDataAndType(uri, getMimeType(f));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(FileItem item) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", item.getFile());
            intent.setType(getMimeType(item.getFile()));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot share", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(File f) {
        String n = f.getName().toLowerCase();
        if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".gif")) return "image/*";
        if (n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".avi")) return "video/*";
        if (n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".flac")) return "audio/*";
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".apk")) return "application/vnd.android.package-archive";
        if (n.endsWith(".txt")) return "text/plain";
        return "*/*";
    }
}
