package com.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RootUtils {
    public static boolean isRootAvailable() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) { return false; }
    }
    public static void checkRootAccess() {}
    public static List<File> listFilesInRoot(File dir) {
        List<File> list = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls -1 '" + dir.getAbsolutePath().replace("'", "'\\''") + "'"});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) list.add(new File(dir, line));
            p.waitFor();
        } catch (Exception e) {}
        return list;
    }
    public static boolean deleteFile(File f) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -rf '" + f.getAbsolutePath().replace("'", "'\\''") + "'"});
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) { return false; }
    }
}
