package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.cocosw.bottomsheet.BottomSheet;
import com.cocosw.bottomsheet.BottomSheet.Builder;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.ModerationLogicType;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer.RoundedDrawable;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class UsersAdapter extends Adapter<ViewHolder> implements OnClickListener {
    private static final String TAG = "UsersAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static boolean mIgnoreSwitch;
    private Drawable DEFAULT_AVATAR;
    private int LAYOUT_ID = R.layout.item_user;
    private Callback mActionsCallback;
    private WeakReference<FragmentActivity> mActivityRef;
    private int mCurrentHeight;
    private DisplayImageOptions mDio;
    private ArrayList<Integer> mDisabledActionIDs = new ArrayList();
    private Integer mDividerResID;
    private View mHeaderView;
    private ArrayList<User> mListOfSelectedItems = new ArrayList();
    private int mMinimalHeight;
    private ModerationLogicType mModerationLogicType;
    private OnClickListener mOnIconClickListener = new OnClickListener() {
        public void onClick(View view) {
            Object tag = view.getTag();
            if (Network.isLoggedIn() && tag != null && (tag instanceof User)) {
                try {
                    final User user = (User) tag;
                    if (!TextUtils.equals(PreferenceUtility.getUser()._id, user._id)) {
                        final FragmentActivity a = (FragmentActivity) UsersAdapter.this.mActivityRef.get();
                        if (a != null) {
                            int resID = PreferenceUtility.isEmailVerified() ? R.menu.menu_chat : R.menu.menu_chat_for_unverified;
                            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[UsersAdapter.this.mModerationLogicType.ordinal()]) {
                                case UsersAdapter.TYPE_ITEM /*1*/:
                                    if (UsersAdapter.this.mShowAppointOption && PreferenceUtility.isEmailVerified() && !RoleType.moderator.equals(user.role)) {
                                        resID = R.menu.menu_chat_with_appoint;
                                        break;
                                    }
                                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                                    resID = R.menu.menu_chat_ignored;
                                    break;
                                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                                    resID = R.menu.menu_chat_muted;
                                    break;
                                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                                    resID = R.menu.menu_chat_mods;
                                    break;
                                case Player.STATE_ENDED /*5*/:
                                    resID = R.menu.menu_chat_banned;
                                    break;
                            }
                            final Builder builder = new Builder(a).sheet(resID).title(user.username).listener(new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (a instanceof ChatActivity) {
                                        a.finish();
                                    }
                                    final int userIndex = UsersAdapter.this.mUsers.indexOf(user);
                                    if (userIndex == -1) {
                                        Exception e = new Exception("Can't find user " + user);
                                        e.printStackTrace();
                                        Crashlytics.logException(e);
                                        return;
                                    }
                                    switch (which) {
                                        case R.id.action_appoint:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 5, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_ignore:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, UsersAdapter.TYPE_ITEM, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_mute:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 2, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_ban:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                MaterialDialog.Builder title = new MaterialDialog.Builder((Context) UsersAdapter.this.mActivityRef.get()).title((int) R.string.ban_user);
                                                Object[] objArr = new Object[UsersAdapter.TYPE_ITEM];
                                                objArr[UsersAdapter.TYPE_HEADER] = user.username;
                                                title.content(R.string.ban_confirm_S_, objArr).positiveText((int) R.string.action_ban).positiveColorRes(R.color.red).negativeText(17039360).negativeColorRes(R.color.blue).callback(new ButtonCallback() {
                                                    public void onPositive(MaterialDialog dialog) {
                                                        super.onPositive(dialog);
                                                        UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 7, userIndex, UsersAdapter.TYPE_HEADER));
                                                    }
                                                }).build().show();
                                                return;
                                            }
                                            return;
                                        case R.id.action_view_profile:
                                            a.startActivity(ProfileActivity.getIntent(a, user));
                                            return;
                                        case R.id.action_unban:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 8, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_unignore:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 3, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_disappoint:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 6, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        case R.id.action_unmute:
                                            if (UsersAdapter.this.mActionsCallback != null) {
                                                UsersAdapter.this.mActionsCallback.handleMessage(Message.obtain(null, 4, userIndex, UsersAdapter.TYPE_HEADER));
                                                return;
                                            }
                                            return;
                                        default:
                                            return;
                                    }
                                }
                            });
                            if (TextUtils.isEmpty(user.profileLogoSmall)) {
                                UsersAdapter.this.showWithDefaultPic(a, builder, user.role);
                            } else {
                                ImageLoader.getInstance().loadImage(user.profileLogoSmall, new SimpleImageLoadingListener() {
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        super.onLoadingComplete(imageUri, view, loadedImage);
                                        try {
                                            if (!a.isFinishing()) {
                                                if ((VERSION.SDK_INT >= 17 && !a.isDestroyed()) || VERSION.SDK_INT < 17) {
                                                    Drawable drawable = RoundedBitmapDrawableFactory.create(a.getResources(), loadedImage);
                                                    drawable.setCornerRadius((float) a.getResources().getDimensionPixelSize(R.dimen.avatar_corner));
                                                    builder.icon(drawable);
                                                    UsersAdapter.this.disableActionsIfRequiredAndShow(builder, user.role);
                                                }
                                            }
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                            Crashlytics.logException(e);
                                        }
                                    }

                                    public void onLoadingCancelled(String imageUri, View view) {
                                        super.onLoadingCancelled(imageUri, view);
                                        UsersAdapter.this.showWithDefaultPic(a, builder, user.role);
                                    }

                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                        super.onLoadingFailed(imageUri, view, failReason);
                                        UsersAdapter.this.showWithDefaultPic(a, builder, user.role);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Crashlytics.logException(new Exception("error while parsing ChatMessage " + tag, e));
                    } catch (Exception e2) {
                    }
                }
            }
        }
    };
    private OnClickListener mOnItemClickListener = new OnClickListener() {
        public void onClick(View view) {
            try {
                User user = (User) view.getTag();
                FragmentActivity activity = (FragmentActivity) UsersAdapter.this.mActivityRef.get();
                if (user != null && activity != null) {
                    final int pos = UsersAdapter.this.mListOfSelectedItems.indexOf(user);
                    if (pos >= 0) {
                        UsersAdapter.this.mListOfSelectedItems.remove(user);
                    } else {
                        UsersAdapter.this.mListOfSelectedItems.add(user);
                    }
                    if (UsersAdapter.this.mOnItemSelectedListener != null) {
                        UsersAdapter.this.mOnItemSelectedListener.handleMessage(Message.obtain(null, UsersAdapter.this.mListOfSelectedItems.size()));
                    }
                    final View selector = view.findViewById(R.id.selected_iv);
                    if (selector != null) {
                        selector.postDelayed(new Runnable() {
                            public void run() {
                                selector.setVisibility(pos == -1 ? UsersAdapter.TYPE_HEADER : 8);
                            }
                        }, (long) selector.getResources().getInteger(17694720));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Callback mOnItemSelectedListener;
    private Callback mOnNeedNextDataCallback;
    private String mSearchTermToTrack = null;
    private boolean mSelectingMode;
    private boolean mShowAppointOption;
    private boolean mSwitchingMode;
    private boolean mTrackFollow = false;
    private final ArrayList<User> mUsers = new ArrayList();
    private boolean mWithFooter;
    private boolean mWithHeader;

    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType = new int[ModerationLogicType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$RoleType = new int[RoleType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.broadcaster.ordinal()] = UsersAdapter.TYPE_ITEM;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.moderator.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.admin.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[ModerationLogicType.Viewers.ordinal()] = UsersAdapter.TYPE_ITEM;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[ModerationLogicType.Ignored.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[ModerationLogicType.Muted.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[ModerationLogicType.Mods.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$ModerationLogicType[ModerationLogicType.Banned.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public static class ItemViewHolder extends ViewHolder implements OnClickListener, OnCheckedChangeListener {
        Switch aSwitch;
        WeakReference<FragmentActivity> activityRef;
        TextView description;
        TextView follow;
        View followLayout;
        ImageView icon;
        Integer position;
        View progress;
        String searchTerm;
        View selector;
        TextView subtitle;
        TextView username;

        public ItemViewHolder(View view, Integer dividerResID, WeakReference<FragmentActivity> activityRef, String searchTerm) {
            super(view);
            view.findViewById(R.id.item_layout).setOnClickListener(this);
            View v = view.findViewById(R.id.divider);
            if (v != null) {
                v.setVisibility(UsersAdapter.TYPE_HEADER);
                if (dividerResID != null) {
                    v.setBackgroundResource(dividerResID.intValue());
                }
            }
            this.activityRef = activityRef;
            this.searchTerm = searchTerm;
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.icon.setOnClickListener(this);
            this.username = (TextView) view.findViewById(R.id.user_name_text);
            this.subtitle = (TextView) view.findViewById(R.id.subtitle_text);
            this.description = (TextView) view.findViewById(R.id.description_text);
            this.followLayout = view.findViewById(R.id.follow_layout);
            this.follow = (TextView) view.findViewById(R.id.follow_text);
            this.selector = view.findViewById(R.id.selected_iv);
            if (this.follow != null) {
                this.follow.setTypeface(UIUtils.getTypeface((Context) activityRef.get(), Constants.FOLLOW_TYPEFACE));
            }
            this.aSwitch = (Switch) view.findViewById(R.id.switcher);
            if (this.aSwitch != null) {
                this.aSwitch.setOnCheckedChangeListener(this);
            }
            this.progress = view.findViewById(R.id.progress);
        }

        public void onClick(View view) {
            View v = view.findViewById(R.id.follow_layout);
            final FragmentActivity activity = (FragmentActivity) this.activityRef.get();
            if (v != null && activity != null) {
                try {
                    final User u = (User) v.getTag();
                    if (this.searchTerm != null) {
                        MixpanelHelper.getInstance(activity).generateSearchEvent(this.searchTerm, u.username);
                    }
                    view.postDelayed(new Runnable() {
                        public void run() {
                            try {
                                activity.startActivity(ProfileActivity.getIntent(activity, u));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, (long) activity.getResources().getInteger(17694720));
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }

        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!UsersAdapter.mIgnoreSwitch) {
                try {
                    final User user = (User) compoundButton.getTag();
                    if (user != null) {
                        user.notifyEnabled = b;
                        Network.setFollowingNotifyToggle(null, EntityType.user, user._id, b, new Listener<Boolean>() {
                            public void onResponse(Boolean response) {
                                if (response == null || !response.booleanValue()) {
                                    user.notifyEnabled = !user.notifyEnabled;
                                }
                            }
                        }, null);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class UsersComparator implements Comparator<User> {
        private UsersComparator() {
        }

        public int compare(User userA, User userB) {
            return userA.compare(userB);
        }
    }

    public UsersAdapter(FragmentActivity activity, int layoutID) {
        this.mActivityRef = new WeakReference(activity);
        this.LAYOUT_ID = layoutID;
        this.mDio = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri(new RoundedDrawable(BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_profile_pic), activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner), TYPE_HEADER)).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
        this.DEFAULT_AVATAR = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.default_profile_pic, activity.getTheme());
    }

    public void enableSelectingMode(Callback onItemSelectedListener) {
        this.mSelectingMode = true;
        this.mOnItemSelectedListener = onItemSelectedListener;
    }

    public void enableSwitchMode() {
        this.mSwitchingMode = true;
    }

    public void enableModerationMode(ModerationLogicType logicType, boolean showAppointOption, Callback actionsCallback) {
        this.mModerationLogicType = logicType;
        this.mActionsCallback = actionsCallback;
        this.mShowAppointOption = showAppointOption;
    }

    public void addDisabledActions(int... actionIDs) {
        if (actionIDs != null && actionIDs.length > 0) {
            int[] arr$ = actionIDs;
            int len$ = arr$.length;
            for (int i$ = TYPE_HEADER; i$ < len$; i$ += TYPE_ITEM) {
                int id = arr$[i$];
                if (id < 0) {
                    Exception e = new Exception("Wrong chat menu item ID: " + id);
                    e.printStackTrace();
                    Crashlytics.logException(e);
                } else {
                    this.mDisabledActionIDs.add(Integer.valueOf(id));
                }
            }
        }
    }

    public void clearDisabledActions() {
        this.mDisabledActionIDs.clear();
    }

    public void setDivider(int resID) {
        this.mDividerResID = Integer.valueOf(resID);
    }

    public void setSearchTermToTrack(String searchTerm) {
        this.mSearchTermToTrack = searchTerm;
    }

    public void enableTrackFollow() {
        this.mTrackFollow = true;
    }

    public String getListOfSelectedUsers() {
        if (this.mListOfSelectedItems.size() == 0) {
            return BuildConfig.FLAVOR;
        }
        String ids = BuildConfig.FLAVOR;
        Iterator i$ = this.mListOfSelectedItems.iterator();
        while (i$.hasNext()) {
            ids = (ids + (ids.length() > 0 ? "," : BuildConfig.FLAVOR)) + ((User) i$.next())._id;
        }
        return ids;
    }

    public String getSelectedUsers() {
        try {
            return new Gson().toJson(this.mListOfSelectedItems);
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }
    }

    public long getCountOfSelectedUsers() {
        return (long) this.mListOfSelectedItems.size();
    }

    public void addUsers(User[] users) {
        if (users != null) {
            int i = this.mUsers.size();
            if (i == 0) {
                Collections.addAll(this.mUsers, users);
                safeNotifyDataSetChanged();
            } else if (i > 0) {
                int ctr = TYPE_HEADER;
                User[] arr$ = users;
                int len$ = arr$.length;
                for (int i$ = TYPE_HEADER; i$ < len$; i$ += TYPE_ITEM) {
                    User u = arr$[i$];
                    if (isUserExists(u) < 0) {
                        this.mUsers.add(u);
                        ctr += TYPE_ITEM;
                    }
                }
                try {
                    Crashlytics.log("UsersAdapter.notifyItemRangeInserted. i: " + i + "; length: " + ctr);
                    notifyItemRangeInserted(i, ctr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    safeNotifyDataSetChanged();
                }
            }
        }
    }

    public void addHeaderView(View header) {
        if (header != null) {
            this.mWithHeader = true;
            this.mHeaderView = header;
            if (this.mUsers.size() > 0) {
                try {
                    Crashlytics.log("UsersAdapter.notifyItemChanged. i: 0");
                    notifyItemChanged(TYPE_HEADER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    safeNotifyDataSetChanged();
                }
            }
        }
    }

    public void setMinimalHeight(int minimalHeight) {
        this.mMinimalHeight = minimalHeight;
        this.mWithFooter = this.mMinimalHeight > 0;
        if (this.mUsers.size() > 0) {
            safeNotifyDataSetChanged();
        }
    }

    public void add(User user) {
        try {
            if (isUserExists(user) < 0) {
                this.mUsers.add(user);
                if (this.mModerationLogicType != null) {
                    Collections.sort(this.mUsers, new UsersComparator());
                    notifyDataSetChanged();
                    return;
                }
                notifyItemInserted(this.mUsers.indexOf(user));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void change(User user) {
        try {
            int pos = isUserExists(user);
            if (pos >= 0) {
                this.mUsers.remove(pos);
                this.mUsers.add(pos, user);
                if (this.mModerationLogicType != null) {
                    Collections.sort(this.mUsers, new UsersComparator());
                    notifyDataSetChanged();
                    return;
                }
                notifyItemChanged(pos);
                return;
            }
            add(user);
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void remove(User user) {
        try {
            int pos = isUserExists(user);
            if (pos >= 0) {
                this.mUsers.remove(pos);
                if (this.mModerationLogicType != null) {
                    Collections.sort(this.mUsers, new UsersComparator());
                    notifyDataSetChanged();
                    return;
                }
                notifyItemRemoved(pos);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void clear() {
        this.mUsers.clear();
        if (this.mListOfSelectedItems.size() > 0) {
            addUsers((User[]) this.mListOfSelectedItems.toArray(new User[this.mListOfSelectedItems.size()]));
        }
    }

    public int getCountOfSelectedItems() {
        return this.mListOfSelectedItems.size();
    }

    public void setOnNeedNextDataCallback(Callback callback) {
        this.mOnNeedNextDataCallback = callback;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        View view = LayoutInflater.from((Context) this.mActivityRef.get()).inflate(this.LAYOUT_ID, parent, false);
        if (this.mSwitchingMode) {
            View v = view.findViewById(R.id.switcher);
            if (v != null) {
                v.setVisibility(TYPE_HEADER);
            }
        }
        return new ItemViewHolder(view, this.mDividerResID, this.mActivityRef, this.mSearchTermToTrack);
    }

    public int getItemCount() {
        int i = TYPE_ITEM;
        int size = (this.mWithHeader ? TYPE_ITEM : TYPE_HEADER) + this.mUsers.size();
        if (!this.mWithFooter) {
            i = TYPE_HEADER;
        }
        return size + i;
    }

    public int getDataItemCount() {
        return this.mUsers.size();
    }

    public int getItemViewType(int position) {
        if ((!this.mWithHeader || position != 0) && (!this.mWithFooter || position <= this.mUsers.size())) {
            return TYPE_ITEM;
        }
        return TYPE_HEADER;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        User user;
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        if (getItemViewType(position) == 0) {
            user = null;
        } else {
            user = (User) this.mUsers.get(position - (this.mWithHeader ? TYPE_ITEM : TYPE_HEADER));
        }
        onBindViewHolder(itemViewHolder, user, position);
    }

    public User getItem(int position) {
        if (position >= 0 && position < this.mUsers.size()) {
            return (User) this.mUsers.get(position);
        }
        IllegalArgumentException e = new IllegalArgumentException("Wrong position to get user: " + position);
        e.printStackTrace();
        Crashlytics.logException(e);
        return null;
    }

    private void onBindViewHolder(ItemViewHolder holder, User user, int position) {
        int i = 4;
        int i2 = TYPE_HEADER;
        FragmentActivity activity = (FragmentActivity) this.mActivityRef.get();
        if (activity != null) {
            LayoutParams lp = holder.itemView.getLayoutParams();
            if (this.mWithHeader && position == 0) {
                holder.itemView.setClickable(false);
                lp.height = this.mHeaderView.getMinimumHeight();
                holder.itemView.setLayoutParams(lp);
                holder.itemView.setVisibility(4);
            } else if (this.mWithFooter && user == null) {
                holder.itemView.setClickable(false);
                lp.height = this.mMinimalHeight - this.mCurrentHeight;
                if (lp.height < 0) {
                    lp.height = TYPE_HEADER;
                }
                holder.itemView.setLayoutParams(lp);
                View view = holder.itemView;
                if (lp.height <= 0) {
                    i = 8;
                }
                view.setVisibility(i);
            } else {
                if (holder.itemView.getMeasuredHeight() == 0) {
                    holder.itemView.measure(TYPE_HEADER, TYPE_HEADER);
                }
                this.mCurrentHeight += holder.itemView.getMeasuredHeight();
                if (user != null) {
                    if (this.mWithHeader) {
                        position--;
                    }
                    if (this.mSelectingMode) {
                        holder.itemView.setOnClickListener(this.mOnItemClickListener);
                        holder.itemView.setTag(user);
                    }
                    holder.icon.setImageBitmap(null);
                    if (this.mModerationLogicType != null) {
                        holder.icon.setTag(user);
                        holder.icon.setOnClickListener(this.mOnIconClickListener);
                    }
                    ImageLoader.getInstance().displayImage(user.profileLogoSmall != null ? user.profileLogoSmall : user.profileLogo, holder.icon, this.mDio);
                    if (this.mModerationLogicType != null) {
                        int colorId = R.color.user;
                        if (user.role != null) {
                            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$RoleType[user.role.ordinal()]) {
                                case TYPE_ITEM /*1*/:
                                    colorId = R.color.broadcaster;
                                    break;
                                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                                    colorId = R.color.moderator;
                                    break;
                                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                                    colorId = R.color.admin;
                                    break;
                                default:
                                    colorId = R.color.user_text_color;
                                    break;
                            }
                        }
                        holder.username.setTextColor(MainApplication.getContext().getResources().getColor(colorId));
                    }
                    holder.username.setText(user.username);
                    if (holder.subtitle != null) {
                        holder.subtitle.setText(user.subtitle != null ? " / " + user.subtitle : BuildConfig.FLAVOR);
                    }
                    if (this.LAYOUT_ID == R.layout.item_user) {
                        holder.description.setVisibility(8);
                        holder.description.setText(String.valueOf(user.followerCount));
                    } else {
                        holder.description.setVisibility(user.getBroadcastCount() == 0 ? 8 : TYPE_HEADER);
                        TextView textView = holder.description;
                        Object[] objArr = new Object[TYPE_ITEM];
                        objArr[TYPE_HEADER] = Integer.valueOf(user.getBroadcastCount());
                        textView.setText(MainApplication.getRString(R.string._N_Broadcasts, objArr));
                    }
                    if (holder.followLayout != null) {
                        if (PreferenceUtility.getUser()._id == null || !PreferenceUtility.getUser()._id.equals(user._id)) {
                            holder.followLayout.setVisibility(TYPE_HEADER);
                        } else {
                            holder.followLayout.setVisibility(8);
                        }
                        holder.followLayout.setTag(user);
                        holder.followLayout.setOnClickListener(this);
                        setupFollowButton(holder.follow, user, activity);
                    }
                    if (holder.progress != null) {
                        holder.progress.setVisibility(8);
                    }
                    if (holder.aSwitch != null) {
                        mIgnoreSwitch = true;
                        holder.aSwitch.setChecked(user.notifyEnabled);
                        holder.aSwitch.setTag(user);
                        mIgnoreSwitch = false;
                    }
                    if (holder.selector != null) {
                        View view2 = holder.selector;
                        if (!this.mListOfSelectedItems.contains(user)) {
                            i2 = 8;
                        }
                        view2.setVisibility(i2);
                    }
                    if (position == this.mUsers.size() - 10 && this.mOnNeedNextDataCallback != null) {
                        this.mOnNeedNextDataCallback.handleMessage(Message.obtain(null, 2));
                    }
                }
            }
        }
    }

    public void onClick(final View view) {
        boolean z = false;
        User tag = view.getTag();
        if (tag != null && (tag instanceof User)) {
            final User user = tag;
            final View progress = ((ViewGroup) view.getParent()).findViewById(R.id.progress);
            int position = this.mUsers.indexOf(user);
            if (this.mTrackFollow) {
                MixpanelHelper.getInstance((Context) this.mActivityRef.get()).generateFollowEvent(user);
            }
            if (progress != null) {
                try {
                    progress.setVisibility(TYPE_HEADER);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            FragmentActivity fragmentActivity = (FragmentActivity) this.mActivityRef.get();
            if (!user.currentFollowed) {
                z = true;
            }
            Network.follow(fragmentActivity, z, EntityType.user, user._id, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    if (progress != null) {
                        progress.setVisibility(8);
                    }
                    if (response != null) {
                        user.currentFollowed = response.booleanValue();
                        if (PreferenceUtility.getUser().followerCount == null) {
                            PreferenceUtility.getUser().followerCount = Integer.valueOf(UsersAdapter.TYPE_HEADER);
                        }
                        User user = PreferenceUtility.getUser();
                        user.followerCount = Integer.valueOf((response.booleanValue() ? UsersAdapter.TYPE_ITEM : -1) + user.followerCount.intValue());
                        UsersAdapter.this.setupFollowButton((TextView) view.findViewById(R.id.follow_text), user, (FragmentActivity) UsersAdapter.this.mActivityRef.get());
                    }
                }
            }, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    if (progress != null) {
                        progress.setVisibility(8);
                    }
                }
            });
        }
    }

    private int isUserExists(User user) {
        for (int i = TYPE_HEADER; i < this.mUsers.size(); i += TYPE_ITEM) {
            if (((User) this.mUsers.get(i)).equals(user)) {
                return i;
            }
        }
        return -1;
    }

    private void setupFollowButton(TextView view, User user, FragmentActivity activtiy) {
        if (view != null && user != null) {
            view.setActivated(!user.currentFollowed);
            view.setText(user.currentFollowed ? R.string.UNFOLLOW : R.string.FOLLOW);
            view.setTextColor(activtiy.getResources().getColor(user.currentFollowed ? R.color.follow_inactive : R.color.follow_active));
        }
    }

    private void safeNotifyDataSetChanged() {
        try {
            Crashlytics.log("UsersAdapter.notifyDataSetChanged");
            notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }
    }

    private void showWithDefaultPic(FragmentActivity a, Builder builder, RoleType role) {
        try {
            if (!a.isFinishing()) {
                if ((VERSION.SDK_INT >= 17 && !a.isDestroyed()) || VERSION.SDK_INT < 17) {
                    int size = (int) TypedValue.applyDimension(TYPE_ITEM, 40.0f, a.getResources().getDisplayMetrics());
                    Drawable drawable = RoundedBitmapDrawableFactory.create(a.getResources(), Bitmap.createScaledBitmap(((BitmapDrawable) this.DEFAULT_AVATAR).getBitmap(), size, size, true));
                    drawable.setCornerRadius((float) a.getResources().getDimensionPixelSize(R.dimen.avatar_corner));
                    builder.icon(drawable);
                    disableActionsIfRequiredAndShow(builder, role);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void disableActionsIfRequiredAndShow(Builder builder, RoleType role) {
        try {
            MenuItem menuItem;
            BottomSheet sheet = builder.build();
            if (this.mDisabledActionIDs != null && RoleType.user.equals(role)) {
                Iterator i$ = this.mDisabledActionIDs.iterator();
                while (i$.hasNext()) {
                    int id = ((Integer) i$.next()).intValue();
                    if (id < 0) {
                        Exception e = new Exception("Wrong chat menu item ID: " + id);
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    } else {
                        menuItem = sheet.getMenu().findItem(id);
                        if (menuItem != null) {
                            menuItem.setVisible(false);
                        }
                    }
                }
            }
            try {
                menuItem = sheet.getMenu().findItem(R.id.action_ban);
                if (menuItem != null) {
                    if (ModerationHelper.canBanUser(PreferenceUtility.getUser().role, role)) {
                        menuItem.setTitle(UIUtils.colorizeText(menuItem.getTitle(), R.color.red));
                    } else {
                        menuItem.setVisible(false);
                    }
                }
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
            sheet.show();
        } catch (Throwable e22) {
            e22.printStackTrace();
            Crashlytics.logException(e22);
        }
    }
}
