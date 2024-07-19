package com.movtery.pojavzh.ui.subassembly.about;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.util.List;

public class SponsorRecyclerAdapter extends RecyclerView.Adapter<SponsorRecyclerAdapter.Holder> {
    private final List<SponsorItemBean> mData;

    public SponsorRecyclerAdapter(List<SponsorItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public SponsorRecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sponsor_view, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SponsorRecyclerAdapter.Holder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final TextView name, time, amount;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.mainView = itemView;

            this.name = itemView.findViewById(R.id.zh_sponsor_name);
            this.time = itemView.findViewById(R.id.zh_sponsor_time);
            this.amount = itemView.findViewById(R.id.zh_sponsor_amount);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setData(SponsorItemBean itemBean) {
            this.name.setText(itemBean.getName());
            this.time.setText(itemBean.getTime());
            this.amount.setText(String.format("￥%s", itemBean.getAmount()));

            if (itemBean.getAmount() >= 12.0f) {
                mainView.setBackground(mainView.getContext().getDrawable(R.drawable.background_sponsor_advanced));
            }
        }
    }
}
