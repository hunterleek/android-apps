package com.app.model;

import java.io.File;

public class FileItem {
    private File file;
    private boolean isSelected;
    private boolean isRootFile;

    public FileItem(File file) {
        this.file = file;
        this.isSelected = false;
        this.isRootFile = file.getAbsolutePath().startsWith("/");
    }

    public File getFile() {
        return file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isRootFile() {
        return isRootFile;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
}
