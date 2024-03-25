package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

public class SelectModPackFragment extends Fragment {
    public static final String TAG = "SelectModPackFragment";

    public SelectModPackFragment(){
        super(R.layout.fragment_select_modpack);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.zh_modpack_button_search_modpack).setOnClickListener(v -> Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, false, null));
        view.findViewById(R.id.zh_modpack_button_local_modpack).setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.INSTALL_LOCAL_MODPACK, true));
    }
}
