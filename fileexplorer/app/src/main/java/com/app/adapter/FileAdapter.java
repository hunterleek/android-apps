package com.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.model.FileItem;
import com.app.model.StoragePartition;
import com.app.util.RootUtils;
import com.app.util.PartitionManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Context context;
    private List<FileItem> fileList;
    private OnFileClickListener listener;
    private boolean isGridView;
    private boolean showHiddenFiles;
    private boolean selectMode;

    public interface OnFileClickListener {
        void onFileClick(FileItem file);
        void onFileLongClick(FileItem file);
        void onOptionsClick(FileItem file, View view);
    }

    public FileAdapter(Context context, List<FileItem> fileList, OnFileClickListener listener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
        this.showHiddenFiles = true; // Show hidden files by default
    }

    public void setGridView(boolean gridView) {
        isGridView = gridView;
        notifyDataSetChanged();
    }

    public void setShowHiddenFiles(boolean show) {
        showHiddenFiles = show;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isGridView ? R.layout.item_grid : R.layout.item_file;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem file = fileList.get(position);
        File actualFile = file.getFile();

        holder.checkSelect.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        holder.checkSelect.setChecked(file.isSelected());

        if (isGridView && !actualFile.isDirectory()) {
            holder.imageIcon.setVisibility(View.GONE);
            loadThumbnail(holder.imageThumbnail, actualFile);
        } else {
            holder.imageThumbnail.setVisibility(View.GONE);
            holder.imageIcon.setVisibility(View.VISIBLE);
            setFileIcon(holder.imageIcon, actualFile);
        }

        holder.textName.setText(actualFile.getName());

        if (actualFile.isDirectory()) {
            String count = getDirectoryCount(actualFile) + " items";
            holder.textSize.setText(count);
            holder.textDate.setText("");
        } else {
            holder.textSize.setText(StoragePartition.formatSize(getFileSize(actualFile)));
            holder.textDate.setText(formatDate(getFileModified(actualFile)));
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectMode) {
                file.setSelected(!file.isSelected());
                notifyItemChanged(position);
            } else {
                listener.onFileClick(file);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onFileLongClick(file);
            return true;
        });

        if (holder.btnOptions != null) {
            holder.btnOptions.setOnClickListener(v -> listener.onOptionsClick(file, v));
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private void setFileIcon(ImageView imageView, File file) {
        if (file.isDirectory()) {
            imageView.setImageResource(R.drawable.ic_folder);
        } else {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp")) {
                imageView.setImageResource(R.drawable.ic_image);
            } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi") ||
                       name.endsWith(".mov") || name.endsWith(".webm") || name.endsWith(".3gp")) {
                imageView.setImageResource(R.drawable.ic_video_file);
            } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") ||
                       name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".wma")) {
                imageView.setImageResource(R.drawable.ic_audio_file);
            } else if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") ||
                       name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".bz2")) {
                imageView.setImageResource(R.drawable.ic_archive);
            } else if (name.endsWith(".apk")) {
                imageView.setImageResource(R.drawable.ic_apk);
            } else if (name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx") ||
                       name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".ppt") ||
                       name.endsWith(".pptx") || name.endsWith(".txt") || name.endsWith(".rtf")) {
                imageView.setImageResource(R.drawable.ic_document);
            } else {
                imageView.setImageResource(R.drawable.ic_folder);
            }
        }
    }

    private void loadThumbnail(ImageView imageView, File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
            name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp")) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.ic_image);
                    }
                } catch (Exception e) {
                    imageView.setImageResource(R.drawable.ic_image);
                }
            });
        } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi")) {
            imageView.setImageResource(R.drawable.ic_video_file);
        } else {
            imageView.setImageResource(R.drawable.ic_folder);
        }
    }

    private int getDirectoryCount(File directory) {
        if (!directory.canRead()) {
            if (RootUtils.isRootAvailable()) {
                return RootUtils.listFilesInRoot(directory).size();
            }
            return 0;
        }
        File[] files = directory.listFiles();
        return files != null ? files.length : 0;
    }

    private long getFileSize(File file) {
        if (file.canRead()) {
            return file.length();
        }
        return RootUtils.getFileSize(file);
    }

    private long getFileModified(File file) {
        if (file.canRead()) {
            return file.lastModified();
        }
        return RootUtils.getFileModifiedTime(file);
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageIcon, imageThumbnail;
        TextView textName, textSize, textDate;
        CheckBox checkSelect;
        View btnOptions;

        ViewHolder(View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textName = itemView.findViewById(R.id.textName);
            textSize = itemView.findViewById(R.id.textSize);
            textDate = itemView.findViewById(R.id.textDate);
            checkSelect = itemView.findViewById(R.id.checkSelect);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }
}
