package com.helpshift.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.DefaultRetryPolicy;
import com.helpshift.D.attr;
import com.helpshift.D.dimen;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.Log;
import com.helpshift.util.Xml;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemActions;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemChangedListener;
import com.helpshift.view.SimpleMenuItemCompat.QueryTextActions;
import com.helpshift.widget.SimpleSearchView;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class ActionBarHelperBase extends ActionBarHelper {
    private static final String MENU_ATTR_ACTION_LAYOUT = "actionLayout";
    private static final String MENU_ATTR_ACTION_VIEW_CLASS = "actionViewClass";
    private static final String MENU_ATTR_ID = "id";
    private static final String MENU_ATTR_SHOW_AS_ACTION = "showAsAction";
    private static final String MENU_RES_NAMESPACE = "http://schemas.android.com/apk/res/android";
    private static final String TAG = ActionBarHelper.class.getName();
    private boolean actionBarInitialised = false;
    private Set<Integer> actionItemIds = new HashSet();
    private Map<Integer, Integer> actionItemIdsToActionLayoutLookup = new HashMap();
    private Map<Integer, String> actionItemIdsToActionViewClassLookup = new HashMap();
    private LayoutInflater mInflater;
    private Map<Integer, MenuItemActions> menuItemActionsMap = new HashMap();
    private boolean progressVisible = false;
    private Map<Integer, QueryTextActions> queryTextActionsMap = new HashMap();
    protected boolean viewExpanded = false;

    public static class HomeView extends LinearLayout {
        private Context mContext;
        private ImageView mIconView;

        public HomeView(Context context) {
            super(context);
            this.mContext = context;
        }

        public HomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mContext = context;
        }

        public void setIcon(Drawable icon) {
            this.mIconView.setImageDrawable(icon);
        }

        protected void onFinishInflate() {
            super.onFinishInflate();
            this.mIconView = (ImageView) findViewById(id.hs__actionbar_compat_home);
        }
    }

    private class WrappedMenuInflater extends MenuInflater {
        MenuInflater inflater;

        public WrappedMenuInflater(Context context, MenuInflater superMenuInflater) {
            super(context);
            this.inflater = superMenuInflater;
        }

        public void inflate(int menuRes, Menu menu) {
            loadActionBarMetadata(menuRes);
            this.inflater.inflate(menuRes, menu);
            for (int i = 0; i < menu.size(); i++) {
                try {
                    SimpleMenuItem item = (SimpleMenuItem) menu.getItem(i);
                    int itemId = item.getItemId();
                    if (ActionBarHelperBase.this.actionItemIdsToActionViewClassLookup.containsKey(Integer.valueOf(itemId)) && ((String) ActionBarHelperBase.this.actionItemIdsToActionViewClassLookup.get(Integer.valueOf(itemId))).equals("android.widget.SearchView")) {
                        View searchView = (SimpleSearchView) ActionBarHelperBase.this.mInflater.inflate(layout.hs__simple_search_view, null);
                        searchView.setId(itemId);
                        item.setActionView(searchView);
                    }
                    if (ActionBarHelperBase.this.actionItemIdsToActionLayoutLookup.containsKey(Integer.valueOf(item.getItemId()))) {
                        View view = ActionBarHelperBase.this.mInflater.inflate(((Integer) ActionBarHelperBase.this.actionItemIdsToActionLayoutLookup.get(Integer.valueOf(item.getItemId()))).intValue(), null);
                        view.setId(itemId);
                        item.setActionView(view);
                    }
                } catch (ClassCastException e) {
                    Log.v(ActionBarHelperBase.TAG, "ClassCastException on hardware menu button click", e);
                }
            }
        }

        private void loadActionBarMetadata(int menuRes) {
            XmlResourceParser parser = null;
            try {
                parser = ActionBarHelperBase.this.mActivity.getResources().getXml(menuRes);
                int eventType = parser.getEventType();
                boolean eof = false;
                while (!eof) {
                    switch (eventType) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            eof = true;
                            break;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            if (!parser.getName().equals("item")) {
                                break;
                            }
                            int itemId = parser.getAttributeResourceValue(ActionBarHelperBase.MENU_RES_NAMESPACE, ActionBarHelperBase.MENU_ATTR_ID, 0);
                            if (itemId == 0) {
                                break;
                            }
                            int showAsAction = parser.getAttributeIntValue(ActionBarHelperBase.MENU_RES_NAMESPACE, ActionBarHelperBase.MENU_ATTR_SHOW_AS_ACTION, -1);
                            if (!((showAsAction & 2) == 0 && (showAsAction & 1) == 0)) {
                                int actionLayout = parser.getAttributeResourceValue(ActionBarHelperBase.MENU_RES_NAMESPACE, ActionBarHelperBase.MENU_ATTR_ACTION_LAYOUT, 0);
                                if (actionLayout != 0) {
                                    ActionBarHelperBase.this.actionItemIdsToActionLayoutLookup.put(Integer.valueOf(itemId), Integer.valueOf(actionLayout));
                                }
                                ActionBarHelperBase.this.actionItemIds.add(Integer.valueOf(itemId));
                            }
                            String actionViewClass = parser.getAttributeValue(ActionBarHelperBase.MENU_RES_NAMESPACE, ActionBarHelperBase.MENU_ATTR_ACTION_VIEW_CLASS);
                            if (actionViewClass == null) {
                                break;
                            }
                            ActionBarHelperBase.this.actionItemIdsToActionViewClassLookup.put(Integer.valueOf(itemId), actionViewClass);
                            break;
                        default:
                            break;
                    }
                    eventType = parser.next();
                }
                if (parser != null) {
                    parser.close();
                }
            } catch (XmlPullParserException e) {
                throw new InflateException("Error inflating menu XML", e);
            } catch (IOException e2) {
                throw new InflateException("Error inflating menu XML", e2);
            } catch (Throwable th) {
                if (parser != null) {
                    parser.close();
                }
            }
        }
    }

    public ActionBarHelperBase(Activity activity) {
        super(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mActivity.requestWindowFeature(7);
        this.mInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
    }

    public void onPostCreate(Bundle savedInstanceState) {
        int i;
        super.onPostCreate(savedInstanceState);
        this.mActivity.getWindow().setFeatureInt(7, layout.hs__actionbar_compat);
        setupActionBar();
        SimpleMenu menu = new SimpleMenu(this.mActivity);
        this.mActivity.onCreatePanelMenu(0, menu);
        this.mActivity.onPrepareOptionsMenu(menu);
        for (i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (this.actionItemIds.contains(Integer.valueOf(menuItem.getItemId()))) {
                addActionItemCompatFromMenuItem(menuItem);
            }
        }
        for (i = 0; i < menu.size(); i++) {
            menuItem = menu.getItem(i);
            if (this.actionItemIds.contains(Integer.valueOf(menuItem.getItemId()))) {
                addActionItemCompatOnTextListener(menuItem);
                addActionItemCompatExpandListener(menuItem);
            }
        }
    }

    private void setupActionBar() {
        ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat != null) {
            ApplicationInfo appInfo = this.mActivity.getApplicationInfo();
            SimpleMenu tempMenu = new SimpleMenu(this.mActivity);
            SimpleMenuItem homeItem = new SimpleMenuItem(tempMenu, 16908332, 0, appInfo.name);
            homeItem.setIcon(Xml.getLogoResourceValue(this.mActivity));
            addActionItemCompatFromMenuItem(homeItem);
            LayoutParams springLayoutParams = new LayoutParams(0, -1);
            springLayoutParams.weight = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            TextView titleView = new TextView(this.mActivity, null, attr.hs__actionbarCompatTitleStyle);
            titleView.setLayoutParams(springLayoutParams);
            titleView.setText(this.mActivity.getTitle());
            titleView.setId(16908310);
            actionBarCompat.addView(titleView);
            addActionItemCompatFromMenuItem(new SimpleMenuItem(tempMenu, 16908301, 0, appInfo.name));
            this.actionBarInitialised = true;
        }
    }

    private void addActionItemCompatFromMenuItem(MenuItem item) {
        ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat != null) {
            View actionView = ((SimpleMenuItem) item).getActionView();
            if (actionView != null) {
                actionBarCompat.addView(actionView);
                return;
            }
            switch (item.getItemId()) {
                case 16908301:
                    addProgressActionItem(actionBarCompat);
                    return;
                case 16908332:
                    addHomeActionItem(actionBarCompat, item);
                    return;
                default:
                    addActionItem(actionBarCompat, item);
                    return;
            }
        }
    }

    private void addHomeActionItem(ViewGroup actionBarCompat, final MenuItem item) {
        HomeView homeView = (HomeView) this.mInflater.inflate(layout.hs__actionbar_compat_home, actionBarCompat, false);
        if (homeView != null) {
            homeView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (ActionBarHelperBase.this.viewExpanded) {
                        ActionBarHelperBase.this.collapseActionView(null);
                    } else {
                        ActionBarHelperBase.this.mActivity.onMenuItemSelected(0, item);
                    }
                }
            });
            homeView.setClickable(true);
            homeView.setFocusable(true);
            homeView.setIcon(item.getIcon());
            actionBarCompat.addView(homeView);
        }
    }

    private void addActionItem(ViewGroup actionBarCompat, final MenuItem item) {
        int actionButtonWidth = (int) this.mActivity.getResources().getDimension(dimen.hs__actionbar_compat_button_width);
        final ImageButton actionButton = new ImageButton(this.mActivity, null, attr.hs__actionbarCompatItemBaseStyle);
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(actionButtonWidth, -1));
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ActionBarHelperBase.this.mActivity.onMenuItemSelected(0, item);
            }
        });
        actionBarCompat.addView(actionButton);
        ((SimpleMenuItem) item).setOnMenuItemChangedListener(new MenuItemChangedListener() {
            public void visibilityChanged(boolean visible) {
                if (visible) {
                    actionButton.setVisibility(0);
                } else {
                    actionButton.setVisibility(8);
                }
            }
        });
    }

    private void addProgressActionItem(ViewGroup actionBarCompat) {
        int buttonWidthId = dimen.hs__actionbar_compat_button_width;
        int buttonHeightId = dimen.hs__actionbar_compat_height;
        ProgressBar indicator = new ProgressBar(this.mActivity, null, attr.hs__actionbarCompatProgressIndicatorStyle);
        int buttonWidth = this.mActivity.getResources().getDimensionPixelSize(buttonWidthId);
        int buttonHeight = this.mActivity.getResources().getDimensionPixelSize(buttonHeightId);
        int progressIndicatorWidth = buttonWidth / 2;
        LayoutParams indicatorLayoutParams = new LayoutParams(progressIndicatorWidth, progressIndicatorWidth);
        indicatorLayoutParams.setMargins((buttonWidth - progressIndicatorWidth) / 2, (buttonHeight - progressIndicatorWidth) / 2, (buttonWidth - progressIndicatorWidth) / 2, 0);
        indicator.setLayoutParams(indicatorLayoutParams);
        if (this.progressVisible) {
            indicator.setVisibility(0);
        } else {
            indicator.setVisibility(8);
        }
        indicator.setId(id.hs__actionbar_compat_item_refresh_progress);
        actionBarCompat.addView(indicator);
    }

    public ViewGroup getActionBarCompat() {
        return (ViewGroup) this.mActivity.findViewById(id.hs__actionbar_compat);
    }

    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return new WrappedMenuInflater(this.mActivity, superMenuInflater);
    }

    public void setDisplayHomeAsUpEnabled(boolean b) {
    }

    public void setTitle(String title) {
        this.mActivity.setTitle(title);
        if (this.actionBarInitialised) {
            TextView titleText = (TextView) getActionBarCompat().findViewById(16908310);
            if (titleText != null) {
                titleText.setText(title);
            }
        }
    }

    public void supportRequestWindowFeature(int featureId) {
    }

    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (this.actionBarInitialised) {
            View progress = getActionBarCompat().findViewById(id.hs__actionbar_compat_item_refresh_progress);
            if (visible) {
                progress.setVisibility(0);
            } else {
                progress.setVisibility(8);
            }
        }
        this.progressVisible = visible;
    }

    public void setNavigationMode(int navigationMode) {
    }

    public void setOnQueryTextListener(MenuItem menuItem, QueryTextActions queryTextActions) {
        this.queryTextActionsMap.put(Integer.valueOf(menuItem.getItemId()), queryTextActions);
    }

    public void setOnActionExpandListener(MenuItem menuItem, MenuItemActions menuItemActions) {
        this.menuItemActionsMap.put(Integer.valueOf(menuItem.getItemId()), menuItemActions);
    }

    private void addActionItemCompatExpandListener(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        final View title = getActionBarCompat().findViewById(16908310);
        final View conversation = getActionBarCompat().findViewById(id.hs__action_report_issue);
        if (this.menuItemActionsMap.containsKey(Integer.valueOf(menuItemId)) && menuItemId == id.hs__action_search) {
            final MenuItemActions itemActions = (MenuItemActions) this.menuItemActionsMap.get(Integer.valueOf(menuItemId));
            ((SimpleSearchView) getActionBarCompat().findViewById(id.hs__action_search)).setOnActionExpandListener(new OnActionExpandListener() {
                public boolean onMenuItemActionExpand(MenuItem item) {
                    title.setVisibility(8);
                    if (conversation != null) {
                        conversation.setVisibility(8);
                    }
                    ActionBarHelperBase.this.viewExpanded = true;
                    return itemActions.menuItemExpanded();
                }

                public boolean onMenuItemActionCollapse(MenuItem item) {
                    title.setVisibility(0);
                    if (conversation != null) {
                        conversation.setVisibility(0);
                    }
                    ActionBarHelperBase.this.viewExpanded = false;
                    return itemActions.menuItemCollapsed();
                }
            });
        }
    }

    protected boolean isViewExpanded() {
        return this.viewExpanded;
    }

    private void addActionItemCompatOnTextListener(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        if (this.queryTextActionsMap.containsKey(Integer.valueOf(menuItemId)) && menuItemId == id.hs__action_search) {
            final QueryTextActions queryTextActions = (QueryTextActions) this.queryTextActionsMap.get(Integer.valueOf(menuItemId));
            ((SimpleSearchView) getActionBarCompat().findViewById(id.hs__action_search)).setQueryTextListener(new OnQueryTextListenerCompat() {
                public boolean onQueryTextSubmit(String query) {
                    return queryTextActions.queryTextSubmitted(query);
                }

                public boolean onQueryTextChange(String newText) {
                    return queryTextActions.queryTextChanged(newText);
                }
            });
        }
    }

    public void collapseActionView(MenuItem menuItem) {
        ((SimpleSearchView) getActionBarCompat().findViewById(id.hs__action_search)).collapseActionView();
    }

    public String getQuery(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        String query = BuildConfig.FLAVOR;
        if (menuItemId == id.hs__action_search) {
            return ((SimpleSearchView) getActionBarCompat().findViewById(id.hs__action_search)).getQuery();
        }
        return query;
    }

    public void clearFocus(MenuItem menuItem) {
        if (menuItem.getItemId() == id.hs__action_search) {
            ((SimpleSearchView) getActionBarCompat().findViewById(id.hs__action_search)).clearFocus();
        }
    }
}
