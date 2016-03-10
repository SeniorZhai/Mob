package com.cocosw.bottomsheet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.cocosw.bottomsheet.SimpleSectionedGridAdapter.Section;
import com.google.android.exoplayer.C;
import com.helpshift.res.values.HSConsts;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BottomSheet extends Dialog implements DialogInterface {
    private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";
    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private ActionMenu actions;
    private SimpleSectionedGridAdapter adapter;
    private Builder builder;
    private boolean cancelOnSwipeDown = true;
    private boolean cancelOnTouchOutside = true;
    private Drawable close;
    private boolean collapseListIcons;
    private OnDismissListener dismissListener;
    private ActionMenu fullMenuItem;
    private final SparseIntArray hidden = new SparseIntArray();
    private ImageView icon;
    private int limit = -1;
    private GridView list;
    private boolean mInPortrait;
    private boolean mNavBarAvailable;
    private float mSmallestWidthDp;
    private int mStatusBarHeight;
    private ActionMenu menuItem;
    private Drawable more;
    private String moreText;
    private String sNavBarOverride;

    public static class Builder {
        private final Context context;
        private OnDismissListener dismissListener;
        private boolean grid;
        private Drawable icon;
        private int limit;
        private OnClickListener listener;
        private final ActionMenu menu;
        private OnMenuItemClickListener menulistener;
        private int theme;
        private CharSequence title;

        public Builder(@NonNull Activity context) {
            this(context, R.style.BottomSheet_Dialog);
            TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.bottomSheetStyle});
            try {
                this.theme = ta.getResourceId(0, R.style.BottomSheet_Dialog);
            } finally {
                ta.recycle();
            }
        }

        public Builder(Context context, int theme) {
            this.limit = -1;
            this.context = context;
            this.theme = theme;
            this.menu = new ActionMenu(context);
        }

        public Builder sheet(int xmlRes) {
            new MenuInflater(this.context).inflate(xmlRes, this.menu);
            return this;
        }

        public Builder sheet(int id, int iconRes, int textRes) {
            ActionMenuItem item = new ActionMenuItem(this.context, 0, id, 0, 0, this.context.getText(textRes));
            item.setIcon(iconRes);
            this.menu.add(item);
            return this;
        }

        public Builder sheet(int id, @NonNull Drawable icon, @NonNull CharSequence text) {
            ActionMenuItem item = new ActionMenuItem(this.context, 0, id, 0, 0, text);
            item.setIcon(icon);
            this.menu.add(item);
            return this;
        }

        public Builder sheet(int id, int textRes) {
            this.menu.add(0, id, 0, textRes);
            return this;
        }

        public Builder sheet(int id, @NonNull CharSequence text) {
            this.menu.add(0, id, 0, text);
            return this;
        }

        public Builder title(int titleRes) {
            this.title = this.context.getText(titleRes);
            return this;
        }

        @Deprecated
        public Builder remove(int id) {
            this.menu.removeItem(id);
            return this;
        }

        public Builder icon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public Builder icon(int iconRes) {
            this.icon = this.context.getResources().getDrawable(iconRes);
            return this;
        }

        public Builder listener(@NonNull OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder listener(@NonNull OnMenuItemClickListener listener) {
            this.menulistener = listener;
            return this;
        }

        public Builder darkTheme() {
            this.theme = R.style.BottomSheet_Dialog_Dark;
            return this;
        }

        public BottomSheet show() {
            BottomSheet dialog = build();
            dialog.show();
            return dialog;
        }

        public Builder grid() {
            this.grid = true;
            return this;
        }

        public Builder limit(int limitRes) {
            this.limit = this.context.getResources().getInteger(limitRes);
            return this;
        }

        @SuppressLint({"Override"})
        public BottomSheet build() {
            BottomSheet dialog = new BottomSheet(this.context, this.theme);
            dialog.builder = this;
            return dialog;
        }

        public Builder title(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setOnDismissListener(@NonNull OnDismissListener listener) {
            this.dismissListener = listener;
            return this;
        }
    }

    BottomSheet(Context context) {
        super(context, R.style.BottomSheet_Dialog);
    }

    BottomSheet(Context context, int theme) {
        super(context, theme);
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.BottomSheet, R.attr.bottomSheetStyle, 0);
        try {
            this.more = a.getDrawable(R.styleable.BottomSheet_bs_moreDrawable);
            this.close = a.getDrawable(R.styleable.BottomSheet_bs_closeDrawable);
            this.moreText = a.getString(R.styleable.BottomSheet_bs_moreText);
            this.collapseListIcons = a.getBoolean(R.styleable.BottomSheet_bs_collapseListIcons, true);
            if (VERSION.SDK_INT >= 19) {
                WindowManager wm = (WindowManager) context.getSystemService("window");
                this.mInPortrait = context.getResources().getConfiguration().orientation == 1;
                try {
                    Method m = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", new Class[]{String.class});
                    m.setAccessible(true);
                    this.sNavBarOverride = (String) m.invoke(null, new Object[]{"qemu.hw.mainkeys"});
                } catch (Throwable th) {
                    this.sNavBarOverride = null;
                }
                a = context.obtainStyledAttributes(new int[]{16843760});
                try {
                    this.mNavBarAvailable = a.getBoolean(0, false);
                    if ((((Activity) context).getWindow().getAttributes().flags & C.SAMPLE_FLAG_DECODE_ONLY) != 0) {
                        this.mNavBarAvailable = true;
                    }
                    this.mSmallestWidthDp = getSmallestWidthDp(wm);
                    if (this.mNavBarAvailable) {
                        setTranslucentStatus(true);
                    }
                    this.mStatusBarHeight = getInternalDimensionSize(context.getResources(), STATUS_BAR_HEIGHT_RES_NAME);
                } finally {
                    a.recycle();
                }
            }
        } finally {
            a.recycle();
        }
    }

    @SuppressLint({"NewApi"})
    private float getSmallestWidthDp(WindowManager wm) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (VERSION.SDK_INT >= 16) {
            wm.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        return Math.min(((float) metrics.widthPixels) / metrics.density, ((float) metrics.heightPixels) / metrics.density);
    }

    @TargetApi(14)
    private int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        if (VERSION.SDK_INT < 14 || !hasNavBar(context)) {
            return 0;
        }
        String key;
        if (this.mInPortrait) {
            key = NAV_BAR_HEIGHT_RES_NAME;
        } else if (!isNavigationAtBottom()) {
            return 0;
        } else {
            key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
        }
        return getInternalDimensionSize(res, key);
    }

    @TargetApi(14)
    private boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", AbstractSpiCall.ANDROID_CLIENT_TYPE);
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            if (HSConsts.STATUS_INPROGRESS.equals(this.sNavBarOverride)) {
                return false;
            }
            if (HSConsts.STATUS_NEW.equals(this.sNavBarOverride)) {
                return true;
            }
            return hasNav;
        }
        return !ViewConfiguration.get(context).hasPermanentMenuKey();
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int resourceId = res.getIdentifier(key, "dimen", AbstractSpiCall.ANDROID_CLIENT_TYPE);
        if (resourceId > 0) {
            return res.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean isNavigationAtBottom() {
        return this.mSmallestWidthDp >= 600.0f || this.mInPortrait;
    }

    private int getNumColumns() {
        int i = 1;
        try {
            Field numColumns = GridView.class.getDeclaredField("mRequestedNumColumns");
            numColumns.setAccessible(true);
            i = numColumns.getInt(this.list);
        } catch (Exception e) {
        }
        return i;
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        this.cancelOnTouchOutside = cancel;
    }

    public void setCanceledOnSwipeDown(boolean cancel) {
        this.cancelOnSwipeDown = cancel;
    }

    private void init(Context context) {
        setCanceledOnTouchOutside(this.cancelOnTouchOutside);
        final ClosableSlidingLayout mDialogView = (ClosableSlidingLayout) View.inflate(context, R.layout.bottom_sheet_dialog, null);
        setContentView(mDialogView);
        if (!this.cancelOnSwipeDown) {
            mDialogView.swipeable = this.cancelOnSwipeDown;
        }
        mDialogView.setSlideListener(new SlideListener() {
            public void onClosed() {
                BottomSheet.this.dismiss();
            }

            public void onOpened() {
                BottomSheet.this.showFullItems();
            }
        });
        setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                BottomSheet.this.list.setAdapter(BottomSheet.this.adapter);
                BottomSheet.this.list.startLayoutAnimation();
                if (BottomSheet.this.builder.icon == null) {
                    BottomSheet.this.icon.setVisibility(8);
                    return;
                }
                BottomSheet.this.icon.setVisibility(0);
                BottomSheet.this.icon.setImageDrawable(BottomSheet.this.builder.icon);
            }
        });
        int[] location = new int[2];
        mDialogView.getLocationOnScreen(location);
        if (VERSION.SDK_INT >= 19) {
            int i;
            if (location[0] == 0) {
                i = this.mStatusBarHeight;
            } else {
                i = 0;
            }
            mDialogView.setPadding(0, i, 0, 0);
            View childAt = mDialogView.getChildAt(0);
            if (this.mNavBarAvailable) {
                i = getNavigationBarHeight(getContext()) + mDialogView.getPaddingBottom();
            } else {
                i = 0;
            }
            childAt.setPadding(0, 0, 0, i);
        }
        TextView title = (TextView) mDialogView.findViewById(R.id.bottom_sheet_title);
        if (this.builder.title != null) {
            title.setVisibility(0);
            title.setText(this.builder.title);
        }
        this.icon = (ImageView) mDialogView.findViewById(R.id.bottom_sheet_title_image);
        this.list = (GridView) mDialogView.findViewById(R.id.bottom_sheet_gridview);
        mDialogView.mTarget = this.list;
        if (!this.builder.grid) {
            this.list.setNumColumns(1);
        }
        if (this.builder.grid) {
            for (int i2 = 0; i2 < getMenu().size(); i2++) {
                if (getMenu().getItem(i2).getIcon() == null) {
                    throw new IllegalArgumentException("You must set icon for each items in grid style");
                }
            }
        }
        if (this.builder.limit > 0) {
            this.limit = this.builder.limit * getNumColumns();
        } else {
            this.limit = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }
        mDialogView.setCollapsible(false);
        this.actions = this.builder.menu;
        this.menuItem = this.actions;
        if (getMenu().size() > this.limit) {
            this.fullMenuItem = this.builder.menu;
            this.menuItem = this.builder.menu.clone(this.limit - 1);
            ActionMenuItem item = new ActionMenuItem(context, 0, R.id.bs_more, 0, this.limit - 1, this.moreText);
            item.setIcon(this.more);
            this.menuItem.add(item);
            this.actions = this.menuItem;
            mDialogView.setCollapsible(true);
        }
        this.adapter = new SimpleSectionedGridAdapter(context, new BaseAdapter() {

            class ViewHolder {
                private ImageView image;
                private TextView title;

                ViewHolder() {
                }
            }

            public int getCount() {
                return BottomSheet.this.actions.size() - BottomSheet.this.hidden.size();
            }

            public MenuItem getItem(int position) {
                return BottomSheet.this.actions.getItem(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public int getViewTypeCount() {
                return 1;
            }

            public boolean isEnabled(int position) {
                return getItem(position).isEnabled();
            }

            public boolean areAllItemsEnabled() {
                return false;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) BottomSheet.this.getContext().getSystemService("layout_inflater");
                    if (BottomSheet.this.builder.grid) {
                        convertView = inflater.inflate(R.layout.bs_grid_entry, parent, false);
                    } else {
                        convertView = inflater.inflate(R.layout.bs_list_entry, parent, false);
                    }
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.bs_list_title);
                    holder.image = (ImageView) convertView.findViewById(R.id.bs_list_image);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                for (int i = 0; i < BottomSheet.this.hidden.size(); i++) {
                    if (BottomSheet.this.hidden.valueAt(i) <= position) {
                        position++;
                    }
                }
                MenuItem item = getItem(position);
                holder.title.setText(item.getTitle());
                if (item.getIcon() == null) {
                    holder.image.setVisibility(BottomSheet.this.collapseListIcons ? 8 : 4);
                } else {
                    holder.image.setVisibility(0);
                    holder.image.setImageDrawable(item.getIcon());
                }
                holder.image.setEnabled(item.isEnabled());
                holder.title.setEnabled(item.isEnabled());
                return convertView;
            }
        }, R.layout.bs_list_divider, R.id.headerlayout, R.id.header);
        this.list.setAdapter(this.adapter);
        this.adapter.setGridView(this.list);
        updateSection();
        this.list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (((MenuItem) BottomSheet.this.adapter.getItem(position)).getItemId() == R.id.bs_more) {
                    BottomSheet.this.showFullItems();
                    mDialogView.setCollapsible(false);
                    return;
                }
                if (!((ActionMenuItem) BottomSheet.this.adapter.getItem(position)).invoke()) {
                    if (BottomSheet.this.builder.menulistener != null) {
                        BottomSheet.this.builder.menulistener.onMenuItemClick((MenuItem) BottomSheet.this.adapter.getItem(position));
                    } else if (BottomSheet.this.builder.listener != null) {
                        BottomSheet.this.builder.listener.onClick(BottomSheet.this, ((MenuItem) BottomSheet.this.adapter.getItem(position)).getItemId());
                    }
                }
                BottomSheet.this.dismiss();
            }
        });
        if (this.builder.dismissListener != null) {
            setOnDismissListener(this.builder.dismissListener);
        }
        setListLayout();
    }

    private void updateSection() {
        this.actions.removeInvisible();
        if (!this.builder.grid && this.actions.size() > 0) {
            int groupId = this.actions.getItem(0).getGroupId();
            ArrayList<Section> sections = new ArrayList();
            for (int i = 0; i < this.actions.size(); i++) {
                if (this.actions.getItem(i).getGroupId() != groupId) {
                    groupId = this.actions.getItem(i).getGroupId();
                    sections.add(new Section(i, null));
                }
            }
            if (sections.size() > 0) {
                Section[] s = new Section[sections.size()];
                sections.toArray(s);
                this.adapter.setSections(s);
                return;
            }
            this.adapter.mSections.clear();
        }
    }

    private void showFullItems() {
        if (VERSION.SDK_INT >= 19) {
            Transition changeBounds = new ChangeBounds();
            changeBounds.setDuration(300);
            TransitionManager.beginDelayedTransition(this.list, changeBounds);
        }
        this.actions = this.fullMenuItem;
        updateSection();
        this.adapter.notifyDataSetChanged();
        this.list.setLayoutParams(new LayoutParams(-1, -1));
        this.icon.setVisibility(0);
        this.icon.setImageDrawable(this.close);
        this.icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BottomSheet.this.showShortItems();
            }
        });
        setListLayout();
    }

    private void showShortItems() {
        this.actions = this.menuItem;
        updateSection();
        this.adapter.notifyDataSetChanged();
        setListLayout();
        if (this.builder.icon == null) {
            this.icon.setVisibility(8);
            return;
        }
        this.icon.setVisibility(0);
        this.icon.setImageDrawable(this.builder.icon);
    }

    private boolean hasDivider() {
        return this.adapter.mSections.size() > 0;
    }

    private void setListLayout() {
        if (hasDivider()) {
            this.list.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (VERSION.SDK_INT < 16) {
                        BottomSheet.this.list.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        BottomSheet.this.list.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    View lastChild = BottomSheet.this.list.getChildAt(BottomSheet.this.list.getChildCount() - 1);
                    if (lastChild != null) {
                        BottomSheet.this.list.setLayoutParams(new LayoutParams(-1, (lastChild.getBottom() + lastChild.getPaddingBottom()) + BottomSheet.this.list.getPaddingBottom()));
                    }
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = -2;
        params.gravity = 80;
        TypedArray a = getContext().obtainStyledAttributes(new int[]{16842996});
        try {
            params.width = a.getLayoutDimension(0, -1);
            super.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (BottomSheet.this.dismissListener != null) {
                        BottomSheet.this.dismissListener.onDismiss(dialog);
                    }
                    if (BottomSheet.this.limit != ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
                        BottomSheet.this.showShortItems();
                    }
                }
            });
            getWindow().setAttributes(params);
        } finally {
            a.recycle();
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= 67108864;
        } else {
            winParams.flags &= -67108865;
        }
        win.setAttributes(winParams);
        win.setFlags(C.SAMPLE_FLAG_DECODE_ONLY, C.SAMPLE_FLAG_DECODE_ONLY);
    }

    public Menu getMenu() {
        return this.builder.menu;
    }

    public void invalidate() {
        updateSection();
        this.adapter.notifyDataSetChanged();
        setListLayout();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }
}
