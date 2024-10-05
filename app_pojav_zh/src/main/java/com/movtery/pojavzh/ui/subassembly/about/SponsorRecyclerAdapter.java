package com.movtery.pojavzh.ui.subassembly.about;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.ItemSponsorViewBinding;

import java.util.List;

public class SponsorRecyclerAdapter extends RecyclerView.Adapter<SponsorRecyclerAdapter.Holder> {
    private final List<SponsorItemBean> mData;

    public SponsorRecyclerAdapter(List<SponsorItemBean> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public SponsorRecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSponsorViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final ItemSponsorViewBinding binding;

        public Holder(@NonNull ItemSponsorViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setData(SponsorItemBean itemBean) {
            binding.nameView.setText(itemBean.getName());
            binding.timeView.setText(itemBean.getTime());
            binding.amountView.setText(String.format("￥%s", itemBean.getAmount()));

            if (itemBean.getAmount() >= 12.0f) {
                binding.getRoot().setBackground(binding.getRoot().getContext().getDrawable(R.drawable.background_sponsor_advanced));
            }
        }
    }
}
