package com.movtery.custommouse;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.List;

public class MouseListAdapter extends RecyclerView.Adapter<MouseListAdapter.InnerHolder> {
    private final List<MouseItemBean> mData;
    private OnItemClickListener mOnItemClickListener;

    public MouseListAdapter(List<MouseItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public MouseListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_mouse_list_view, null);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MouseListAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
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

    public interface OnItemClickListener {
        void onItemClick(File file);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private File mFile;
        private final ImageView icon;
        private final TextView name;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.zh_custom_mouse_image);
            name = itemView.findViewById(R.id.zh_custom_mouse_name);

            itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(mFile));
        }

        public void setData(MouseItemBean mouseItemBean) {
            this.mFile = new File(PojavZHTools.DIR_CUSTOM_MOUSE, mouseItemBean.name);
            this.icon.setImageDrawable(mouseItemBean.mouseIcon);
            this.name.setText(mouseItemBean.name);
        }
    }
}
