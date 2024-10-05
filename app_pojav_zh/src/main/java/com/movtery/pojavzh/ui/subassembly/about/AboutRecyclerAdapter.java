package com.movtery.pojavzh.ui.subassembly.about;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.ItemAboutViewBinding;

import java.util.List;

public class AboutRecyclerAdapter extends RecyclerView.Adapter<AboutRecyclerAdapter.InnerHolder> {
    private final List<AboutItemBean> itemBeans;

    public AboutRecyclerAdapter(List<AboutItemBean> data) {
        this.itemBeans = data;
    }

    @NonNull
    @Override
    public AboutRecyclerAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InnerHolder(ItemAboutViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final ItemAboutViewBinding binding;

        public InnerHolder(@NonNull ItemAboutViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(AboutItemBean data) {
            binding.imageView.setImageDrawable(data.getIcon());
            binding.titleView.setText(data.getTitle());
            binding.descView.setText(data.getDesc());

            if (data.getButtonBean() != null) {
                AboutItemBean.AboutItemButtonBean buttonBean = data.getButtonBean();
                String buttonName = buttonBean.getName();

                binding.buttonView.setText(buttonName);

                binding.buttonView.setOnClickListener(v -> {
                    String url = buttonBean.getUrl();
                    Tools.openURL(buttonBean.getActivity(), url);
                });
            } else {
                binding.buttonView.setVisibility(View.GONE);
            }
        }
    }
}