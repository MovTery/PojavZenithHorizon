package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.RTRecyclerViewAdapter;
import net.kdt.pojavlaunch.multirt.Runtime;

import java.util.ArrayList;
import java.util.List;

public class SelectRuntimeDialog extends FullScreenDialog {
    private RecyclerView recyclerView;

    public SelectRuntimeDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_select_item);
        init(context);
    }

    private void init(Context context) {
        recyclerView = findViewById(R.id.zh_select_view);
        TextView mTitleText = findViewById(R.id.zh_select_item_title);
        TextView mMessageText = findViewById(R.id.zh_select_item_message);
        mTitleText.setText(R.string.zh_install_select_jre_environment);
        mMessageText.setText(R.string.zh_install_recommend_use_jre8);
        mMessageText.setVisibility(View.VISIBLE);
        ImageButton mCloseButton = findViewById(R.id.zh_select_item_close_button);

        mCloseButton.setOnClickListener(v -> this.dismiss());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setListener(RuntimeSelectedListener listener) {
        List<Runtime> runtimes = new ArrayList<>(MultiRTUtils.getRuntimes());
        if (!runtimes.isEmpty()) runtimes.add(new Runtime("auto"));
        RTRecyclerViewAdapter adapter = new RTRecyclerViewAdapter(runtimes, listener);
        recyclerView.setAdapter(adapter);
    }

    public interface RuntimeSelectedListener {
        void onSelected(String jreName);
    }
}
