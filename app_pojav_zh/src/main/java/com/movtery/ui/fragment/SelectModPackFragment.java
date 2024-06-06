package com.movtery.ui.fragment;

import static com.movtery.utils.PojavZHTools.copyFileInBackground;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavApplication;
import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.SearchModFragment;

import java.io.File;

public class SelectModPackFragment extends Fragment {
    public static final String TAG = "SelectModPackFragment";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private File modPackFile;

    public SelectModPackFragment(){
        super(R.layout.fragment_select_modpack);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension(null),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            modPackFile = copyFileInBackground(requireContext(), result, Tools.DIR_CACHE.getAbsolutePath());

                            PojavZHTools.DIR_GAME_MODPACK = modPackFile.getAbsolutePath();
                            ExtraCore.setValue(ExtraConstants.INSTALL_LOCAL_MODPACK, true);
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.zh_modpack_button_search_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, true);
                bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, null);
                Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, bundle);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.zh_modpack_button_local_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Toast.makeText(requireActivity(), getString(R.string.zh_select_modpack_local_tip), Toast.LENGTH_SHORT).show();
                openDocumentLauncher.launch(null);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
