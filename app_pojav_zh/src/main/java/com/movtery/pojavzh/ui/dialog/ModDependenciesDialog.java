package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependenciesAdapter;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import java.util.Collections;
import java.util.List;

public class ModDependenciesDialog extends FullScreenDialog {

    public ModDependenciesDialog(@NonNull Context context, ModDependencies.SelectedMod mod, List<ModDependencies> mData, Runnable downloadRunnable) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_mod_dependencies);
        init(context, mod, mData, downloadRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);

            //隐藏状态栏
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void init(Context context, ModDependencies.SelectedMod mod, List<ModDependencies> mData, Runnable downloadRunnable) {
        TextView mTitle = findViewById(R.id.zh_mod_dependencies_title);
        RecyclerView modRecyclerView = findViewById(R.id.zh_mod_dependencies);
        Button mCloseButton = findViewById(R.id.zh_mod_dependencies_close_button);
        Button mDownloadButton = findViewById(R.id.zh_mod_dependencies_download_button);

        mTitle.setText(context.getString(R.string.zh_profile_mods_dependencies_dialog_title, mod.modName));
        mDownloadButton.setText(context.getString(R.string.zh_profile_mods_dependencies_dialog_this_mod, mod.modName));

        Collections.sort(mData);
        ModDependenciesAdapter adapter = new ModDependenciesAdapter(mod, mData);
        adapter.setOnItemCLickListener(this::dismiss);
        modRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        modRecyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        modRecyclerView.setAdapter(adapter);

        mCloseButton.setOnClickListener(v -> this.dismiss());
        mDownloadButton.setOnClickListener(v -> {
            PojavApplication.sExecutorService.execute(downloadRunnable);
            this.dismiss();
        });
    }
}
