package com.movtery.pojavzh.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.ui.dialog.ControlInfoDialog;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

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
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        if (viewType == VIEW_TYPE_VALID) {
            view = layoutInflater.inflate(R.layout.item_control_list_view, viewGroup, false);
            return new ValidViewHolder(view);
        } else {
            view = layoutInflater.inflate(R.layout.item_file_list_view, viewGroup, false);
            return new InvalidViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ControlItemBean controlItemBean = this.mData.get(position);
        if (getItemViewType(position) == VIEW_TYPE_VALID) {
            ((ValidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(controlItemBean.getControlInfoData().fileName);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onLongClick(controlItemBean.getControlInfoData().fileName);
                    return true;
                }
                return false;
            });
        } else {
            ((InvalidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onInvalidItemClick(controlItemBean.getControlInfoData().fileName);
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
        return (this.mData.get(position).isInvalid()) ? VIEW_TYPE_INVALID : VIEW_TYPE_VALID;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String name);
        void onLongClick(String name);
        void onInvalidItemClick(String name);
    }

    public class ValidViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle, mAuthor, mVersion, mFileName, mDesc;
        private final Button mInfo;

        public ValidViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.zh_control_title);
            mAuthor = itemView.findViewById(R.id.zh_control_author);
            mVersion = itemView.findViewById(R.id.zh_control_version);
            mFileName = itemView.findViewById(R.id.zh_control_file_name);
            mDesc = itemView.findViewById(R.id.zh_control_desc);
            mInfo = itemView.findViewById(R.id.zh_control_info_button);
        }

        public void setData(ControlItemBean controlItemBean) {
            ControlInfoData controlInfoData = controlItemBean.getControlInfoData();

            mInfo.setOnClickListener(v -> {
                ControlInfoDialog controlInfoDialog = new ControlInfoDialog(mInfo.getContext(), () -> runOnUiThread(ControlListAdapter.this::notifyDataSetChanged), controlInfoData);
                controlInfoDialog.show();
            });

            //初始化控制布局名称，如果为空，那么将设置为文件名
            if (controlInfoData.name != null && !controlInfoData.name.isEmpty() && !controlInfoData.name.equals("null")) {
                mTitle.setText(controlInfoData.name);
                String fileNameString = StringUtils.insertSpace(mFileName.getContext().getString(R.string.zh_controls_info_file_name), controlInfoData.fileName);
                mFileName.setVisibility(View.VISIBLE);
                mFileName.setText(fileNameString);
            } else {
                mTitle.setText(controlInfoData.fileName);
                mFileName.setVisibility(View.GONE);
            }

            //设置高亮
            int color = controlItemBean.isHighlighted() ? Color.rgb(69, 179, 162) : mTitle.getResources().getColor(R.color.primary_text, mTitle.getContext().getTheme());
            mTitle.setTextColor(color);

            //初始化作者名，如果没有填写，那么就隐藏它
            if (controlInfoData.author != null && !controlInfoData.author.isEmpty() && !controlInfoData.author.equals("null")) {
                String authorString = StringUtils.insertSpace(mAuthor.getContext().getString(R.string.zh_controls_info_author), controlInfoData.author);
                mAuthor.setVisibility(View.VISIBLE);
                mAuthor.setText(authorString);
            } else {
                mAuthor.setVisibility(View.GONE);
            }

            //初始化版本
            if (controlInfoData.version != null && !controlInfoData.version.isEmpty() && !controlInfoData.version.equals("null")) {
                String versionString = StringUtils.insertSpace(mVersion.getContext().getString(R.string.zh_controls_info_version), controlInfoData.version);
                mVersion.setVisibility(View.VISIBLE);
                mVersion.setText(versionString);
            } else {
                mVersion.setVisibility(View.GONE);
            }

            //初始化描述说明
            if (controlInfoData.desc != null && !controlInfoData.desc.isEmpty() && !controlInfoData.desc.equals("null")) {
                mDesc.setText(controlInfoData.desc);
            } else {
                mDesc.setText(R.string.zh_controls_info_no_info);
            }
        }
    }

    public static class InvalidViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final TextView title;

        public InvalidViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            itemView.findViewById(R.id.zh_file_check).setVisibility(View.GONE);
            ImageView imageView = itemView.findViewById(R.id.zh_file_image);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_disabled));
            title = itemView.findViewById(R.id.zh_file_name);
        }

        public void setData(ControlItemBean controlItemBean) {
            String text = StringUtils.insertSpace(context.getString(R.string.zh_controls_info_invalid), controlItemBean.getControlInfoData().fileName);
            title.setText(text);

            //设置文本字体
            title.setTextColor(Color.rgb(255, 60, 60));
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(14);
        }
    }
}
