package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependenciesAdapter;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogModDependenciesBinding;

import java.util.Collections;
import java.util.List;

public class ModDependenciesDialog extends FullScreenDialog {
    private final DialogModDependenciesBinding binding = DialogModDependenciesBinding.inflate(getLayoutInflater());

    public ModDependenciesDialog(@NonNull Context context, ModDependencies.SelectedMod mod, List<ModDependencies> mData, Runnable downloadRunnable) {
        super(context);

        this.setCancelable(false);
        this.setContentView(binding.getRoot());
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
        binding.titleView.setText(context.getString(R.string.profile_mods_dependencies_dialog_title, mod.modName));
        binding.downloadButton.setText(context.getString(R.string.profile_mods_dependencies_dialog_this_mod, mod.modName));

        Collections.sort(mData);
        ModDependenciesAdapter adapter = new ModDependenciesAdapter(mod, mData);
        adapter.setOnItemCLickListener(this::dismiss);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        binding.recyclerView.setAdapter(adapter);

        binding.closeButton.setOnClickListener(v -> this.dismiss());
        binding.downloadButton.setOnClickListener(v -> {
            PojavApplication.sExecutorService.execute(downloadRunnable);
            this.dismiss();
        });
    }
}
