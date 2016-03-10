package com.helpshift.view;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;
import com.helpshift.app.SimpleMenuItem;

@TargetApi(14)
public class SimpleMenuItemCompat {
    private static boolean isAboveICS = (VERSION.SDK_INT >= 14);

    public interface QueryTextActions {
        boolean queryTextChanged(String str);

        boolean queryTextSubmitted(String str);
    }

    public interface MenuItemActions {
        boolean menuItemCollapsed();

        boolean menuItemExpanded();
    }

    public interface MenuItemChangedListener {
        void visibilityChanged(boolean z);
    }

    public static View getActionView(MenuItem menuItem) {
        if (menuItem instanceof SimpleMenuItem) {
            return ((SimpleMenuItem) menuItem).getActionView();
        }
        return MenuItemCompat.getActionView(menuItem);
    }
}
