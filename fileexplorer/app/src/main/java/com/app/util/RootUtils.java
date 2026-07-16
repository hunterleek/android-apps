package com.app.util;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RootUtils {

    private static final String TAG = "RootUtils";
    private static boolean rootAvailable = false;

    public static boolean isRootAvailable() {
        return rootAvailable;
    }

    public static boolean checkRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("echo\n");
            os.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.destroy();
            rootAvailable = line != null;
            return rootAvailable;
        } catch (IOException e) {
            rootAvailable = false;
            return false;
        }
    }

    public static List<File> getRootDirectories() {
        List<File> directories = new ArrayList<>();
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("ls /\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        directories.add(new File("/" + line.trim()));
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                Log.e(TAG, "Error getting root directories", e);
            }
        }
        return directories;
    }

    public static List<File> listFilesInRoot(File directory) {
        List<File> files = new ArrayList<>();
        if (rootAvailable && directory != null) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("ls -la \"" + directory.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("total")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 9) {
                            String name = parts[parts.length - 1];
                            if (!name.equals(".") && !name.equals("..")) {
                                files.add(new File(directory, name));
                            }
                        }
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                Log.e(TAG, "Error listing root files", e);
            }
        }
        return files;
    }

    public static boolean canReadFile(File file) {
        if (file.canRead()) return true;
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("cat \"" + file.getAbsolutePath() + "\" > /dev/null\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
                return process.exitValue() == 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static long getFileSize(File file) {
        if (file.canRead()) {
            return file.length();
        }
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("stat -c %s \"" + file.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null) {
                    return Long.parseLong(line.trim());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file size", e);
            }
        }
        return 0;
    }

    public static long getFileModifiedTime(File file) {
        if (file.canRead()) {
            return file.lastModified();
        }
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("stat -c %Y \"" + file.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null) {
                    return Long.parseLong(line.trim()) * 1000;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file modified time", e);
            }
        }
        return 0;
    }

    public static boolean isHidden(File file) {
        if (file.canRead()) {
            return file.isHidden();
        }
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("ls -la \"" + file.getParent() + "\" | grep \" " + file.getName() + "$\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null && line.trim().startsWith("-")) {
                    return line.trim().charAt(0) == '.';
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking hidden file", e);
            }
        }
        return false;
    }

    public static boolean deleteFile(File file) {
        if (file.canWrite()) {
            return file.delete();
        }
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("rm -rf \"" + file.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
                return process.exitValue() == 0;
            } catch (Exception e) {
                Log.e(TAG, "Error deleting file", e);
            }
        }
        return false;
    }

    public static boolean createDirectory(File directory) {
        if (directory.canWrite()) {
            return directory.mkdirs();
        }
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("mkdir -p \"" + directory.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
                return process.exitValue() == 0;
            } catch (Exception e) {
                Log.e(TAG, "Error creating directory", e);
            }
        }
        return false;
    }

    public static String getFilePermissions(File file) {
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("stat -c %A \"" + file.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null) {
                    return line.trim();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file permissions", e);
            }
        }
        return "---------";
    }

    public static String getFileOwner(File file) {
        if (rootAvailable) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("stat -c %U:%G \"" + file.getAbsolutePath() + "\"\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null) {
                    return line.trim();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file owner", e);
            }
        }
        return "root:root";
    }
}
