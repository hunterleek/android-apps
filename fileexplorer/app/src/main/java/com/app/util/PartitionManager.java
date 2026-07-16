package com.app.util;

import android.util.Log;
import com.app.model.StoragePartition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartitionManager {

    private static final String TAG = "PartitionManager";

    public static List<StoragePartition> getAllPartitions() {
        List<StoragePartition> partitions = new ArrayList<>();

        // Standard Android partitions
        partitions.add(new StoragePartition("Internal Storage", "/storage/emulated/0", "Internal", false));

        // Check for SD card
        File sdCard = new File("/storage/sdcard1");
        if (!sdCard.exists()) {
            sdCard = new File("/storage/external_sd");
        }
        if (!sdCard.exists()) {
            sdCard = new File("/mnt/sdcard");
        }
        if (sdCard.exists() && sdCard.canRead()) {
            partitions.add(new StoragePartition("SD Card", sdCard.getAbsolutePath(), "External", false));
        }

        // Add root filesystem if rooted
        if (RootUtils.isRootAvailable()) {
            partitions.add(new StoragePartition("Root (/)", "/", "System", true));
        }

        // Parse /proc/mounts for additional partitions
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String device = parts[0];
                    String mountPoint = parts[1];
                    String fsType = parts[2];

                    // Skip standard mounts we already added
                    if (mountPoint.equals("/") || mountPoint.equals("/storage/emulated/0") ||
                        mountPoint.startsWith("/storage/sdcard") || mountPoint.startsWith("/storage/external_sd") ||
                        mountPoint.equals("/mnt/sdcard")) {
                        continue;
                    }

                    // Skip virtual filesystems
                    if (device.startsWith("tmpfs") || device.startsWith("proc") ||
                        device.startsWith("sysfs") || device.startsWith("devpts") ||
                        device.startsWith("cgroup") || device.equals("none")) {
                        continue;
                    }

                    // Add partition if it exists and is accessible
                    File mountFile = new File(mountPoint);
                    if (mountFile.exists() && mountFile.canRead()) {
                        String name = device.replace("/dev/block/", "").replace("mmcblk", "MMC ").replace("p", " Partition ");
                        partitions.add(new StoragePartition(name, mountPoint, fsType, false));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading /proc/mounts", e);
        }

        // Add common Android partitions if they exist
        addPartitionIfExists(partitions, "Data", "/data", "Data", true);
        addPartitionIfExists(partitions, "System", "/system", "System", true);
        addPartitionIfExists(partitions, "Vendor", "/vendor", "Vendor", true);
        addPartitionIfExists(partitions, "Product", "/product", "Product", true);
        addPartitionIfExists(partitions, "ODM", "/odm", "ODM", true);
        addPartitionIfExists(partitions, "Cache", "/cache", "Cache", true);
        addPartitionIfExists(partitions, "Recovery", "/recovery", "Recovery", true);
        addPartitionIfExists(partitions, "Boot", "/boot", "Boot", true);
        addPartitionIfExists(partitions, "Recovery", "/recovery", "Recovery", true);

        return partitions;
    }

    private static void addPartitionIfExists(List<StoragePartition> partitions, String name, String path, String type, boolean isRoot) {
        File file = new File(path);
        if (file.exists()) {
            partitions.add(new StoragePartition(name, path, type, isRoot));
        }
    }

    public static List<File> getAllSystemDirectories() {
        List<File> directories = new ArrayList<>();

        // Root directories
        if (RootUtils.isRootAvailable()) {
            directories.add(new File("/"));
            directories.add(new File("/bin"));
            directories.add(new File("/sbin"));
            directories.add(new File("/system"));
            directories.add(new File("/system/bin"));
            directories.add(new File("/system/xbin"));
            directories.add(new File("/system/lib"));
            directories.add(new File("/system/lib64"));
            directories.add(new File("/system/etc"));
            directories.add(new File("/system/priv-app"));
            directories.add(new File("/system/app"));
            directories.add(new File("/system/framework"));
            directories.add(new File("/vendor"));
            directories.add(new File("/vendor/bin"));
            directories.add(new File("/vendor/lib"));
            directories.add(new File("/vendor/lib64"));
            directories.add(new File("/vendor/etc"));
            directories.add(new File("/product"));
            directories.add(new File("/odm"));
            directories.add(new File("/data"));
            directories.add(new File("/data/local"));
            directories.add(new File("/data/local/tmp"));
            directories.add(new File("/data/misc"));
            directories.add(new File("/data/data"));
            directories.add(new File("/cache"));
            directories.add(new File("/proc"));
            directories.add(new File("/sys"));
            directories.add(new File("/dev"));
            directories.add(new File("/dev/block"));
            directories.add(new File("/mnt"));
            directories.add(new File("/root"));
        }

        // User-accessible directories
        directories.add(new File("/storage/emulated/0"));
        directories.add(new File("/storage/emulated/0/Download"));
        directories.add(new File("/storage/emulated/0/Documents"));
        directories.add(new File("/storage/emulated/0/Pictures"));
        directories.add(new File("/storage/emulated/0/DCIM"));
        directories.add(new File("/storage/emulated/0/Movies"));
        directories.add(new File("/storage/emulated/0/Music"));
        directories.add(new File("/storage/emulated/0/Android"));

        return directories;
    }
}
