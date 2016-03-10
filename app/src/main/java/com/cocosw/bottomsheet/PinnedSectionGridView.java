package com.cocosw.bottomsheet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

class PinnedSectionGridView extends GridView {
    private int mAvailableWidth;
    private int mColumnWidth;
    private int mHorizontalSpacing;
    private int mNumColumns;

    public PinnedSectionGridView(Context context) {
        super(context);
    }

    public PinnedSectionGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedSectionGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNumColumns(int numColumns) {
        this.mNumColumns = numColumns;
        super.setNumColumns(numColumns);
    }

    public int getNumColumns() {
        return this.mNumColumns;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.mHorizontalSpacing = horizontalSpacing;
        super.setHorizontalSpacing(horizontalSpacing);
    }

    public int getHorizontalSpacing() {
        return this.mHorizontalSpacing;
    }

    public void setColumnWidth(int columnWidth) {
        this.mColumnWidth = columnWidth;
        super.setColumnWidth(columnWidth);
    }

    public int getColumnWidth() {
        return this.mColumnWidth;
    }

    public int getAvailableWidth() {
        return this.mAvailableWidth != 0 ? this.mAvailableWidth : getWidth();
    }
}
