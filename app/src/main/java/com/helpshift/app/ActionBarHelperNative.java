package com.helpshift.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build.VERSION;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import com.helpshift.D.menu;
import com.helpshift.D.string;
import com.helpshift.HSApiData;
import com.helpshift.HSStorage;
import com.helpshift.Log;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemActions;
import com.helpshift.view.SimpleMenuItemCompat.QueryTextActions;
import io.fabric.sdk.android.BuildConfig;
import org.json.JSONException;

@TargetApi(14)
public class ActionBarHelperNative extends ActionBarHelper {
    public static final String TAG = "HelpShiftDebug";
    private HSApiData data;
    private boolean indeterminateVisibility = false;
    private MenuItem refreshItem = null;
    private String screenType;
    private HSStorage storage;

    public ActionBarHelperNative(Activity activity) {
        super(activity);
    }

    public void setDisplayHomeAsUpEnabled(boolean b) {
        if (isTablet() && isDialogUIForTabletsEnabled()) {
            this.mActivity.getActionBar().setIcon(17170445);
            this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(!isRoot());
            return;
        }
        this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(b);
    }

    private boolean isTablet() {
        if (TextUtils.isEmpty(this.screenType)) {
            this.screenType = this.mActivity.getResources().getString(string.hs__screen_type);
        }
        return !this.screenType.equals("phone");
    }

    private boolean isDialogUIForTabletsEnabled() {
        if (this.data == null || this.storage == null) {
            this.data = new HSApiData(this.mActivity);
            this.storage = this.data.storage;
        }
        Boolean enableDialogUIForTablets = Boolean.valueOf(false);
        try {
            enableDialogUIForTablets = Boolean.valueOf(this.storage.getAppConfig().optBoolean("enableDialogUIForTablets"));
        } catch (JSONException e) {
            Log.d(TAG, "isDialogUIForTabletsEnabled : ", e);
        }
        return enableDialogUIForTablets.booleanValue();
    }

    private boolean isRoot() {
        return this.mActivity.getIntent().getExtras().getBoolean("isRoot", false);
    }

    public void setTitle(String title) {
        this.mActivity.getActionBar().setTitle(title);
    }

    public void supportRequestWindowFeature(int featureId) {
        this.mActivity.requestWindowFeature(featureId);
    }

    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        this.indeterminateVisibility = visible;
        if (isLollipop()) {
            setRefreshActionButtonState(visible);
        } else {
            this.mActivity.setProgressBarIndeterminateVisibility(visible);
        }
    }

    public void setNavigationMode(int navigationMode) {
        this.mActivity.getActionBar().setNavigationMode(navigationMode);
    }

    public void setOnQueryTextListener(MenuItem menuItem, final QueryTextActions queryTextActions) {
        View actionView = menuItem.getActionView();
        if (actionView instanceof SearchView) {
            ((SearchView) actionView).setOnQueryTextListener(new OnQueryTextListener() {
                public boolean onQueryTextSubmit(String query) {
                    return queryTextActions.queryTextSubmitted(query);
                }

                public boolean onQueryTextChange(String newText) {
                    return queryTextActions.queryTextChanged(newText);
                }
            });
        }
    }

    public void setOnActionExpandListener(MenuItem menuItem, final MenuItemActions itemActions) {
        menuItem.setOnActionExpandListener(new OnActionExpandListener() {
            public boolean onMenuItemActionExpand(MenuItem item) {
                return itemActions.menuItemExpanded();
            }

            public boolean onMenuItemActionCollapse(MenuItem item) {
                return itemActions.menuItemCollapsed();
            }
        });
    }

    public void collapseActionView(MenuItem menuItem) {
        if (menuItem.getActionView() instanceof SearchView) {
            MenuItemCompat.collapseActionView(menuItem);
        }
    }

    public void setQueryHint(MenuItem menuItem, String hint) {
        View actionView = menuItem.getActionView();
        if (actionView instanceof SearchView) {
            ((SearchView) actionView).setQueryHint(hint);
        }
    }

    public String getQuery(MenuItem menuItem) {
        View actionView = menuItem.getActionView();
        String query = BuildConfig.FLAVOR;
        if (actionView instanceof SearchView) {
            return ((SearchView) actionView).getQuery().toString();
        }
        return query;
    }

    public void clearFocus(MenuItem menuItem) {
        if (menuItem.getActionView() != null) {
            menuItem.getActionView().clearFocus();
        }
    }

    public void setIcon(int resId) {
        this.mActivity.getActionBar().setIcon(resId);
    }

    private boolean isLollipop() {
        return VERSION.SDK_INT >= 21;
    }

    private void setRefreshActionButtonState(boolean visible) {
        if (this.refreshItem != null) {
            this.refreshItem.setVisible(visible);
        }
    }

    public void setupIndeterminateProgressBar(Menu menu, MenuInflater inflater) {
        if (isLollipop()) {
            inflater.inflate(menu.hs__actionbar_indeterminate_progress, menu);
            this.refreshItem = menu.findItem(16908301);
            setRefreshActionButtonState(this.indeterminateVisibility);
        }
    }
}
