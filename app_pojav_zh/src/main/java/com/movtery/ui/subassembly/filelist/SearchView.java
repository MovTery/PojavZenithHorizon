package com.movtery.ui.subassembly.filelist;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;

public class SearchView {
    private final View view;
    private EditText mSearchEditText;
    private SearchListener searchListener;
    private ShowSearchResultsListener showSearchResultsListener;
    private SearchAsynchronousUpdatesListener searchAsynchronousUpdatesListener;

    public SearchView(View view) {
        this.view = view;

        init();
    }

    private void init() {
        mSearchEditText = view.findViewById(R.id.zh_search_edit_text);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_search_button);
        CheckBox mShowSearchResultsOnly = view.findViewById(R.id.zh_search_show_search_results_only);
        CheckBox mCaseSensitive = view.findViewById(R.id.zh_search_case_sensitive);
        TextView searchCountText = view.findViewById(R.id.zh_search_text);

        mSearchButton.setOnClickListener(v -> search(searchCountText, mCaseSensitive.isChecked()));
        mShowSearchResultsOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (showSearchResultsListener != null) showSearchResultsListener.onSearch(isChecked);
            if (!mSearchEditText.getText().toString().isEmpty()) search(searchCountText, mCaseSensitive.isChecked());
        });
    }

    private void search(TextView searchCountText, boolean caseSensitive) {
        int searchCount;
        String string = mSearchEditText.getText().toString();
        if (searchListener != null) {
            searchCount = searchListener.onSearch(string, caseSensitive);
            searchCountText.setText(searchCountText.getContext().getString(R.string.zh_search_count, searchCount));
            if (searchCount != 0) searchCountText.setVisibility(View.VISIBLE);
        } else if (searchAsynchronousUpdatesListener != null) {
            searchAsynchronousUpdatesListener.onSearch(searchCountText, string, caseSensitive);
        }
    }

    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    public void setAsynchronousUpdatesListener(SearchAsynchronousUpdatesListener listener) {
        this.searchAsynchronousUpdatesListener = listener;
    }

    public void setShowSearchResultsListener(ShowSearchResultsListener listener) {
        this.showSearchResultsListener = listener;
    }

    public void setVisibility() {
        boolean isVisible = view.getVisibility() == View.VISIBLE;
        PojavZHTools.setVisibilityAnim(view, !isVisible, 150);
    }

    public void close() {
        if (view.getVisibility() != View.GONE) {
            PojavZHTools.setVisibilityAnim(view, false, 150);
        }
    }

    public interface SearchListener {
        int onSearch(String string, boolean caseSensitive);
    }

    public interface SearchAsynchronousUpdatesListener {
        void onSearch(TextView searchCount, String string, boolean caseSensitive);
    }

    public interface ShowSearchResultsListener {
        void onSearch(boolean show);
    }
}
