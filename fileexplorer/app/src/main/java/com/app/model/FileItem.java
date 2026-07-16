package com.app.model;

import java.io.File;

public class FileItem {
    private File file;
    private boolean selected;

    public FileItem(File file) { this.file = file; }
    public File getFile() { return file; }
    public String getName() { return file.getName(); }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
}
