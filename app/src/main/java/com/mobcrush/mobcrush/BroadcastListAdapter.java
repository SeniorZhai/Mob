package com.mobcrush.mobcrush;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import io.fabric.sdk.android.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class BroadcastListAdapter extends ArrayAdapter<Broadcast> {
    private static final int LAYOUT_ID = 2130903087;
    private WeakReference<FragmentActivity> mActivityRef;
    private DisplayImageOptions mDio;
    private LayoutInflater mLayoutInflater;
    private Callback mOnEventsCallback;
    private Callback mOnNeedNextDataCallback;
    private boolean mShowUserNames = true;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private static final String CUSTOM_FONT_NAME = "Klavika-Light.ttf";
        public TextView mBroadcastNameView;
        public CardView mCardView;
        public TextView mGameNameView;
        public ImageView mImageView;
        public TextView mLikesView;
        public View mLiveView;
        public View mOverflowBtn;
        public View mProgressView;
        public View mUserGameDelimiter;
        public TextView mUserNameView;
        public TextView mViewersCountView;

        public ViewHolder(CardView v, OnClickListener listener) {
            super(v);
            this.mCardView = v;
            this.mImageView = (ImageView) v.findViewById(R.id.image);
            this.mLiveView = v.findViewById(R.id.live_text);
            Typeface tf = UIUtils.getTypeface(v.getContext(), CUSTOM_FONT_NAME);
            this.mGameNameView = (TextView) v.findViewById(R.id.game_name_text);
            this.mGameNameView.setTypeface(tf);
            this.mUserNameView = (TextView) v.findViewById(R.id.user_name_text);
            this.mUserNameView.setTypeface(tf);
            this.mBroadcastNameView = (TextView) v.findViewById(R.id.broadcast_name_text);
            this.mBroadcastNameView.setTypeface(tf);
            this.mViewersCountView = (TextView) v.findViewById(R.id.viewers_text);
            this.mViewersCountView.setTypeface(tf);
            this.mLikesView = (TextView) v.findViewById(R.id.likes_text);
            this.mLikesView.setTypeface(tf);
            this.mProgressView = v.findViewById(R.id.progressBar);
            this.mUserGameDelimiter = v.findViewById(R.id.user_game_delimiter);
            this.mOverflowBtn = v.findViewById(R.id.overflow_btn);
            v.setOnClickListener(listener);
        }
    }

    public BroadcastListAdapter(FragmentActivity activity) {
        super(activity, R.layout.card_broadcast);
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mActivityRef = new WeakReference(activity);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.cards_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).showImageOnFail((int) R.drawable.ic_error_grey600_36dp).cacheOnDisk(false).cacheInMemory(true).build();
    }

    public void addBroadcasts(Broadcast[] broadcasts) {
        addAll(Arrays.asList(broadcasts));
    }

    public void setOnEventsListener(Callback callback) {
        this.mOnEventsCallback = callback;
    }

    public void setOnNeedNextDataCallback(Callback callback) {
        this.mOnNeedNextDataCallback = callback;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_broadcast, parent, false);
            v.setUseCompatPadding(false);
            ViewHolder vh = new ViewHolder(v, null);
            convertView = vh.mCardView;
            convertView.setTag(R.integer.google_play_services_version, vh);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag(R.integer.google_play_services_version);
        if (holder != null) {
            onBindViewHolder(holder, position);
        }
        return convertView;
    }

    public void onBindViewHolder(final ViewHolder holder, int position) {
        int i = 0;
        Broadcast broadcast = (Broadcast) getItem(position);
        holder.mCardView.setTag(Integer.valueOf(position));
        holder.mLiveView.setVisibility(broadcast.isLive ? 0 : 4);
        if (this.mShowUserNames) {
            int i2;
            holder.mUserNameView.setVisibility(0);
            holder.mUserNameView.setText(broadcast.user != null ? broadcast.user.username : BuildConfig.FLAVOR);
            holder.mGameNameView.setText(broadcast.game != null ? broadcast.game.name : BuildConfig.FLAVOR);
            View view = holder.mUserGameDelimiter;
            if (broadcast.user == null || TextUtils.isEmpty(broadcast.user.username)) {
                i2 = 8;
            } else {
                i2 = 0;
            }
            view.setVisibility(i2);
        } else {
            holder.mGameNameView.setText(broadcast.game != null ? broadcast.game.name : BuildConfig.FLAVOR);
        }
        if (TextUtils.isEmpty(broadcast.title)) {
            holder.mBroadcastNameView.setHeight(0);
        } else {
            holder.mBroadcastNameView.setText(broadcast.title);
            LayoutParams layoutParams = holder.mBroadcastNameView.getLayoutParams();
            layoutParams.height = -2;
            holder.mBroadcastNameView.setLayoutParams(layoutParams);
        }
        holder.mLikesView.setText(String.valueOf(broadcast.likes));
        holder.mLikesView.setCompoundDrawablesWithIntrinsicBounds(holder.mLikesView.getResources().getDrawable(broadcast.currentLiked ? R.drawable.ic_like_feed_active : R.drawable.ic_like_feed), null, null, null);
        holder.mViewersCountView.setText(broadcast.getViewsNumber() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + MainApplication.getRString(broadcast.isLive ? R.string.viewers : R.string.views, new Object[0]));
        holder.mImageView.setImageBitmap(null);
        if (!(PreferenceUtility.getConfig() == null || PreferenceUtility.getConfig().snapshotBaseUrl == null)) {
            ImageLoader.getInstance().displayImage(PreferenceUtility.getConfig().videoSnapshotUrl.replace(Constants.BROADCAST_ID_HOLDER, broadcast._id), holder.mImageView, this.mDio, new ImageLoadingListener() {
                public void onLoadingStarted(String imageUri, View view) {
                    holder.mProgressView.setVisibility(0);
                }

                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.mImageView.setScaleType(ScaleType.CENTER);
                    holder.mProgressView.setVisibility(8);
                }

                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.mImageView.setScaleType(ScaleType.FIT_CENTER);
                    holder.mProgressView.setVisibility(8);
                    if (loadedImage != null) {
                        LayoutParams layoutParams = holder.mImageView.getLayoutParams();
                        layoutParams.height = (int) ((((double) holder.mImageView.getWidth()) * ((double) loadedImage.getHeight())) / ((double) loadedImage.getWidth()));
                        holder.mImageView.setLayoutParams(layoutParams);
                    }
                }

                public void onLoadingCancelled(String imageUri, View view) {
                    holder.mProgressView.setVisibility(8);
                }
            });
        }
        View view2 = holder.mOverflowBtn;
        if (!PreferenceUtility.getUser().equals(broadcast.user)) {
            i = 8;
        }
        view2.setVisibility(i);
        holder.mOverflowBtn.setTag(Integer.valueOf(position));
        if (position == getCount() - 5 && this.mOnNeedNextDataCallback != null) {
            this.mOnNeedNextDataCallback.handleMessage(Message.obtain(null, 2));
        }
    }
}
