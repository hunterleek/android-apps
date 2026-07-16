package com.app.util;

import com.app.model.StoragePartition;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PartitionManager {
    public static List<StoragePartition> getPartitions() {
        List<StoragePartition> list = new ArrayList<>();
        list.add(new StoragePartition("Internal", "/storage/emulated/0", "internal", false));
        File sd = new File("/storage/sdcard1");
        if (sd.exists()) list.add(new StoragePartition("SD Card", sd.getAbsolutePath(), "external", false));
        return list;
    }
}
