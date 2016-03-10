package com.mobcrush.mobcrush.ui;

import android.support.v4.app.Fragment;
import android.view.View;

public abstract class ScrollTabHolderFragment extends Fragment implements ScrollTabHolder {
    protected ScrollTabHolder mScrollTabHolder;

    public void setScrollTabHolder(ScrollTabHolder scrollTabHolder) {
        this.mScrollTabHolder = scrollTabHolder;
    }

    public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
    }
}
