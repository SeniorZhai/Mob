package com.helpshift.app;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemActions;
import com.helpshift.view.SimpleMenuItemCompat.QueryTextActions;

public abstract class ActionBarHelper {
    public static final int NAVIGATION_MODE_STANDARD = 0;
    protected Activity mActivity;

    public abstract void clearFocus(MenuItem menuItem);

    public abstract void collapseActionView(MenuItem menuItem);

    public abstract String getQuery(MenuItem menuItem);

    public abstract void setDisplayHomeAsUpEnabled(boolean z);

    public abstract void setNavigationMode(int i);

    public abstract void setOnActionExpandListener(MenuItem menuItem, MenuItemActions menuItemActions);

    public abstract void setOnQueryTextListener(MenuItem menuItem, QueryTextActions queryTextActions);

    public abstract void setSupportProgressBarIndeterminateVisibility(boolean z);

    public abstract void setTitle(String str);

    public abstract void supportRequestWindowFeature(int i);

    public ActionBarHelper(Activity activity) {
        this.mActivity = activity;
    }

    public static ActionBarHelper createInstance(Activity activity) {
        if (VERSION.SDK_INT >= 14) {
            return new ActionBarHelperNative(activity);
        }
        return new ActionBarHelperBase(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onPostCreate(Bundle savedInstanceState) {
    }

    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return superMenuInflater;
    }

    public void setQueryHint(MenuItem menuItem, String hint) {
    }

    public void setIcon(int resId) {
    }

    public void setupIndeterminateProgressBar(Menu menu, MenuInflater inflater) {
    }
}
