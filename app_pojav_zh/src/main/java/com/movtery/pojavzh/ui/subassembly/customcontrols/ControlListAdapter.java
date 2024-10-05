package com.movtery.pojavzh.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.dialog.ControlInfoDialog;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.ItemControlListViewBinding;
import net.kdt.pojavlaunch.databinding.ItemFileListViewBinding;

import java.util.List;

public class ControlListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_VALID = 0;
    private static final int VIEW_TYPE_INVALID = 1;
    private final List<ControlItemBean> mData;
    private OnItemClickListener mOnItemClickListener;

    public ControlListAdapter(List<ControlItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_VALID) {
            return new ValidViewHolder(ItemControlListViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        } else {
            return new InvalidViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ControlItemBean controlItemBean = this.mData.get(position);
        if (getItemViewType(position) == VIEW_TYPE_VALID) {
            ((ValidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(controlItemBean.controlInfoData.fileName);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onLongClick(controlItemBean.controlInfoData.fileName);
                    return true;
                }
                return false;
            });
        } else {
            ((InvalidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onInvalidItemClick(controlItemBean.controlInfoData.fileName);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (this.mData != null) ? this.mData.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (this.mData.get(position).isInvalid) ? VIEW_TYPE_INVALID : VIEW_TYPE_VALID;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String name);

        void onLongClick(String name);

        void onInvalidItemClick(String name);
    }

    public static class InvalidViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final ItemFileListViewBinding binding;

        public InvalidViewHolder(@NonNull ItemFileListViewBinding binding) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            this.binding = binding;
            binding.zhFileCheck.setVisibility(View.GONE);
            binding.zhFileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_disabled));
        }

        public void setData(ControlItemBean controlItemBean) {
            String text = StringUtils.insertSpace(context.getString(R.string.zh_controls_info_invalid), controlItemBean.controlInfoData.fileName);
            binding.zhFileName.setText(text);

            //设置文本字体
            binding.zhFileName.setTextColor(Color.rgb(255, 60, 60));
            binding.zhFileName.setTypeface(null, Typeface.BOLD);
            binding.zhFileName.setTextSize(14);
        }
    }

    public class ValidViewHolder extends RecyclerView.ViewHolder {
        private final Context mContext;
        private final ItemControlListViewBinding binding;

        public ValidViewHolder(@NonNull ItemControlListViewBinding binding) {
            super(binding.getRoot());
            mContext = binding.getRoot().getContext();
            this.binding = binding;
        }

        public void setData(ControlItemBean controlItemBean) {
            ControlInfoData controlInfoData = controlItemBean.controlInfoData;

            binding.infoButton.setOnClickListener(v -> {
                ControlInfoDialog controlInfoDialog = new ControlInfoDialog(mContext, () -> runOnUiThread(ControlListAdapter.this::notifyDataSetChanged), controlInfoData);
                controlInfoDialog.show();
            });

            //初始化控制布局名称，如果为空，那么将设置为文件名
            if (!controlInfoData.name.isEmpty() && !controlInfoData.name.equals("null")) {
                if (controlInfoData.name.equals("control.default.title.text")) {
                    controlInfoData.name = mContext.getString(R.string.zh_controls_info_default_title);
                }
                binding.title.setText(controlInfoData.name);
                String fileNameString = StringUtils.insertSpace(mContext.getString(R.string.zh_controls_info_file_name), controlInfoData.fileName);
                binding.fileName.setVisibility(View.VISIBLE);
                binding.fileName.setText(fileNameString);
            } else {
                binding.title.setText(controlInfoData.fileName);
                binding.fileName.setVisibility(View.GONE);
            }

            //设置高亮
            int color = controlItemBean.isHighlighted ?
                    Color.rgb(69, 179, 162) :
                    binding.title.getResources().getColor(R.color.primary_text, binding.title.getContext().getTheme());
            binding.title.setTextColor(color);

            //初始化作者名，如果没有填写，那么就隐藏它
            if (!controlInfoData.author.isEmpty() && !controlInfoData.author.equals("null")) {
                String authorString = StringUtils.insertSpace(mContext.getString(R.string.zh_controls_info_author), controlInfoData.author);
                binding.author.setVisibility(View.VISIBLE);
                binding.author.setText(authorString);
            } else {
                binding.author.setVisibility(View.GONE);
            }

            //初始化版本
            if (!controlInfoData.version.isEmpty() && !controlInfoData.version.equals("null")) {
                String versionString = StringUtils.insertSpace(mContext.getString(R.string.zh_controls_info_version), controlInfoData.version);
                binding.version.setVisibility(View.VISIBLE);
                binding.version.setText(versionString);
            } else {
                binding.version.setVisibility(View.GONE);
            }

            //初始化描述说明
            if (!controlInfoData.desc.isEmpty() && !controlInfoData.desc.equals("null")) {
                if (controlInfoData.desc.equals("control.default.desc.text")) {
                    controlInfoData.desc = mContext.getString(R.string.zh_controls_info_default_desc);
                }
                binding.desc.setText(controlInfoData.desc);
            } else {
                binding.desc.setText(R.string.zh_controls_info_no_info);
            }
        }
    }
}
