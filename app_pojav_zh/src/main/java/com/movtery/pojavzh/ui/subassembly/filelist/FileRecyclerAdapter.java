package com.movtery.pojavzh.ui.subassembly.filelist;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.InnerHolder> {
    private final List<FileItemBean> mData;
    private final List<FileItemBean> selectedFiles = new ArrayList<>();
    private final int textColor = Color.rgb(69, 179, 162);
    private boolean isMultiSelectMode = false;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnMultiSelectListener mOnMultiSelectListener;

    public FileRecyclerAdapter(List<FileItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public FileRecyclerAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list_view, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileRecyclerAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    private void toggleSelection(FileItemBean itemBean, CheckBox checkBox) {
        if (itemBean.isCanCheck) {
            if (selectedFiles.contains(itemBean)) {
                selectedFiles.remove(itemBean);
                checkBox.setChecked(false);
            } else {
                selectedFiles.add(itemBean);
                checkBox.setChecked(true);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMultiSelectMode(boolean multiSelectMode) {
        isMultiSelectMode = multiSelectMode;
        if (!multiSelectMode) {
            selectedFiles.clear(); // 退出多选模式时重置选择的文件
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAllFiles(boolean selectAll) {
        selectedFiles.clear();
        if (selectAll) { //全选时遍历全部item设置选择状态
            for (FileItemBean item : mData) {
                if (item.isCanCheck) {
                    selectedFiles.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<FileItemBean> getSelectedFiles() {
        return selectedFiles;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnMultiSelectListener(OnMultiSelectListener listener) {
        this.mOnMultiSelectListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, FileItemBean itemBean);
    }

    public interface OnMultiSelectListener {
        void onMultiSelect(List<FileItemBean> itemBeans);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, FileItemBean itemBean);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final CheckBox checkBox;
        private int mPosition;
        private FileItemBean mFileItemBean;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.zh_file_image);
            name = itemView.findViewById(R.id.zh_file_name);
            checkBox = itemView.findViewById(R.id.zh_file_check);

            checkBox.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    toggleSelection(mFileItemBean, checkBox);
                }
            });
            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(v -> {
                    if (isMultiSelectMode) {
                        toggleSelection(mFileItemBean, checkBox);
                    } else {
                        mOnItemClickListener.onItemClick(mPosition, mFileItemBean);
                    }
                });
            }
            itemView.setOnLongClickListener(v -> {
                if (isMultiSelectMode) {
                    if (mOnMultiSelectListener != null)
                        mOnMultiSelectListener.onMultiSelect(getSelectedFiles());
                } else {
                    if (mOnItemLongClickListener != null)
                        mOnItemLongClickListener.onItemLongClick(mPosition, mFileItemBean);
                }
                return true;
            });
        }

        public void setData(FileItemBean fileItemBean, int position) {
            this.mPosition = position;
            this.mFileItemBean = fileItemBean;
            this.icon.setImageDrawable(fileItemBean.image);
            this.name.setText(fileItemBean.name == null ? fileItemBean.file.getName() : fileItemBean.name);

            int color;
            if (fileItemBean.isHighlighted) {
                color = textColor; //设置高亮
            } else {
                color = this.name.getResources().getColor(R.color.black_or_white, this.name.getContext().getTheme());
            }
            this.name.setTextColor(color);

            if (fileItemBean.isCanCheck) {
                checkBox.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE);
                checkBox.setChecked(selectedFiles.contains(fileItemBean));
            } else {
                checkBox.setVisibility(View.GONE);
            }
        }
    }
}
