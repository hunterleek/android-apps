package com.app.model;

public class StoragePartition {
    private String name, path;
    public StoragePartition(String name, String path, String type, boolean isRoot) {
        this.name = name; this.path = path;
    }
    public String getName() { return name; }
    public String getPath() { return path; }

    public static String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        if (size < 1024L * 1024 * 1024) return String.format(java.util.Locale.US, "%.2f MB", size / (1024.0 * 1024.0));
        return String.format(java.util.Locale.US, "%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
