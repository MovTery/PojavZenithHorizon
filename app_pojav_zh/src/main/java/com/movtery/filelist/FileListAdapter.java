package com.movtery.filelist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.InnerHolder> {
    private final List<FileItemBean> mData;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public FileListAdapter(List<FileItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public FileListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_file_list_view, null);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, File file, String name);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, File file, String name);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private int mPosition;
        private File mFile;
        private String mName;
        private final ImageView icon;
        private final TextView name;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.zh_file_image);
            name = itemView.findViewById(R.id.zh_file_name);

            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(mPosition, mFile, mName));
            }
            if (mOnItemLongClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    mOnItemLongClickListener.onItemLongClick(mPosition, mFile, mName);
                    return true;
                });
            }
        }

        public void setData(FileItemBean fileItemBean, int position) {
            this.mPosition = position;
            this.mFile = fileItemBean.getFile();
            this.mName = fileItemBean.getName();
            this.icon.setImageDrawable(fileItemBean.getImage());
            this.name.setText(fileItemBean.getName() == null ? fileItemBean.getFile().getName() : fileItemBean.getName());
        }
    }
}
