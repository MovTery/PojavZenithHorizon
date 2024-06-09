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
    private EditText mSearchEditText;
    private SearchListener searchListener;
    private ShowSearchResultsListener showSearchResultsListener;

    public SearchView(View view) {
        this.view = view;

        init();
    }

    private void init() {
        mSearchEditText = view.findViewById(R.id.zh_search_edit_text);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_search_button);
        CheckBox mShowSearchResultsOnly = view.findViewById(R.id.zh_search_show_search_results_only);

        mSearchButton.setOnClickListener(v -> search());
        mShowSearchResultsOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (showSearchResultsListener != null) showSearchResultsListener.onSearch(isChecked);
            if (!mSearchEditText.getText().toString().isEmpty()) search();
        });
    }

    private void search() {
        if (searchListener != null) searchListener.onSearch(mSearchEditText.getText().toString());
    }

    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    public void setShowSearchResultsListener(ShowSearchResultsListener listener) {
        this.showSearchResultsListener = listener;
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

    public interface SearchListener {
        void onSearch(String string);
    }

    public interface ShowSearchResultsListener {
        void onSearch(boolean show);
    }
}
