package com.helpshift.widget;

import android.content.Context;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.helpshift.D.id;
import io.fabric.sdk.android.BuildConfig;

public class SimpleSearchView extends LinearLayout {
    private ImageButton clearButton;
    private InputMethodManager imm = ((InputMethodManager) this.mContext.getSystemService("input_method"));
    private Context mContext;
    private OnActionExpandListener mOnActionExpandListener;
    private OnQueryTextListenerCompat mQueryTextListener;
    private ImageButton searchButton;
    private EditText searchQuery;

    public SimpleSearchView(Context context) {
        super(context);
        this.mContext = context;
    }

    public SimpleSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.searchQuery = (EditText) findViewById(id.hs__search_query);
        this.searchButton = (ImageButton) findViewById(id.hs__search_button);
        this.clearButton = (ImageButton) findViewById(id.hs__search_query_clear);
        this.searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SimpleSearchView.this.menuItemExpanded();
            }
        });
        this.searchQuery.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0) {
                    SimpleSearchView.this.clearButton.setVisibility(0);
                } else {
                    SimpleSearchView.this.clearButton.setVisibility(8);
                }
                SimpleSearchView.this.mQueryTextListener.onQueryTextChange(charSequence.toString());
            }

            public void afterTextChanged(Editable editable) {
            }
        });
        this.clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SimpleSearchView.this.searchQuery.setText(BuildConfig.FLAVOR);
                SimpleSearchView.this.showKeyBoard();
            }
        });
    }

    private void menuItemExpanded() {
        showKeyBoard();
        this.searchQuery.setVisibility(0);
        this.searchButton.setVisibility(8);
        this.searchQuery.requestFocus();
        this.mOnActionExpandListener.onMenuItemActionExpand(null);
    }

    private void showKeyBoard() {
        this.searchQuery.requestFocus();
        this.searchQuery.postDelayed(new Runnable() {
            public void run() {
                SimpleSearchView.this.imm.showSoftInput(SimpleSearchView.this.searchQuery, 0);
            }
        }, 200);
    }

    private void hideKeyboard() {
        this.searchQuery.clearFocus();
        this.imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void setQueryTextListener(OnQueryTextListenerCompat queryTextListener) {
        this.mQueryTextListener = queryTextListener;
    }

    public void setOnActionExpandListener(OnActionExpandListener onActionExpandListener) {
        this.mOnActionExpandListener = onActionExpandListener;
    }

    public void collapseActionView() {
        hideKeyboard();
        this.searchQuery.setVisibility(8);
        this.clearButton.setVisibility(8);
        this.searchButton.setVisibility(0);
        this.searchQuery.setText(BuildConfig.FLAVOR);
        this.mOnActionExpandListener.onMenuItemActionCollapse(null);
    }

    public String getQuery() {
        return this.searchQuery.getText().toString();
    }

    public void clearFocus() {
        hideKeyboard();
    }
}
