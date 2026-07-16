package com.app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import com.app.adapter.FileAdapter;
import com.app.model.FileItem;
import com.app.model.StoragePartition;
import com.app.util.PartitionManager;
import com.app.util.RootUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int PERMISSION_REQUEST_ALL_FILES = 1001;
    private static final int PERMISSION_REQUEST_STORAGE = 1002;

    private RecyclerView recyclerView;
    private TextView textEmpty, textPath;
    private FileAdapter adapter;
    private List<FileItem> fileList;
    private File currentDirectory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View selectionBar;
    private TextView textSelectedCount;
    private boolean isGridView = false;
    private boolean showHiddenFiles = true;
    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkRootAccess();
        requestPermissions();
        setupNavigation();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        setupSelectionBar();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        textEmpty = findViewById(R.id.textEmpty);
        textPath = findViewById(R.id.textPath);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        selectionBar = findViewById(R.id.selectionBar);
        textSelectedCount = findViewById(R.id.textSelectedCount);
    }

    private void checkRootAccess() {
        RootUtils.checkRootAccess();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to manage all files. Please grant permission on the next screen.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(this, "Some features may not work without permission", Toast.LENGTH_LONG).show();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                loadDirectory(new File("/storage/emulated/0"));
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_STORAGE);
            } else {
                loadDirectory(new File("/storage/emulated/0"));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadDirectory(new File("/storage/emulated/0"));
            } else {
                Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_LONG).show();
                loadDirectory(new File("/storage/emulated/0"));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            if (currentDirectory == null) {
                loadDirectory(new File("/storage/emulated/0"));
            }
        }
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            int id = item.getItemId();
            if (id == R.id.nav_internal) {
                loadDirectory(new File("/storage/emulated/0"));
            } else if (id == R.id.nav_sdcard) {
                File sdCard = getSdCardPath();
                if (sdCard != null) {
                    loadDirectory(sdCard);
                } else {
                    Toast.makeText(this, "SD Card not found", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_downloads) {
                loadDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()));
            } else if (id == R.id.nav_documents) {
                loadDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()));
            } else if (id == R.id.nav_images) {
                loadDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()));
            } else if (id == R.id.nav_videos) {
                loadDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()));
            } else if (id == R.id.nav_audio) {
                loadDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()));
            } else if (id == R.id.nav_archives) {
                navigateToArchives();
            } else if (id == R.id.nav_apks) {
                navigateToApks();
            } else if (id == R.id.nav_recent) {
                navigateToRecent();
            } else if (id == R.id.nav_favorites) {
                navigateToFavorites();
            } else if (id == R.id.nav_trash) {
                navigateToTrash();
            } else if (id == R.id.nav_show_hidden) {
                showHiddenFiles = !showHiddenFiles;
                item.setChecked(showHiddenFiles);
                adapter.setShowHiddenFiles(showHiddenFiles);
                if (currentDirectory != null) {
                    loadDirectory(currentDirectory);
                }
            }
            return true;
        });

        updateStorageInfo();
    }

    private void updateStorageInfo() {
        View header = navigationView.getHeaderView(0);
        if (header != null) {
            TextView textStorage = header.findViewById(R.id.textStorage);
            File internal = new File("/storage/emulated/0");
            if (internal.exists()) {
                long total = internal.getTotalSpace();
                long free = internal.getFreeSpace();
                long used = total - free;
                textStorage.setText(StoragePartition.formatSize(used) + " / " + StoragePartition.formatSize(total));
            }
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.menu_grid) {
                isGridView = !isGridView;
                item.setIcon(isGridView ? R.drawable.ic_list : R.drawable.ic_grid);
                adapter.setGridView(isGridView);
                return true;
            } else if (id == R.id.menu_sort) {
                showSortDialog();
                return true;
            } else if (id == R.id.menu_select_all) {
                enterSelectMode();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        fileList = new ArrayList<>();
        adapter = new FileAdapter(this, fileList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showNewFileMenu());
    }

    private void setupSelectionBar() {
        findViewById(R.id.btnCopy).setOnClickListener(v -> copySelectedFiles());
        findViewById(R.id.btnCut).setOnClickListener(v -> cutSelectedFiles());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteSelectedFiles());
        findViewById(R.id.btnMore).setOnClickListener(v -> showMoreOptions());
    }

    public void loadDirectory(File directory) {
        if (directory == null) return;

        currentDirectory = directory;
        textPath.setText(directory.getAbsolutePath());

        fileList.clear();

        File[] files = directory.listFiles();
        if (files == null && RootUtils.isRootAvailable()) {
            files = RootUtils.listFilesInRoot(directory).toArray(new File[0]);
        }

        if (files != null && files.length > 0) {
            List<File> fileListTemp = new ArrayList<>();
            Collections.addAll(fileListTemp, files);

            Collections.sort(fileListTemp, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File file : fileListTemp) {
                if (!showHiddenFiles && file.getName().startsWith(".")) {
                    continue;
                }
                fileList.add(new FileItem(file));
            }

            recyclerView.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFileClick(FileItem file) {
        if (file.isDirectory()) {
            loadDirectory(file.getFile());
        } else {
            openFile(file.getFile());
        }
    }

    @Override
    public void onFileLongClick(FileItem file) {
        enterSelectMode();
        file.setSelected(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onOptionsClick(FileItem file, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.context_menu);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_copy) {
                // Copy file
                return true;
            } else if (id == R.id.menu_cut) {
                // Cut file
                return true;
            } else if (id == R.id.menu_rename) {
                showRenameDialog(file);
                return true;
            } else if (id == R.id.menu_delete) {
                deleteFile(file);
                return true;
            } else if (id == R.id.menu_properties) {
                showProperties(file);
                return true;
            } else if (id == R.id.menu_share) {
                shareFile(file);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void openFile(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = androidx.core.content.FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);
            intent.setDataAndType(uri, getMimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open this file type", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
            name.endsWith(".gif") || name.endsWith(".webp")) {
            return "image/*";
        } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi")) {
            return "video/*";
        } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac")) {
            return "audio/*";
        } else if (name.endsWith(".pdf")) {
            return "application/pdf";
        } else if (name.endsWith(".apk")) {
            return "application/vnd.android.package-archive";
        } else if (name.endsWith(".txt")) {
            return "text/plain";
        }
        return "*/*";
    }

    private void showNewFileMenu() {
        new AlertDialog.Builder(this)
            .setTitle("Create New")
            .setItems(new String[]{"Folder", "File"}, (dialog, which) -> {
                if (which == 0) {
                    showNewFolderDialog();
                } else {
                    showNewFileDialog();
                }
            })
            .show();
    }

    private void showNewFolderDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_new_folder, null);
        com.google.android.material.textfield.TextInputEditText editName = view.findViewById(R.id.editName);

        new AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = editName.getText().toString().trim();
                if (!name.isEmpty()) {
                    File newFolder = new File(currentDirectory, name);
                    if (newFolder.mkdirs()) {
                        loadDirectory(currentDirectory);
                        Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showNewFileDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_new_folder, null);
        com.google.android.material.textfield.TextInputEditText editName = view.findViewById(R.id.editName);

        new AlertDialog.Builder(this)
            .setTitle("New File")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = editName.getText().toString().trim();
                if (!name.isEmpty()) {
                    File newFile = new File(currentDirectory, name);
                    try {
                        if (newFile.createNewFile()) {
                            loadDirectory(currentDirectory);
                            Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showRenameDialog(FileItem file) {
        View view = getLayoutInflater().inflate(R.layout.dialog_rename, null);
        com.google.android.material.textfield.TextInputEditText editName = view.findViewById(R.id.editName);
        editName.setText(file.getName());

        new AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(view)
            .setPositiveButton("Rename", (dialog, which) -> {
                String newName = editName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    File newFile = new File(file.getFile().getParent(), newName);
                    if (file.getFile().renameTo(newFile)) {
                        loadDirectory(currentDirectory);
                        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to rename", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteFile(FileItem file) {
        new AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete \"" + file.getName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (file.getFile().delete() || (RootUtils.isRootAvailable() && RootUtils.deleteFile(file.getFile()))) {
                    loadDirectory(currentDirectory);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showProperties(FileItem file) {
        Intent intent = new Intent(this, PropertiesActivity.class);
        intent.putExtra("file_path", file.getFile().getAbsolutePath());
        startActivity(intent);
    }

    private void shareFile(FileItem file) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = androidx.core.content.FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file.getFile());
            intent.setDataAndType(uri, getMimeType(file.getFile()));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot share this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void enterSelectMode() {
        selectMode = true;
        selectionBar.setVisibility(View.VISIBLE);
        adapter.setSelectMode(true);
    }

    private void exitSelectMode() {
        selectMode = false;
        selectionBar.setVisibility(View.GONE);
        adapter.setSelectMode(false);
        for (FileItem item : fileList) {
            item.setSelected(false);
        }
    }

    private void copySelectedFiles() {
        // Implement copy functionality
        Toast.makeText(this, "Copy to clipboard", Toast.LENGTH_SHORT).show();
        exitSelectMode();
    }

    private void cutSelectedFiles() {
        // Implement cut functionality
        Toast.makeText(this, "Cut to clipboard", Toast.LENGTH_SHORT).show();
        exitSelectMode();
    }

    private void deleteSelectedFiles() {
        int count = 0;
        for (FileItem item : fileList) {
            if (item.isSelected()) {
                if (item.getFile().delete()) count++;
            }
        }
        Toast.makeText(this, "Deleted " + count + " items", Toast.LENGTH_SHORT).show();
        loadDirectory(currentDirectory);
        exitSelectMode();
    }

    private void showMoreOptions() {
        // Show more options menu
    }

    private void showSortDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setItems(new String[]{"Name", "Date", "Size", "Type"}, (dialog, which) -> {
                // Implement sorting
                loadDirectory(currentDirectory);
            })
            .show();
    }

    private File getSdCardPath() {
        File[] externalDirs = getExternalFilesDirs(null);
        if (externalDirs != null && externalDirs.length > 1) {
            String path = externalDirs[1].getAbsolutePath();
            int index = path.indexOf("/Android");
            if (index > 0) {
                return new File(path.substring(0, index));
            }
        }
        return null;
    }

    private void navigateToArchives() {
        // Navigate to archives folder
    }

    private void navigateToApks() {
        // Navigate to APKs folder
    }

    private void navigateToRecent() {
        // Navigate to recent files
    }

    private void navigateToFavorites() {
        // Navigate to favorites
    }

    private void navigateToTrash() {
        // Navigate to trash
    }

    @Override
    public void onBackPressed() {
        if (selectMode) {
            exitSelectMode();
        } else if (currentDirectory != null && !currentDirectory.getAbsolutePath().equals("/storage/emulated/0")) {
            loadDirectory(currentDirectory.getParentFile());
        } else {
            super.onBackPressed();
        }
    }
}
