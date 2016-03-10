package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.android.volley.DefaultRetryPolicy;
import com.cocosw.bottomsheet.BottomSheet;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.ui.MentionSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;

public class ChatMessagesAdapter extends Adapter<ViewHolder> implements OnClickListener {
    private static final int LAYOUT_ID = 2130903088;
    private static String LIKE = null;
    private static final String TAG = "ChatMessagesAdapter";
    private Drawable DEFAULT_AVATAR;
    private Callback mActionCallback;
    private WeakReference<FragmentActivity> mActivityRef;
    private WeakReference<FragmentActivity> mChatActivityRef;
    protected ArrayList<ChatMessage> mDataset;
    private DisplayImageOptions mDio;
    private ArrayList<Integer> mDisabledActionIDs = new ArrayList();
    private Callback mGetDataCallback;
    private ChatMessage mLastMessageWhenDataWasAsked;
    private boolean mShowAppointOption;
    private ArrayList<String> mUsersWhoAlreadyLiked;

    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$RoleType = new int[RoleType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.broadcaster.ordinal()] = 1;
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
        }
    }

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        View actionLayout;
        TextView description;
        ImageView icon;
        TextView message;
        View messageLayout;
        TextView subtitle;
        TextView username;

        public ViewHolder(CardView v, OnClickListener listener) {
            super(v);
            this.messageLayout = v.findViewById(R.id.message_layout);
            this.actionLayout = v.findViewById(R.id.action_layout);
            this.username = (TextView) v.findViewById(R.id.user_name_text);
            this.subtitle = (TextView) v.findViewById(R.id.subtitle_text);
            this.message = (TextView) v.findViewById(R.id.message_text);
            this.description = (TextView) v.findViewById(R.id.message_description_text);
            this.icon = (ImageView) v.findViewById(R.id.message_icon);
            this.icon.setOnClickListener(listener);
        }
    }

    public ChatMessagesAdapter(FragmentActivity activity, ChatMessage[] messages) {
        setHasStableIds(true);
        this.mActivityRef = new WeakReference(activity);
        if (activity != null) {
            this.DEFAULT_AVATAR = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.default_profile_pic, activity.getTheme());
            this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri(this.DEFAULT_AVATAR).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
        } else {
            this.DEFAULT_AVATAR = MainApplication.getContext().getResources().getDrawable(R.drawable.default_profile_pic);
        }
        this.mDataset = new ArrayList();
        this.mUsersWhoAlreadyLiked = new ArrayList();
        LIKE = MainApplication.getRString(R.string.action_like, new Object[0]).toUpperCase();
        if (messages != null) {
            for (ChatMessage m : messages) {
                add(m, false);
            }
            notifyDataSetChanged();
        }
    }

    public long getItemId(int position) {
        if (this.mDataset == null || this.mDataset.size() <= position) {
            return super.getItemId(position);
        }
        ChatMessage message = (ChatMessage) this.mDataset.get(position);
        return message.timestamp + ((long) (message.message != null ? message.message.hashCode() : 0));
    }

    public void setActivity(FragmentActivity activity) {
        if (activity != null) {
            this.mActivityRef = new WeakReference(activity);
        }
    }

    public void setChatActivity(FragmentActivity activity) {
        this.mChatActivityRef = new WeakReference(activity);
    }

    public void showAppointOption() {
        this.mShowAppointOption = true;
    }

    public void addDisabledActions(int... actionIDs) {
        if (actionIDs != null && actionIDs.length > 0) {
            for (int id : actionIDs) {
                if (id > 0) {
                    this.mDisabledActionIDs.add(Integer.valueOf(id));
                } else {
                    Exception e = new Exception("Added wrong ActionId to disable: " + id);
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
    }

    public void clearDisabledActions() {
        this.mDisabledActionIDs.clear();
    }

    public void setActionCallback(Callback callback) {
        this.mActionCallback = callback;
    }

    public void setGetDataCallback(Callback callback) {
        this.mLastMessageWhenDataWasAsked = null;
        this.mGetDataCallback = callback;
    }

    public void onGetDataFailed() {
        this.mLastMessageWhenDataWasAsked = null;
    }

    public String[] getItemsAsStrings() {
        ArrayList<String> items = new ArrayList();
        for (int i = 0; i < this.mDataset.size(); i++) {
            items.add(((ChatMessage) this.mDataset.get(i)).toString());
        }
        return (String[]) items.toArray(new String[items.size()]);
    }

    public void add(String message) {
        add((ChatMessage) new Gson().fromJson(message, ChatMessage.class), true);
    }

    public synchronized void add(ChatMessage message, boolean notify) {
        if (message != null) {
            if (!TextUtils.isEmpty(message.userId) && TextUtils.equals(message.message, LIKE)) {
                if (!this.mUsersWhoAlreadyLiked.contains(message.userId)) {
                    this.mUsersWhoAlreadyLiked.add(message.userId);
                }
            }
            int pos = getPositionToInsert(message);
            this.mDataset.add(pos, message);
            if (notify) {
                try {
                    notifyItemInserted(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    safeNotifyDataSetChanged();
                }
            }
        }
    }

    public synchronized void add(ChatMessage message, boolean checkForUnique, boolean notify) {
        if (message != null) {
            if (this.mDataset != null && this.mDataset.size() > 0 && checkForUnique) {
                for (int i = 0; i < this.mDataset.size(); i++) {
                    ChatMessage m = (ChatMessage) this.mDataset.get(i);
                    if (TextUtils.equals(message.userId, m.userId) && TextUtils.equals(message.message, m.message) && (message.isSystemMsg() || (m.timestamp != 0 && m.timestamp == message.timestamp))) {
                        break;
                    }
                }
            }
        }
        add(message, notify);
    }

    public synchronized void change(ChatMessage message) {
        if (message != null) {
            if (this.mDataset != null && this.mDataset.size() > 0) {
                for (int i = 0; i < this.mDataset.size(); i++) {
                    if (TextUtils.equals(message._id, ((ChatMessage) this.mDataset.get(i))._id)) {
                        this.mDataset.set(i, message);
                        notifyItemChanged(i);
                        break;
                    }
                }
            }
        }
    }

    private synchronized int getPositionToInsert(ChatMessage message) {
        int i;
        if (this.mDataset == null || this.mDataset.size() == 0) {
            i = 0;
        } else {
            i = 0;
            if (message.timestamp == 0) {
                i = this.mDataset.size();
            } else {
                while (i < this.mDataset.size()) {
                    if (((ChatMessage) this.mDataset.get(i)).timestamp > message.timestamp) {
                        break;
                    }
                    i++;
                }
            }
        }
        return i;
    }

    public void remove(ChatMessage message) {
        if (message != null && this.mDataset != null && this.mDataset.size() > 0) {
            for (int i = 0; i < this.mDataset.size(); i++) {
                if (((ChatMessage) this.mDataset.get(i)).timestamp == message.timestamp) {
                    this.mDataset.remove(i);
                    try {
                        notifyItemRemoved(i);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        safeNotifyDataSetChanged();
                        return;
                    }
                }
            }
        }
    }

    public void remove(int position) {
        if (this.mDataset != null && position >= 0 && position < getItemCount()) {
            this.mDataset.remove(position);
            try {
                notifyItemRemoved(position);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                safeNotifyDataSetChanged();
            }
        }
    }

    public void clear() {
        if (this.mDataset != null) {
            this.mDataset.clear();
            safeNotifyDataSetChanged();
        }
    }

    public ChatMessage getItem(int index) {
        return this.mDataset != null ? (ChatMessage) this.mDataset.get(index) : null;
    }

    public int getItemCount() {
        return this.mDataset == null ? 0 : this.mDataset.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chat_message, parent, false);
        v.setUseCompatPadding(false);
        return new ViewHolder(v, this);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage message = (ChatMessage) this.mDataset.get(position);
        if (message != null) {
            if (position == 1 && this.mDataset.size() >= 10 && this.mLastMessageWhenDataWasAsked != message && this.mGetDataCallback != null) {
                Log.i(TAG, "mGetDataCallback for position " + position + "; total count: " + this.mDataset.size());
                this.mLastMessageWhenDataWasAsked = message;
                this.mGetDataCallback.handleMessage(Message.obtain());
            }
            holder.actionLayout.setAlpha(0.0f);
            holder.actionLayout.setTranslationX(0.0f);
            holder.messageLayout.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            holder.messageLayout.setTranslationX(0.0f);
            int colorId = R.color.user;
            if (message.role != null) {
                switch (AnonymousClass3.$SwitchMap$com$mobcrush$mobcrush$logic$RoleType[message.role.ordinal()]) {
                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        colorId = R.color.broadcaster;
                        break;
                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                        colorId = R.color.moderator;
                        break;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        colorId = R.color.admin;
                        break;
                    default:
                        colorId = R.color.user;
                        break;
                }
            }
            holder.username.setTextColor(MainApplication.getContext().getResources().getColor(colorId));
            holder.username.setText(TextUtils.isEmpty(message.username) ? MainApplication.getRString(R.string.guest, new Object[0]) : message.username);
            if (holder.subtitle != null) {
                holder.subtitle.setText(message.subtitle != null ? " / " + message.subtitle : BuildConfig.FLAVOR);
            }
            try {
                holder.message.setText(configMentioning(PreferenceUtility.getUser().getMentionRegexp().matcher(message.message), new SpannableString(message.message), 0));
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                holder.message.setText(message.message);
            }
            holder.description.setVisibility(message.triggeredMute ? 0 : 8);
            holder.icon.setImageDrawable(this.DEFAULT_AVATAR);
            holder.icon.setTag(message);
            if (!TextUtils.isEmpty(message.profileLogoSmall)) {
                ImageLoader.getInstance().displayImage(message.profileLogoSmall, holder.icon, this.mDio);
            }
        }
    }

    private Spannable configMentioning(Matcher matcher, Spannable spannable, int startFrom) {
        if (matcher.find(startFrom)) {
            int start = matcher.start() + matcher.group(1).length();
            int end = matcher.end() - matcher.group(3).length();
            if (spannable.length() == end - start) {
                spannable = new SpannableString(spannable + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            }
            spannable.setSpan(new MentionSpan(), start, end, 33);
            configMentioning(matcher, spannable, matcher.end());
        }
        return spannable;
    }

    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null && (tag instanceof ChatMessage)) {
            try {
                final ChatMessage message = (ChatMessage) tag;
                final User user = message.getUser();
                if (!TextUtils.equals(PreferenceUtility.getUser()._id, message.userId)) {
                    final FragmentActivity a = (FragmentActivity) this.mActivityRef.get();
                    if (a != null) {
                        int chatMenuResID = R.menu.menu_chat_for_unverified;
                        if (PreferenceUtility.isEmailVerified()) {
                            chatMenuResID = (!this.mShowAppointOption || RoleType.moderator.equals(user.role)) ? R.menu.menu_chat : R.menu.menu_chat_with_appoint;
                        }
                        final BottomSheet.Builder builder = new BottomSheet.Builder(a).sheet(chatMenuResID).title(message.username).listener(new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final int messageIndex = ChatMessagesAdapter.this.mDataset.indexOf(message);
                                if (messageIndex == -1) {
                                    Exception e = new Exception("Can't find message " + message);
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                switch (which) {
                                    case R.id.action_appoint:
                                        if (ChatMessagesAdapter.this.mActionCallback != null) {
                                            ChatMessagesAdapter.this.mActionCallback.handleMessage(Message.obtain(null, 5, messageIndex, 0));
                                            return;
                                        }
                                        return;
                                    case R.id.action_ignore:
                                        if (ChatMessagesAdapter.this.mActionCallback != null) {
                                            ChatMessagesAdapter.this.mActionCallback.handleMessage(Message.obtain(null, 1, messageIndex, 0));
                                            return;
                                        }
                                        return;
                                    case R.id.action_mute:
                                        if (ChatMessagesAdapter.this.mActionCallback != null) {
                                            ChatMessagesAdapter.this.mActionCallback.handleMessage(Message.obtain(null, 2, messageIndex, 0));
                                            return;
                                        }
                                        return;
                                    case R.id.action_ban:
                                        if (ChatMessagesAdapter.this.mActionCallback != null) {
                                            new MaterialDialog.Builder((Context) ChatMessagesAdapter.this.mActivityRef.get()).title((int) R.string.ban_user).content(R.string.ban_confirm_S_, user.username).positiveText((int) R.string.action_ban).positiveColorRes(R.color.red).negativeText(17039360).negativeColorRes(R.color.blue).callback(new ButtonCallback() {
                                                public void onPositive(MaterialDialog dialog) {
                                                    super.onPositive(dialog);
                                                    ChatMessagesAdapter.this.mActionCallback.handleMessage(Message.obtain(null, 7, messageIndex, 0));
                                                }
                                            }).build().show();
                                            return;
                                        }
                                        return;
                                    case R.id.action_view_profile:
                                        a.startActivity(ProfileActivity.getIntent(a, user));
                                        return;
                                    default:
                                        return;
                                }
                            }
                        });
                        if (TextUtils.isEmpty(message.getProfileLogoSmall())) {
                            showWithDefaultPic(a, builder, message.role);
                        } else {
                            ImageLoader.getInstance().loadImage(message.getProfileLogoSmall(), new SimpleImageLoadingListener() {
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);
                                    try {
                                        if (!a.isFinishing()) {
                                            if ((VERSION.SDK_INT >= 17 && !a.isDestroyed()) || VERSION.SDK_INT < 17) {
                                                Drawable drawable = RoundedBitmapDrawableFactory.create(a.getResources(), loadedImage);
                                                drawable.setCornerRadius((float) a.getResources().getDimensionPixelSize(R.dimen.avatar_corner));
                                                builder.icon(drawable);
                                                ChatMessagesAdapter.this.disableActionsIfRequiredAndShow(builder, message.role);
                                            }
                                        }
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                        Crashlytics.logException(e);
                                    }
                                }

                                public void onLoadingCancelled(String imageUri, View view) {
                                    super.onLoadingCancelled(imageUri, view);
                                    ChatMessagesAdapter.this.showWithDefaultPic(a, builder, message.role);
                                }

                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    super.onLoadingFailed(imageUri, view, failReason);
                                    ChatMessagesAdapter.this.showWithDefaultPic(a, builder, message.role);
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

    private void showWithDefaultPic(FragmentActivity a, BottomSheet.Builder builder, RoleType role) {
        try {
            if (!a.isFinishing()) {
                if ((VERSION.SDK_INT >= 17 && !a.isDestroyed()) || VERSION.SDK_INT < 17) {
                    int size = (int) TypedValue.applyDimension(1, 40.0f, a.getResources().getDisplayMetrics());
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

    private void disableActionsIfRequiredAndShow(BottomSheet.Builder builder, RoleType role) {
        MenuItem menuItem;
        FragmentActivity activity = null;
        BottomSheet sheet = builder.build();
        if (!(this.mDisabledActionIDs == null || ModerationHelper.isAdmin(PreferenceUtility.getUser()._id))) {
            Iterator i$ = this.mDisabledActionIDs.iterator();
            while (i$.hasNext()) {
                int id = ((Integer) i$.next()).intValue();
                if (id > 0) {
                    try {
                        menuItem = Utils.findItem(sheet.getMenu(), id);
                        if (menuItem != null) {
                            menuItem.setVisible(false);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        return;
                    }
                }
            }
        }
        try {
            menuItem = Utils.findItem(sheet.getMenu(), R.id.action_ban);
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
        if (this.mChatActivityRef != null) {
            activity = (FragmentActivity) this.mChatActivityRef.get();
        }
        if (activity != null) {
            activity.finish();
            this.mChatActivityRef = null;
        }
        sheet.show();
    }

    private void safeNotifyDataSetChanged() {
        try {
            Crashlytics.log("ChatMessagesAdapter.safeNotifyDataSetChanged");
            notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }
    }
}
