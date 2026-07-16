package com.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.R;
import com.app.model.FileItem;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Context context;
    private List<FileItem> files;
    private OnFileClickListener listener;
    private boolean showHiddenFiles = true;

    public interface OnFileClickListener {
        void onFileClick(FileItem file);
        void onFileLongClick(FileItem file);
        void onOptionsClick(FileItem file, View view);
    }

    public FileAdapter(Context context, List<FileItem> files, OnFileClickListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
    }

    public void setShowHiddenFiles(boolean show) { this.showHiddenFiles = show; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FileItem item = files.get(position);
        File f = item.getFile();
        h.name.setText(f.getName());
        h.icon.setImageResource(f.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_document);
        if (f.isDirectory()) {
            File[] list = f.listFiles();
            h.size.setText((list != null ? list.length : 0) + " items");
        } else {
            h.size.setText(formatSize(f.length()));
        }
        h.date.setText(formatDate(f.lastModified()));
        h.itemView.setOnClickListener(v -> listener.onFileClick(item));
        h.itemView.setOnLongClickListener(v -> { listener.onFileLongClick(item); return true; });
        h.icon.setOnClickListener(v -> listener.onOptionsClick(item, v));
        h.itemView.setBackgroundColor(item.isSelected() ? 0xFF333333 : 0xFF000000);
    }

    @Override
    public int getItemCount() { return files.size(); }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024L * 1024 * 1024) return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private String formatDate(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(time));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, size, date;
        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.imageIcon);
            name = v.findViewById(R.id.textName);
            size = v.findViewById(R.id.textSize);
            date = v.findViewById(R.id.textDate);
        }
    }
}
