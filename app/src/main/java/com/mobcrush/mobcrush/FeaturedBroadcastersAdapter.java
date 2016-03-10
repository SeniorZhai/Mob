package com.mobcrush.mobcrush;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class FeaturedBroadcastersAdapter extends Adapter<ViewHolder> implements OnClickListener {
    private static final int ITEM_LAYOUT_ID = 2130903177;
    private static final int ITEM_TYPE = 1;
    private static final int SECTION_LAYOUT_ID = 2130903175;
    private static final int SECTION_TEXT_ID = 2131624337;
    private static final int SECTION_TYPE = 0;
    private static final String TAG = "FeaturedBroadcastersAdapter";
    private WeakReference<FragmentActivity> mActivityRef;
    private DisplayImageOptions mDio;
    private final ArrayList<User> mFeaturedUsers = new ArrayList();
    private final ArrayList<String> mSections = new ArrayList();
    private final ArrayList<User> mUsers = new ArrayList();

    public static class ItemViewHolder extends ViewHolder implements OnClickListener {
        WeakReference<FragmentActivity> activityRef;
        TextView description;
        TextView follow;
        ImageView icon;
        Integer position;
        View progress;
        View selector;
        TextView username;

        public ItemViewHolder(View view, WeakReference<FragmentActivity> activityRef) {
            super(view);
            view.setOnClickListener(this);
            View v = view.findViewById(R.id.divider);
            if (v != null) {
                v.setVisibility(FeaturedBroadcastersAdapter.SECTION_TYPE);
            }
            this.activityRef = activityRef;
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.username = (TextView) view.findViewById(R.id.user_name_text);
            this.description = (TextView) view.findViewById(R.id.description_text);
            this.follow = (TextView) view.findViewById(R.id.follow_text);
            this.selector = view.findViewById(R.id.selected_iv);
            if (this.follow != null) {
                this.follow.setTypeface(UIUtils.getTypeface((Context) activityRef.get(), Constants.FOLLOW_TYPEFACE));
            }
            this.progress = view.findViewById(R.id.progress);
        }

        public void onClick(View view) {
            final View v = view.findViewById(R.id.follow_text);
            final FragmentActivity activity = (FragmentActivity) this.activityRef.get();
            if (v != null && v.getTag() != null && activity != null) {
                view.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            activity.startActivity(ProfileActivity.getIntent(activity, (User) v.getTag()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, (long) activity.getResources().getInteger(17694720));
            }
        }
    }

    public static class SectionViewHolder extends ViewHolder {
        public TextView title;

        public SectionViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.section_title);
        }
    }

    public FeaturedBroadcastersAdapter(FragmentActivity activity) {
        this.mActivityRef = new WeakReference(activity);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri(new RoundedDrawable(BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_profile_pic), activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner), SECTION_TYPE)).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
        this.mSections.add(MainApplication.getRString(R.string.spotlight, new Object[SECTION_TYPE]));
        this.mSections.add(activity.getString(R.string.all));
    }

    public void addFeaturedUsers(User[] users) {
        if (users != null) {
            int i = this.mFeaturedUsers.size() + ITEM_TYPE;
            Collections.addAll(this.mFeaturedUsers, users);
            try {
                Crashlytics.log("FeaturedBroadcastersAdapter.notifyItemRangeInserted. i: " + i + "; length: " + users.length);
                notifyItemRangeInserted(i, users.length);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                safeNotifyDataSetChanged();
            }
        }
    }

    public void addUsers(User[] users) {
        if (users != null) {
            int i = this.mUsers.size();
            Collections.addAll(this.mUsers, users);
            try {
                Crashlytics.log("FeaturedBroadcastersAdapter.notifyItemRangeInserted. i: " + i + "; length: " + users.length);
                notifyItemRangeInserted((this.mSections.size() + this.mFeaturedUsers.size()) + i, users.length);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                safeNotifyDataSetChanged();
            }
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == 0) {
            return new SectionViewHolder(LayoutInflater.from((Context) this.mActivityRef.get()).inflate(R.layout.item_section, parent, false));
        }
        return new ItemViewHolder(LayoutInflater.from((Context) this.mActivityRef.get()).inflate(R.layout.item_user_broadcasters, parent, false), this.mActivityRef);
    }

    public int getItemViewType(int position) {
        return isHeaderPosition(position) ? SECTION_TYPE : ITEM_TYPE;
    }

    public int getItemCount() {
        return (this.mSections.size() + this.mFeaturedUsers.size()) + this.mUsers.size();
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeaderPosition(position)) {
            ((SectionViewHolder) holder).title.setText((CharSequence) this.mSections.get(position > 0 ? ITEM_TYPE : SECTION_TYPE));
        } else if (position <= this.mFeaturedUsers.size()) {
            onBindViewHolder((ItemViewHolder) holder, (User) this.mFeaturedUsers.get(position - 1), true);
        } else {
            int pos = (position - this.mSections.size()) - this.mFeaturedUsers.size();
            if (pos < 0 || pos >= this.mUsers.size()) {
                Log.e("!!!", "wrong position: " + position + " mUsers.size: " + this.mUsers.size());
            } else {
                onBindViewHolder((ItemViewHolder) holder, (User) this.mUsers.get(pos), false);
            }
        }
    }

    public User getItem(int position) {
        if (isHeaderPosition(position)) {
            return null;
        }
        if (position <= this.mFeaturedUsers.size()) {
            return (User) this.mFeaturedUsers.get(position - 1);
        }
        return (User) this.mUsers.get((position - this.mSections.size()) - this.mFeaturedUsers.size());
    }

    private void onBindViewHolder(ItemViewHolder holder, User user, boolean featured) {
        FragmentActivity activity = (FragmentActivity) this.mActivityRef.get();
        if (activity != null) {
            holder.icon.setImageBitmap(null);
            ImageLoader.getInstance().displayImage(user.profileLogoSmall != null ? user.profileLogoSmall : user.profileLogo, holder.icon, this.mDio);
            holder.username.setText(user.username);
            if (featured) {
                holder.description.setVisibility(8);
                holder.description.setText(String.valueOf(user.followerCount));
            } else {
                holder.description.setVisibility(SECTION_TYPE);
                TextView textView = holder.description;
                Object[] objArr = new Object[ITEM_TYPE];
                objArr[SECTION_TYPE] = user.followerCount;
                textView.setText(MainApplication.getRString(R.string._N_Followers, objArr));
            }
            if (holder.follow != null) {
                boolean z;
                if (PreferenceUtility.getUser()._id == null || !PreferenceUtility.getUser()._id.equals(user._id)) {
                    holder.follow.setVisibility(SECTION_TYPE);
                } else {
                    holder.follow.setVisibility(8);
                }
                holder.follow.setTag(user);
                TextView textView2 = holder.follow;
                if (user.currentFollowed) {
                    z = false;
                } else {
                    z = true;
                }
                textView2.setActivated(z);
                holder.follow.setText(user.currentFollowed ? R.string.UNFOLLOW : R.string.FOLLOW);
                holder.follow.setTextColor(activity.getResources().getColor(user.currentFollowed ? R.color.follow_inactive : R.color.follow_active));
                holder.follow.setOnClickListener(this);
            }
            if (holder.progress != null) {
                holder.progress.setVisibility(8);
            }
        }
    }

    public void onClick(View view) {
        boolean z = false;
        User tag = view.getTag();
        if (tag != null && (tag instanceof User)) {
            final User user = tag;
            final View progress = ((ViewGroup) view.getParent()).findViewById(R.id.progress);
            final int position = (this.mSections.size() + this.mFeaturedUsers.size()) + this.mUsers.indexOf(user);
            if (progress != null) {
                try {
                    progress.setVisibility(SECTION_TYPE);
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
                        user.followerCount = Integer.valueOf((response.booleanValue() ? FeaturedBroadcastersAdapter.ITEM_TYPE : -1) + user.followerCount.intValue());
                        try {
                            Crashlytics.log("FeaturedBroadcastersAdapter.notifyItemChanged " + position);
                            FeaturedBroadcastersAdapter.this.notifyItemChanged(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            FeaturedBroadcastersAdapter.this.safeNotifyDataSetChanged();
                        }
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

    private boolean isHeaderPosition(int position) {
        return position == 0 || position == this.mFeaturedUsers.size() + ITEM_TYPE;
    }

    private void safeNotifyDataSetChanged() {
        try {
            Crashlytics.log("FeaturedBroadcastersAdapter.notifyDataSetChanged");
            notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }
    }
}
