package com.movtery.pojavzh.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.ui.dialog.ControlInfoDialog;

import java.util.List;

public class ControlListAdapter extends RecyclerView.Adapter<ControlListAdapter.InnerHolder> {
    private final List<ControlItemBean> mData;
    private OnItemClickListener mOnItemClickListener;

    public ControlListAdapter(List<ControlItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public ControlListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_control_list_view, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ControlListAdapter.InnerHolder holder, int position) {
        holder.setData(this.mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (this.mData != null) {
            return this.mData.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String name);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private int mPosition;
        private String mName;
        private final TextView mTitle, mAuthor, mVersion, mFileName, mDesc;
        private final Button mInfo;
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.zh_control_title);
            mAuthor = itemView.findViewById(R.id.zh_control_author);
            mVersion = itemView.findViewById(R.id.zh_control_version);
            mFileName = itemView.findViewById(R.id.zh_control_file_name);
            mDesc = itemView.findViewById(R.id.zh_control_desc);
            mInfo = itemView.findViewById(R.id.zh_control_info_button);

            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(mPosition, mName));
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setData(ControlItemBean controlItemBean, int position) {
            this.mPosition = position;
            ControlInfoData controlInfoData = controlItemBean.getControlInfoData();
            this.mName = controlInfoData.fileName;

            mInfo.setOnClickListener(v -> {
                @SuppressLint("NotifyDataSetChanged")
                ControlInfoDialog controlInfoDialog = new ControlInfoDialog(mInfo.getContext(), () -> runOnUiThread(ControlListAdapter.this::notifyDataSetChanged), controlInfoData);
                controlInfoDialog.show();
            });

            //初始化控制布局名称，如果为空，那么将设置为文件名
            if (controlInfoData.name != null && !controlInfoData.name.isEmpty() && !controlInfoData.name.equals("null")) {
                mTitle.setText(controlInfoData.name);
                String fileNameString = mFileName.getContext().getString(R.string.zh_controls_info_file_name) + controlInfoData.fileName;
                mFileName.setVisibility(View.VISIBLE);
                mFileName.setText(fileNameString);
            } else {
                mTitle.setText(controlInfoData.fileName);
                mFileName.setVisibility(View.GONE);
            }

            //设置高亮
            int color;
            if (controlItemBean.isHighlighted()) {
                color = Color.rgb(69, 179, 162);
            } else {
                color = mTitle.getResources().getColor(R.color.primary_text, mTitle.getContext().getTheme());
            }
            mTitle.setTextColor(color);

            //初始化作者名，如果没有填写，那么就隐藏它
            if (controlInfoData.author != null && !controlInfoData.author.isEmpty() && !controlInfoData.author.equals("null")) {
                String authorString = mAuthor.getContext().getString(R.string.zh_controls_info_author) + controlInfoData.author;
                mAuthor.setVisibility(View.VISIBLE);
                mAuthor.setText(authorString);
            } else {
                mAuthor.setVisibility(View.GONE);
            }

            //初始化版本
            if (controlInfoData.version != null && !controlInfoData.version.isEmpty() && !controlInfoData.version.equals("null")) {
                String authorString = mVersion.getContext().getString(R.string.zh_controls_info_version) + controlInfoData.version;
                mVersion.setVisibility(View.VISIBLE);
                mVersion.setText(authorString);
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
}
