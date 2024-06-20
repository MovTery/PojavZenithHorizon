package com.movtery.pojavzh.ui.subassembly.about;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.util.List;

public class AboutRecyclerAdapter extends RecyclerView.Adapter<AboutRecyclerAdapter.InnerHolder> {
    private final List<AboutItemBean> itemBeans;

    public AboutRecyclerAdapter(List<AboutItemBean> data) {
        this.itemBeans = data;
    }

    @NonNull
    @Override
    public AboutRecyclerAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_view, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutRecyclerAdapter.InnerHolder holder, int position) {
        holder.setData(this.itemBeans.get(position));
    }

    @Override
    public int getItemCount() {
        if (this.itemBeans != null) {
            return this.itemBeans.size();
        }
        return 0;
    }

    public static class InnerHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView mIcon;
        private final TextView mTitle, mDesc;
        private final Button mButton;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            this.mIcon = itemView.findViewById(R.id.zh_about_image);
            this.mTitle = itemView.findViewById(R.id.zh_about_title);
            this.mDesc = itemView.findViewById(R.id.zh_about_desc);
            this.mButton = itemView.findViewById(R.id.zh_about_button);
        }

        public void setData(AboutItemBean data) {
            this.mIcon.setImageDrawable(data.getIcon());
            this.mTitle.setText(data.getTitle());
            this.mDesc.setText(data.getDesc());

            if (data.getButtonBean() != null) {
                AboutItemBean.AboutItemButtonBean buttonBean = data.getButtonBean();
                String buttonName = buttonBean.getName();

                this.mButton.setText(buttonName);

                this.mButton.setOnClickListener(v -> {
                    String url = buttonBean.getUrl();
                    Tools.openURL(buttonBean.getActivity(), url);
                });
            } else {
                this.mButton.setVisibility(View.GONE);
            }
        }
    }
}