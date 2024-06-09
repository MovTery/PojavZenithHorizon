package com.movtery.ui.subassembly.filelist;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;

public class SearchView {
    private final View view;
    private final FileRecyclerView fileRecyclerView;
    private EditText mSearchEditText;

    public SearchView(FileRecyclerView fileRecyclerView, View view) {
        this.view = view;
        this.fileRecyclerView = fileRecyclerView;

        init();
    }

    private void init() {
        mSearchEditText = view.findViewById(R.id.zh_search_edit_text);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_search_button);
        CheckBox mShowSearchResultsOnly = view.findViewById(R.id.zh_search_show_search_results_only);

        mSearchButton.setOnClickListener(v -> search());
        mShowSearchResultsOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fileRecyclerView.setShowSearchResultsOnly(isChecked);
            search();
        });
    }

    private void search() {
        fileRecyclerView.searchFiles(mSearchEditText.getText().toString());
    }

    public void setVisibility() {
        boolean isVisible = view.getVisibility() == View.VISIBLE;
        PojavZHTools.fadeAnim(view, 0, isVisible ? 1 : 0, isVisible ? 0 : 1, 150,
                () -> runOnUiThread(() -> view.setVisibility(isVisible ? View.GONE : View.VISIBLE)));
    }

    public void close() {
        if (view.getVisibility() != View.GONE) {
            PojavZHTools.fadeAnim(view, 0, 1, 0, 150,
                    () -> runOnUiThread(() -> view.setVisibility(View.GONE)));
        }
    }
}
