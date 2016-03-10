package com.mobcrush.mobcrush.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;
import android.view.View;

public class DividerItemDecoration extends ItemDecoration {
    private int mColumnsCount;
    private Drawable mDivider;
    private boolean mHorizontalDelimiters;
    private boolean mWithHeader;

    public DividerItemDecoration(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{16843284});
        this.mDivider = a.getDrawable(0);
        a.recycle();
    }

    public DividerItemDecoration(Drawable divider, boolean withHeader) {
        this.mDivider = divider;
        this.mWithHeader = withHeader;
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (this.mDivider != null) {
            if (view.getVisibility() == 8) {
                outRect.right = 0;
                outRect.left = 0;
                outRect.bottom = 0;
                outRect.top = 0;
                outRect.bottom = this.mDivider.getIntrinsicHeight();
            } else if (getOrientation(parent) == 1) {
                int delWidth;
                int correctionWidth;
                int i;
                int width = this.mDivider.getIntrinsicWidth();
                int pos = parent.getChildPosition(view);
                if (this.mColumnsCount > 0) {
                    delWidth = ((this.mColumnsCount + 1) * width) / this.mColumnsCount;
                } else {
                    delWidth = 0;
                }
                if (this.mColumnsCount > 0) {
                    correctionWidth = (delWidth - width) / (this.mColumnsCount - 1);
                } else {
                    correctionWidth = 0;
                }
                if (pos == 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                outRect.top = (i & this.mWithHeader) != 0 ? 0 : this.mDivider.getIntrinsicHeight();
                outRect.bottom = 0;
                if (this.mColumnsCount == 0) {
                    outRect.left = width;
                    outRect.right = width;
                } else if (this.mHorizontalDelimiters) {
                    boolean isFirstInRow;
                    boolean isLastInRow;
                    int i2;
                    if (pos % this.mColumnsCount == 0) {
                        isFirstInRow = true;
                    } else {
                        isFirstInRow = false;
                    }
                    if ((pos + 1) % this.mColumnsCount == 0) {
                        isLastInRow = true;
                    } else {
                        isLastInRow = false;
                    }
                    if (isFirstInRow) {
                        i2 = width;
                    } else {
                        i2 = (isLastInRow ? -correctionWidth : correctionWidth) + (width / 2);
                    }
                    outRect.left = i2;
                    if (!isLastInRow) {
                        i2 = width / 2;
                        if (isFirstInRow) {
                            correctionWidth = -correctionWidth;
                        }
                        width = i2 + correctionWidth;
                    }
                    outRect.right = width;
                }
            } else {
                outRect.left = this.mDivider.getIntrinsicWidth();
            }
        }
    }

    public void onDrawOver(Canvas c, RecyclerView parent) {
        if (this.mDivider == null) {
            super.onDrawOver(c, parent);
            return;
        }
        int left;
        int childCount;
        int i;
        if (getOrientation(parent) == 1) {
            left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            childCount = parent.getChildCount();
            for (i = 1; i < childCount; i++) {
                View child = parent.getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int top = child.getTop() - params.topMargin;
                this.mDivider.setBounds(left, top, right, top + this.mDivider.getIntrinsicHeight());
                this.mDivider.draw(c);
            }
        }
        if (getOrientation(parent) != 1 || this.mHorizontalDelimiters) {
            top = parent.getPaddingTop();
            int bottom = parent.getHeight() - parent.getPaddingBottom();
            childCount = parent.getChildCount();
            for (i = 1; i < childCount; i++) {
                child = parent.getChildAt(i);
                params = (LayoutParams) child.getLayoutParams();
                left = child.getLeft() - params.leftMargin;
                this.mDivider.setBounds(left, top, left + this.mDivider.getIntrinsicWidth(), bottom);
                this.mDivider.draw(c);
            }
        }
    }

    public void enableHorizontalDelimiters(boolean enable, int columnsCount) {
        this.mHorizontalDelimiters = enable;
        this.mColumnsCount = columnsCount;
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
        }
        throw new IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.");
    }
}
