package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.mod.modpack.install.InstallExtra;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.SearchModFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.io.File;

public class SelectModPackFragment extends FragmentWithAnim implements TaskCountListener {
    public static final String TAG = "SelectModPackFragment";
    private View mMainView;
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private File modPackFile;
    private boolean mTasksRunning;

    public SelectModPackFragment() {
        super(R.layout.fragment_select_modpack);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension(null),
                result -> {
                    if (result != null && !mTasksRunning) {
                        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                .setView(R.layout.view_task_running)
                                .setCancelable(false)
                                .show();
                        PojavApplication.sExecutorService.execute(() -> {
                            modPackFile = copyFileInBackground(requireContext(), result, Tools.DIR_CACHE.getAbsolutePath());
                            ExtraCore.setValue(ZHExtraConstants.INSTALL_LOCAL_MODPACK, new InstallExtra(true, modPackFile.getAbsolutePath(), dialog));
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;
        ProgressKeeper.addTaskCountListener(this);

        Button mSearch = view.findViewById(R.id.zh_modpack_button_search_modpack);
        mSearch.setOnClickListener(v -> {
            if (!mTasksRunning) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, true);
                bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, null);
                ZHTools.swapFragmentWithAnim(this, SearchModFragment.class, SearchModFragment.TAG, bundle);
            } else {
                ViewAnimUtils.setViewAnim(mSearch, Techniques.Shake);
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        Button mLocal = view.findViewById(R.id.zh_modpack_button_local_modpack);
        mLocal.setOnClickListener(v -> {
            if (!mTasksRunning) {
                Toast.makeText(requireActivity(), getString(R.string.zh_select_modpack_local_tip), Toast.LENGTH_SHORT).show();
                openDocumentLauncher.launch(null);
            } else {
                ViewAnimUtils.setViewAnim(mLocal, Techniques.Shake);
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = !(taskCount == 0);
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.BounceInDown);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.FadeOutUp);
        super.slideOut(listener);
    }
}
