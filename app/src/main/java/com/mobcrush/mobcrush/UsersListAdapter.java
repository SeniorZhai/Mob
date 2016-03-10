package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer.RoundedDrawable;
import io.fabric.sdk.android.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class UsersListAdapter extends ArrayAdapter<User> implements OnClickListener {
    private int LAYOUT_ID = R.layout.item_user;
    private WeakReference<FragmentActivity> mActivityRef;
    private DisplayImageOptions mDio;
    private LayoutInflater mLayoutInflater;
    private ArrayList<User> mListOfSelectedItems = new ArrayList();
    private OnClickListener mOnItemClickListener = new OnClickListener() {
        public void onClick(View view) {
            try {
                final ViewHolder holder = (ViewHolder) view.getTag();
                if (holder != null) {
                    final int pos = UsersListAdapter.this.mListOfSelectedItems.indexOf(UsersListAdapter.this.getItem(holder.position.intValue()));
                    if (pos >= 0) {
                        UsersListAdapter.this.mListOfSelectedItems.remove(UsersListAdapter.this.getItem(holder.position.intValue()));
                    } else {
                        UsersListAdapter.this.mListOfSelectedItems.add(UsersListAdapter.this.getItem(holder.position.intValue()));
                    }
                    if (UsersListAdapter.this.mOnItemSelectedListener != null) {
                        UsersListAdapter.this.mOnItemSelectedListener.handleMessage(Message.obtain(null, UsersListAdapter.this.mListOfSelectedItems.size()));
                    }
                    if (holder.selector != null) {
                        holder.selector.postDelayed(new Runnable() {
                            public void run() {
                                holder.selector.setVisibility(pos == -1 ? 0 : 8);
                            }
                        }, (long) holder.selector.getResources().getInteger(17694720));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Callback mOnItemSelectedListener;
    private Callback mOnNeedNextDataCallback;
    private boolean mSelectingMode;
    private User mUser;

    static class ViewHolder {
        TextView description;
        TextView follow;
        ImageView icon;
        Integer position;
        View progress;
        View selector;
        TextView username;

        ViewHolder() {
        }
    }

    public UsersListAdapter(FragmentActivity activity, User user, int layoutID) {
        super(activity, layoutID);
        this.LAYOUT_ID = layoutID;
        this.mUser = user;
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mActivityRef = new WeakReference(activity);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri(new RoundedDrawable(BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_profile_pic), activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner), 0)).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
    }

    public void enableSelectingMode(Callback onItemSelectedListener) {
        this.mSelectingMode = true;
        this.mOnItemSelectedListener = onItemSelectedListener;
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

    public void clear() {
        super.clear();
        addAll(this.mListOfSelectedItems);
    }

    public void add(User object) {
        if (!isUserExists(object)) {
            super.add(object);
        }
    }

    public void addAll(Collection<? extends User> collection) {
        if (collection != null) {
            for (User u : collection) {
                add(u);
            }
        }
    }

    public void addAll(User... items) {
        if (items != null && items.length > 0) {
            for (User u : items) {
                add(u);
            }
        }
    }

    public int getCountOfSelectedItems() {
        return this.mListOfSelectedItems.size();
    }

    public void setOnNeedNextDataCallback(Callback callback) {
        this.mOnNeedNextDataCallback = callback;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int i = 0;
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(this.LAYOUT_ID, parent, false);
            if (this.mSelectingMode) {
                convertView.setOnClickListener(this.mOnItemClickListener);
            }
            holder = new ViewHolder();
            holder.position = Integer.valueOf(position);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.username = (TextView) convertView.findViewById(R.id.user_name_text);
            holder.description = (TextView) convertView.findViewById(R.id.description_text);
            holder.follow = (TextView) convertView.findViewById(R.id.follow_text);
            holder.selector = convertView.findViewById(R.id.selected_iv);
            if (holder.follow != null) {
                holder.follow.setTypeface(UIUtils.getTypeface((Context) this.mActivityRef.get(), Constants.FOLLOW_TYPEFACE));
            }
            holder.progress = convertView.findViewById(R.id.progress);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        if (holder != null) {
            TextView textView;
            User user = (User) getItem(position);
            holder.position = Integer.valueOf(position);
            holder.icon.setImageBitmap(null);
            ImageLoader.getInstance().displayImage(user.profileLogoSmall != null ? user.profileLogoSmall : user.profileLogo, holder.icon, this.mDio);
            holder.username.setText(user.username);
            if (this.LAYOUT_ID == R.layout.item_user) {
                holder.description.setVisibility(8);
                holder.description.setText(String.valueOf(user.followerCount));
            } else {
                int i2;
                textView = holder.description;
                if (user.getBroadcastCount() == 0) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                textView.setVisibility(i2);
                holder.description.setText(MainApplication.getRString(R.string._N_Broadcasts, Integer.valueOf(user.getBroadcastCount())));
            }
            if (holder.follow != null) {
                boolean z;
                if (PreferenceUtility.getUser()._id == null || !PreferenceUtility.getUser()._id.equals(user._id)) {
                    holder.follow.setVisibility(0);
                } else {
                    holder.follow.setVisibility(8);
                }
                holder.follow.setTag(Integer.valueOf(position));
                textView = holder.follow;
                if (user.currentFollowed) {
                    z = false;
                } else {
                    z = true;
                }
                textView.setActivated(z);
                holder.follow.setText(user.currentFollowed ? R.string.UNFOLLOW : R.string.FOLLOW);
                holder.follow.setTextColor(getContext().getResources().getColor(user.currentFollowed ? R.color.follow_inactive : R.color.follow_active));
                holder.follow.setOnClickListener(this);
            }
            if (holder.progress != null) {
                holder.progress.setVisibility(8);
            }
            if (holder.selector != null) {
                View view = holder.selector;
                if (!this.mListOfSelectedItems.contains(getItem(holder.position.intValue()))) {
                    i = 8;
                }
                view.setVisibility(i);
            }
        }
        if (position == getCount() - 10 && this.mOnNeedNextDataCallback != null) {
            this.mOnNeedNextDataCallback.handleMessage(Message.obtain(null, 2));
        }
        return convertView;
    }

    public void onClick(View view) {
        boolean z = false;
        Object tag = view.getTag();
        if (tag != null) {
            final User user = (User) getItem(((Integer) tag).intValue());
            tag = ((View) view.getParent()).getTag();
            final ViewHolder holder = tag != null ? (ViewHolder) tag : null;
            if (!(holder == null || holder.progress == null)) {
                holder.progress.setVisibility(0);
            }
            FragmentActivity fragmentActivity = (FragmentActivity) this.mActivityRef.get();
            if (!user.currentFollowed) {
                z = true;
            }
            Network.follow(fragmentActivity, z, EntityType.user, user._id, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    if (UsersListAdapter.this.mActivityRef.get() != null && PreferenceUtility.getUser().equals(UsersListAdapter.this.mUser)) {
                        ((FragmentActivity) UsersListAdapter.this.mActivityRef.get()).setIntent(new Intent(Constants.ACTION_UPDATE_USER));
                    }
                    if (!(holder == null || holder.progress == null)) {
                        holder.progress.setVisibility(8);
                    }
                    if (response != null) {
                        user.currentFollowed = response.booleanValue();
                        UsersListAdapter.this.notifyDataSetChanged();
                    }
                }
            }, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    if (holder != null && holder.progress != null) {
                        holder.progress.setVisibility(8);
                    }
                }
            });
        }
    }

    private boolean isUserExists(User user) {
        for (int i = 0; i < getCount(); i++) {
            if (((User) getItem(i)).equals(user)) {
                return true;
            }
        }
        return false;
    }
}
