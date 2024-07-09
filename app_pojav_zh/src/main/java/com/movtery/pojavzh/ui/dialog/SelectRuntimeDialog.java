package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.multirt.RTRecyclerViewAdapter;

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
        mTitleText.setText(R.string.multirt_config_title);
        ImageButton mCloseButton = findViewById(R.id.zh_select_item_close_button);

        mCloseButton.setOnClickListener(v -> this.dismiss());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setListener(RuntimeSelectedListener listener) {
        RTRecyclerViewAdapter adapter = new RTRecyclerViewAdapter(listener);
        recyclerView.setAdapter(adapter);
    }

    public interface RuntimeSelectedListener {
        void onSelected(String jreName);
    }
}
