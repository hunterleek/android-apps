package com.app.model;

import java.io.File;

public class StoragePartition {
    private String name;
    private String path;
    private long totalSpace;
    private long freeSpace;
    private long usedSpace;
    private String type;
    private boolean isRoot;
    private boolean isMounted;

    public StoragePartition(String name, String path, String type, boolean isRoot) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.isRoot = isRoot;
        this.isMounted = true;

        File file = new File(path);
        if (file.exists() && file.canRead()) {
            this.totalSpace = file.getTotalSpace();
            this.freeSpace = file.getFreeSpace();
            this.usedSpace = totalSpace - freeSpace;
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public String getType() {
        return type;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public boolean isMounted() {
        return isMounted;
    }

    public int getUsagePercent() {
        if (totalSpace == 0) return 0;
        return (int) ((usedSpace * 100) / totalSpace);
    }

    public static String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024.0));
        if (size < 1024L * 1024 * 1024 * 1024) return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        return String.format("%.2f TB", size / (1024.0 * 1024.0 * 1024.0 * 1024.0));
    }
}
