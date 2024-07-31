package com.movtery.pojavzh.ui.subassembly.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.movtery.pojavzh.utils.anim.AnimUtils;

import net.kdt.pojavlaunch.R;

public class SearchView {
    private final View parentView, mainView;
    private EditText mSearchEditText;
    private SearchListener searchListener;
    private ShowSearchResultsListener showSearchResultsListener;
    private SearchAsynchronousUpdatesListener searchAsynchronousUpdatesListener;

    public SearchView(View parentView, View mainView) {
        this.parentView = parentView;
        this.mainView = mainView;

        init();
    }

    private void init() {
        mSearchEditText = mainView.findViewById(R.id.zh_search_edit_text);
        ImageButton mSearchButton = mainView.findViewById(R.id.zh_search_search_button);
        CheckBox mShowSearchResultsOnly = mainView.findViewById(R.id.zh_search_show_search_results_only);
        CheckBox mCaseSensitive = mainView.findViewById(R.id.zh_search_case_sensitive);
        TextView searchCountText = mainView.findViewById(R.id.zh_search_text);

        mSearchButton.setOnClickListener(v -> search(searchCountText, mCaseSensitive.isChecked()));
        mShowSearchResultsOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (showSearchResultsListener != null) showSearchResultsListener.onSearch(isChecked);
            if (!mSearchEditText.getText().toString().isEmpty())
                search(searchCountText, mCaseSensitive.isChecked());
        });

        DraggableView draggableView = new DraggableView(mainView, new DraggableView.AttributesFetcher() {
            @Override
            public DraggableView.ScreenPixels getScreenPixels() {
                return new DraggableView.ScreenPixels(0, 0, parentView.getWidth() - mainView.getWidth(),
                        parentView.getHeight() - mainView.getHeight());
            }

            @Override
            public int[] get() {
                return new int[]{(int) mainView.getX(), (int) mainView.getY()};
            }

            @Override
            public void set(int x, int y) {
                mainView.setX(x);
                mainView.setY(y);
            }
        });
        draggableView.init();
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
        boolean isVisible = mainView.getVisibility() == View.VISIBLE;
        AnimUtils.setVisibilityAnim(mainView, !isVisible, 150);
    }

    public void close() {
        if (mainView.getVisibility() != View.GONE) {
            AnimUtils.setVisibilityAnim(mainView, false, 150);
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
