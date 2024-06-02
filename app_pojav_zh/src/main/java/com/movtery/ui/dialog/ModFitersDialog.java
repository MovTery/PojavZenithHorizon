package com.movtery.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.feature.mod.ModLoaderList;
import com.movtery.feature.mod.SearchModPlatform;
import com.movtery.feature.mod.SearchModSort;
import com.movtery.ui.subassembly.versionlist.VersionSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;

import java.util.ArrayList;
import java.util.List;

public class ModFitersDialog extends Dialog {
    private final SearchFilters mSearchFilters;
    private OnApplyButtonClickListener mOnApplyButtonClickListener;

    public ModFitersDialog(@NonNull Context context, SearchFilters mSearchFilters) {
        super(context);
        this.mSearchFilters = mSearchFilters;

        setContentView(R.layout.dialog_mod_filters);
        setCancelable(true);
        init();
    }

    private void init() {
        TextView mSelectedVersion = findViewById(R.id.search_mod_selected_mc_version_textview);
        Button mSelectVersionButton = findViewById(R.id.search_mod_mc_version_button);
        Spinner mSortBy = findViewById(R.id.zh_search_mod_sort);
        Spinner mPlatform = findViewById(R.id.zh_search_mod_platform);
        CheckBox mModloaderForge = findViewById(R.id.zh_search_forge_checkBox);
        CheckBox mModloaderFabric = findViewById(R.id.zh_search_fabric_checkBox);
        CheckBox mModloaderQuilt = findViewById(R.id.zh_search_quilt_checkBox);
        CheckBox mModloaderNeoForge = findViewById(R.id.zh_search_neoforge_checkBox);
        Button mResetButton = findViewById(R.id.search_mod_reset);
        Button mApplyButton = findViewById(R.id.search_mod_apply_filters);

        assert mSelectVersionButton != null;
        assert mSelectedVersion != null;
        assert mApplyButton != null;

        // 打开版本选择弹窗
        mSelectVersionButton.setOnClickListener(v -> {
            SelectVersionDialog selectVersionDialog = new SelectVersionDialog(getContext());
            selectVersionDialog.setOnVersionSelectedListener(new VersionSelectedListener() {
                @Override
                public void onVersionSelected(String version) {
                    mSelectedVersion.setText(version);
                    selectVersionDialog.dismiss();
                }
            });

            selectVersionDialog.show();
        });

        mSelectedVersion.setText(mSearchFilters.mcVersion);

        List<String> platfromList = SearchModPlatform.getIndexList();
        if (mPlatform != null) {
            mPlatform.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_simple_list_1, platfromList));
            mPlatform.setSelection(SearchModPlatform.getIndex(mSearchFilters.platform));

            mPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSearchFilters.platform = SearchModPlatform.getPlatform(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        List<String> sortList = new ArrayList<>(SearchModSort.getIndexList());
        if (mSortBy != null) {
            mSortBy.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_simple_list_1, sortList));
            //默认选中筛选器设置的排序索引
            mSortBy.setSelection(mSearchFilters.sort);

            mSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSearchFilters.sort = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        mModloaderForge.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.getModloaderList().get(0)));
        mModloaderFabric.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.getModloaderList().get(1)));
        mModloaderQuilt.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.getModloaderList().get(2)));
        mModloaderNeoForge.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.getModloaderList().get(3)));

        mModloaderForge.setOnClickListener(v -> {
            String forge = ModLoaderList.getModloaderList().get(0);
            if (mModloaderForge.isChecked() && !mSearchFilters.modloaders.contains(forge)) {
                mSearchFilters.modloaders.add(forge);
            } else mSearchFilters.modloaders.remove(forge);
        });
        mModloaderFabric.setOnClickListener(v -> {
            String fabric = ModLoaderList.getModloaderList().get(1);
            if (mModloaderFabric.isChecked() && !mSearchFilters.modloaders.contains(fabric)) {
                mSearchFilters.modloaders.add(fabric);
            } else mSearchFilters.modloaders.remove(fabric);
        });
        mModloaderQuilt.setOnClickListener(v -> {
            String quilt = ModLoaderList.getModloaderList().get(2);
            if (mModloaderQuilt.isChecked() && !mSearchFilters.modloaders.contains(quilt)) {
                mSearchFilters.modloaders.add(quilt);
            } else mSearchFilters.modloaders.remove(quilt);
        });
        mModloaderNeoForge.setOnClickListener(v -> {
            String neoforge = ModLoaderList.getModloaderList().get(3);
            if (mModloaderNeoForge.isChecked() && !mSearchFilters.modloaders.contains(neoforge)) {
                mSearchFilters.modloaders.add(neoforge);
            } else mSearchFilters.modloaders.remove(neoforge);
        });

        mResetButton.setOnClickListener(v -> {
            mSearchFilters.name = "";
            mSearchFilters.mcVersion = "";
            mSearchFilters.modloaders = new ArrayList<>();
            mSearchFilters.sort = 0;
            mSearchFilters.platform = SearchFilters.ApiPlatform.BOTH;

            //重置控件
            mSelectedVersion.setText("");
            if (mSortBy != null) mSortBy.setSelection(0);
            if (mPlatform != null) mPlatform.setSelection(0);
            mModloaderForge.setChecked(false);
            mModloaderFabric.setChecked(false);
            mModloaderQuilt.setChecked(false);
            mModloaderNeoForge.setChecked(false);
        });

        // Apply the new settings
        mApplyButton.setOnClickListener(v -> {
            mSearchFilters.mcVersion = mSelectedVersion.getText().toString();
            mOnApplyButtonClickListener.onClick();
            this.dismiss();
        });
    }

    public void setOnApplyButtonClickListener(OnApplyButtonClickListener listener) {
        this.mOnApplyButtonClickListener = listener;
    }

    public interface OnApplyButtonClickListener {
        void onClick();
    }
}
